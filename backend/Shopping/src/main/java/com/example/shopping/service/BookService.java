package com.example.shopping.service;

import com.example.shopping.dao.BookDao;
import com.example.shopping.dao.BookImageDao;
import com.example.shopping.dao.OrderDao;
import com.example.shopping.dao.UserDao;
import com.example.shopping.dao.WalletDao;
import com.example.shopping.dto.*;
import com.example.shopping.entity.Book;
import com.example.shopping.entity.BookImage;
import com.example.shopping.entity.User;
import com.example.shopping.entity.Wallet;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.DButil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

@Service
public class BookService {

    private final BookDao bookDao;
    private final BookImageDao bookImageDao;
    private final OrderDao orderDao;
    private final WalletDao walletDao;
    private final UserDao userDao;

    @Value("${book.upload.path}")
    private String uploadPath;

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;
    private static final int MAX_IMAGE_COUNT = 3;
    private static final int MIN_IMAGE_COUNT = 1;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    public BookService(BookDao bookDao, BookImageDao bookImageDao, OrderDao orderDao, WalletDao walletDao, UserDao userDao) {
        this.bookDao = bookDao;
        this.bookImageDao = bookImageDao;
        this.orderDao = orderDao;
        this.walletDao = walletDao;
        this.userDao = userDao;
    }

    public Result<?> publishBook(Book book, MultipartFile[] images, Integer sellerId) {
        try {
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                return Result.fail(400, "书名不能为空");
            }
            if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.fail(400, "价格必须大于0");
            }
            if (book.getLocate() == null || book.getLocate().trim().isEmpty()) {
                return Result.fail(400, "位置信息不能为空");
            }
            if (images == null || images.length < MIN_IMAGE_COUNT) {
                return Result.fail(400, "至少上传一张图片");
            }
            if (images.length > MAX_IMAGE_COUNT) {
                return Result.fail(400, "最多上传三张图片");
            }

