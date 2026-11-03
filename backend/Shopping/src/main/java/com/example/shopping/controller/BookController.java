package com.example.shopping.controller;

import com.example.shopping.dto.BookSearchParam;
import com.example.shopping.dto.Result;
import com.example.shopping.entity.Book;
import com.example.shopping.log.LogUtil;
import com.example.shopping.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/publish")
    public Result<?> publishBook(
            @RequestParam String title,
            @RequestParam BigDecimal price,
            @RequestParam(required = false, defaultValue = "1") Integer quantity,
            @RequestParam String locate,
            @RequestPart MultipartFile[] images,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String edition,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {

        Integer sellerId = (Integer) request.getAttribute("currentUserId");

        Book book = new Book();
        book.setTitle(title);
        book.setPrice(price);
        book.setQuantity(quantity);
        book.setLocate(locate);
        book.setPublisher(publisher);
        book.setEdition(edition);
        book.setAuthor(author);
        book.setDescription(description);

        Result<?> result = bookService.publishBook(book, images, sellerId);
        if (result.getCode() == 200) {
            LogUtil.info("图书", sellerId, "发布图书请求", "title=" + title + ", price=" + price);
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Result<?> delistBook(@PathVariable Integer id,
                                HttpServletRequest request) {
        Integer currentUserId = (Integer) request.getAttribute("currentUserId");
        Result<?> result = bookService.delistBook(id, currentUserId);
        if (result.getCode() == 200) {
            LogUtil.info("图书", currentUserId, "下架图书请求", "bookId=" + id);
        }
        return result;
    }

    @GetMapping("/list")
    public Result<?> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/search")
    public Result<?> searchByKeyword(@RequestParam String keyword) {
        return bookService.searchByKeyword(keyword);
    }

    @GetMapping("/search/advanced")
    public Result<?> searchBooks(BookSearchParam param) {
        return bookService.searchBooks(param);
    }

    @GetMapping("/recommend")
    public Result<?> getRecommendBooks(@RequestParam(defaultValue = "10") int count) {
        return bookService.getRecommendBooks(count);
    }

    @GetMapping("/{id}")
    public Result<?> getBookDetail(@PathVariable Integer id) {
        return bookService.getBookDetail(id);
    }

    @GetMapping("/my")
    public Result<?> getMyBooks(HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("currentUserId");
        return bookService.getMyBooks(sellerId);
    }

    @PutMapping("/{id}")
    public Result<?> updateBook(
            @PathVariable Integer id,
            @RequestParam String title,
            @RequestParam BigDecimal price,
            @RequestParam(required = false, defaultValue = "1") Integer quantity,
            @RequestParam String locate,
            @RequestPart(required = false) MultipartFile[] images,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String edition,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {

        Integer currentUserId = (Integer) request.getAttribute("currentUserId");

        Book book = new Book();
        book.setTitle(title);
        book.setPrice(price);
        book.setQuantity(quantity);
        book.setLocate(locate);
        book.setPublisher(publisher);
        book.setEdition(edition);
        book.setAuthor(author);
        book.setDescription(description);

        Result<?> result = bookService.updateBook(id, book, images, currentUserId);
        if (result.getCode() == 200) {
            LogUtil.info("图书", currentUserId, "修改图书请求", "bookId=" + id + ", title=" + title + ", price=" + price);
        }
        return result;
    }
}