            for (MultipartFile image : images) {
                if (image.getSize() > MAX_IMAGE_SIZE) {
                    return Result.fail(400, "图片 " + image.getOriginalFilename() + " 超过2MB限制");
                }
                String ext = getFileExtension(image.getOriginalFilename());
                if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
                    return Result.fail(400, "图片 " + image.getOriginalFilename() + " 格式不支持，仅支持 jpg/jpeg/png/gif/webp");
                }
            }

            book.setSellerId(sellerId);
            if (book.getQuantity() == null || book.getQuantity() < 1) {
                book.setQuantity(1);
            }

            User seller = userDao.findById(sellerId);
            if (seller != null && seller.getIsBanned() != null && seller.getIsBanned() == 1) {
                return Result.fail(403, "账号已被封禁，无法发布书籍");
            }

            Wallet wallet = walletDao.findByUserId(sellerId);
            if (wallet == null) {
                return Result.fail(400, "请先开通钱包后再上架书籍");
            }

            Connection conn = DButil.getConnection();
            String[] savedFileNames = new String[images.length];
            try {
                conn.setAutoCommit(false);
                int bookId = bookDao.insertWithConn(book, conn);

                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    String ext = getFileExtension(image.getOriginalFilename());
                    String fileName = LocalDate.now().toString().replace("-", "") +
                                     "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
                    String filePath = uploadPath + File.separator + fileName;
                    image.transferTo(new File(filePath));
                    savedFileNames[i] = fileName;
                    String imageUrl = "/uploads/images/" + fileName;
                    bookImageDao.insert(bookId, imageUrl, i, conn);
                }

                conn.commit();
                LogUtil.info("图书", sellerId, "发布图书", "发布成功, bookId=" + bookId + ", title=" + book.getTitle() + ", price=" + book.getPrice());
                BookPublishDTO dto = new BookPublishDTO();
                dto.setBookId(bookId);
                return Result.ok(dto);
            } catch (Exception e) {
                DButil.rollback(conn);
                for (String fn : savedFileNames) {
                    if (fn != null) new File(uploadPath + File.separator + fn).delete();
                }
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (IOException e) {
            return Result.fail(500, "图片保存失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "发布失败: " + e.getMessage());
        }
    }

    public Result<?> delistBook(Integer bookId, Integer currentUserId) {
        try {
            Book book = bookDao.findById(bookId);
            if (book == null) {
                LogUtil.warn("图书", currentUserId, "下架图书", "书籍不存在, bookId=" + bookId);
                return Result.fail(404, "书籍不存在");
            }
            if (!book.getSellerId().equals(currentUserId)) {
                LogUtil.warn("图书", currentUserId, "下架图书", "无权操作他人的书籍, bookId=" + bookId);
                return Result.fail(403, "无权操作他人的书籍");
            }
            if (book.getIsDeleted() == 1) {
                LogUtil.warn("图书", currentUserId, "下架图书", "书籍已下架, bookId=" + bookId);
                return Result.fail(400, "书籍已下架");
            }

            boolean hasPending = orderDao.hasUnfinishedOrders(bookId);
            if (hasPending) {
                return Result.fail(400, "该书籍有待完成的订单，无法下架");
            }

            bookDao.deleteById(bookId);
            LogUtil.info("图书", currentUserId, "下架图书", "下架成功, bookId=" + bookId + ", title=" + book.getTitle());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "下架失败: " + e.getMessage());
        }
    }

    public Result<?> getAllBooks() {
        try {
            List<BookListDTO> books = bookDao.findAllBooks();
            return Result.ok(books);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> searchByKeyword(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.fail(400, "搜索关键词不能为空");
            }
            List<BookListDTO> books = bookDao.searchByKeyword(keyword.trim());
            return Result.ok(books);
        } catch (Exception e) {
            return Result.fail(500, "搜索失败: " + e.getMessage());
        }
    }

    public Result<?> searchBooks(BookSearchParam param) {
        try {
            List<BookListDTO> books = bookDao.searchBooks(param);
            return Result.ok(books);
        } catch (Exception e) {
            return Result.fail(500, "搜索失败: " + e.getMessage());
        }
    }

    public Result<?> getRecommendBooks(int count) {
        try {
            List<BookListDTO> books = bookDao.findRandomBooks(count);
            return Result.ok(books);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> getBookDetail(Integer bookId) {
        try {
            Book book = bookDao.findById(bookId);
            if (book == null) {
                return Result.fail(404, "书籍不存在");
            }

            List<BookImage> images = bookImageDao.findByBookId(bookId);

            BookDetailDTO dto = new BookDetailDTO();
            dto.setId(book.getId());
            dto.setSellerId(book.getSellerId());
            dto.setTitle(book.getTitle());
            dto.setPublisher(book.getPublisher());
            dto.setEdition(book.getEdition());
            dto.setAuthor(book.getAuthor());
            dto.setDescription(book.getDescription());
            dto.setPrice(book.getPrice());
            dto.setQuantity(book.getQuantity());
            dto.setLocate(book.getLocate());
            dto.setCreatedTime(book.getCreatedTime());
            dto.setImages(images);

            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> getMyBooks(Integer sellerId) {
        try {
            List<SellerBookDTO> myBooks = bookDao.findBySellerId(sellerId);
            return Result.ok(myBooks);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> updateBook(Integer bookId, Book book, MultipartFile[] images, Integer currentUserId) {
        try {
            Book existing = bookDao.findById(bookId);
            if (existing == null) {
                LogUtil.warn("图书", currentUserId, "修改图书", "书籍不存在, bookId=" + bookId);
                return Result.fail(404, "书籍不存在");
            }
            if (!existing.getSellerId().equals(currentUserId)) {
                LogUtil.warn("图书", currentUserId, "修改图书", "无权操作他人的书籍, bookId=" + bookId);
                return Result.fail(403, "无权操作他人的书籍");
            }
            if (existing.getIsDeleted() == 1) {
                LogUtil.warn("图书", currentUserId, "修改图书", "书籍已下架, bookId=" + bookId);
                return Result.fail(400, "书籍已下架");
            }

            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                return Result.fail(400, "书名不能为空");
            }
            if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.fail(400, "价格必须大于0");
            }
            if (book.getLocate() == null || book.getLocate().trim().isEmpty()) {
                return Result.fail(400, "位置信息不能为空");
            }

            boolean hasNewImages = images != null && images.length > 0;
            if (hasNewImages) {
                if (images.length > MAX_IMAGE_COUNT) {
                    return Result.fail(400, "最多上传三张图片");
                }
                for (MultipartFile image : images) {
                    if (image.getSize() > MAX_IMAGE_SIZE) {
                        return Result.fail(400, "图片 " + image.getOriginalFilename() + " 超过2MB限制");
                    }
                    String ext = getFileExtension(image.getOriginalFilename());
                    if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
                        return Result.fail(400, "图片 " + image.getOriginalFilename() + " 格式不支持");
                    }
                }
            }

            book.setId(bookId);
            book.setSellerId(existing.getSellerId());
            if (book.getQuantity() == null || book.getQuantity() < 1) {
                book.setQuantity(1);
            }

            Connection conn = DButil.getConnection();
            String[] savedFileNames = hasNewImages ? new String[images.length] : null;
            try {
                conn.setAutoCommit(false);
                bookDao.updateBook(book, conn);

                if (hasNewImages) {
                    List<BookImage> oldImages = bookImageDao.findByBookId(bookId);
                    for (BookImage oldImage : oldImages) {
                        String fileName = java.nio.file.Paths.get(oldImage.getImageUrl()).getFileName().toString();
                        File oldFile = new File(uploadPath, fileName);
                        oldFile.delete();
                    }

                    bookImageDao.deleteByBookId(bookId, conn);

                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }

                    for (int i = 0; i < images.length; i++) {
                        MultipartFile image = images[i];
                        String ext = getFileExtension(image.getOriginalFilename());
                        String fileName = LocalDate.now().toString().replace("-", "") +
                                         "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
                        String filePath = uploadPath + File.separator + fileName;
                        image.transferTo(new File(filePath));
                        savedFileNames[i] = fileName;
                        String imageUrl = "/uploads/images/" + fileName;
                        bookImageDao.insert(bookId, imageUrl, i, conn);
                    }
                }

                conn.commit();
                LogUtil.info("图书", currentUserId, "修改图书", "修改成功, bookId=" + bookId + ", title=" + book.getTitle());
                return Result.ok();
            } catch (Exception e) {
                DButil.rollback(conn);
                if (savedFileNames != null) {
                    for (String fn : savedFileNames) {
                        if (fn != null) new File(uploadPath + File.separator + fn).delete();
                    }
                }
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (IOException e) {
            return Result.fail(500, "图片保存失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "修改失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
