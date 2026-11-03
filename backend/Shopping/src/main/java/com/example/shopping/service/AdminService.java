package com.example.shopping.service;

import com.example.shopping.dao.*;
import com.example.shopping.dto.*;
import com.example.shopping.entity.*;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;

@Service
public class AdminService {

    private final UserDao userDao;
    private final WalletDao walletDao;
    private final BookDao bookDao;
    private final BookImageDao bookImageDao;
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final FrozenAmountDao frozenAmountDao;
    private final TransactionLogDao transactionLogDao;

    public AdminService(UserDao userDao, WalletDao walletDao, BookDao bookDao,
                        BookImageDao bookImageDao, OrderDao orderDao, OrderItemDao orderItemDao,
                        FrozenAmountDao frozenAmountDao, TransactionLogDao transactionLogDao) {
        this.userDao = userDao;
        this.walletDao = walletDao;
        this.bookDao = bookDao;
        this.bookImageDao = bookImageDao;
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.frozenAmountDao = frozenAmountDao;
        this.transactionLogDao = transactionLogDao;
    }

    private boolean isAdmin(Integer userId) {
        try {
            User user = userDao.findById(userId);
            return user != null && user.getRole() != null && user.getRole() == 1;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 支付账户管理 ====================

    public Result<?> unlockPayAccount(Integer adminUserId, Integer targetUserId) {
        try {
            if (!isAdmin(adminUserId)) {
                LogUtil.warn("管理", adminUserId, "解封支付账户", "无权限，非管理员操作");
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            User targetUser = userDao.findById(targetUserId);
            if (targetUser == null) {
                return Result.fail(400, "用户不存在");
            }

            Wallet targetWallet = walletDao.findByUserId(targetUserId);
            if (targetWallet == null) {
                return Result.fail(400, "目标用户钱包不存在");
            }

            walletDao.unlockPayAccount(targetWallet.getId());

            LogUtil.info("管理", adminUserId, "解封支付账户", "解封成功, targetUserId=" + targetUserId + ", walletId=" + targetWallet.getId());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "解封失败: " + e.getMessage());
        }
    }

    public Result<?> lockPayAccount(Integer adminUserId, Integer targetUserId) {
        try {
            if (!isAdmin(adminUserId)) {
                LogUtil.warn("管理", adminUserId, "封禁支付账户", "无权限，非管理员操作");
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            User targetUser = userDao.findById(targetUserId);
            if (targetUser == null) {
                return Result.fail(400, "用户不存在");
            }

            if (targetUser.getRole() != null && targetUser.getRole() == 1) {
                return Result.fail(400, "不能锁定管理员的支付账户");
            }

            Wallet targetWallet = walletDao.findByUserId(targetUserId);
            if (targetWallet == null) {
                return Result.fail(400, "目标用户钱包不存在");
            }

            if (targetWallet.getPayLocked() != null && targetWallet.getPayLocked() == 1) {
                return Result.fail(400, "该用户支付账户已被封禁");
            }

            walletDao.lockPayAccount(targetWallet.getId());

            LogUtil.info("管理", adminUserId, "封禁支付账户", "封禁成功, targetUserId=" + targetUserId + ", walletId=" + targetWallet.getId());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "封禁失败: " + e.getMessage());
        }
    }

    // ==================== 用户账号管理 ====================

    public Result<?> banUser(Integer adminUserId, Integer targetUserId) {
        try {
            if (!isAdmin(adminUserId)) {
                LogUtil.warn("管理", adminUserId, "封禁用户", "无权限，非管理员操作");
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            User targetUser = userDao.findById(targetUserId);
            if (targetUser == null) {
                return Result.fail(400, "用户不存在");
            }

            if (targetUser.getRole() != null && targetUser.getRole() == 1) {
                return Result.fail(400, "不能封禁管理员账号");
            }

            if (targetUser.getIsBanned() != null && targetUser.getIsBanned() == 1) {
                return Result.fail(400, "该用户已被封禁");
            }

            Connection conn = DButil.getConnection();
            try {
                conn.setAutoCommit(false);

                userDao.updateBanStatus(targetUserId, 1, conn);
                bookDao.delistBySellerId(targetUserId, conn);

                Wallet bannedSellerWallet = walletDao.findByUserId(targetUserId);

                List<Order> unfinishedOrders = orderDao.findUnfinishedBySellerId(targetUserId, conn);

                int cancelledCount = 0;
                int refundedCount = 0;

                for (Order order : unfinishedOrders) {
                    List<OrderItem> items = orderItemDao.findByOrderId(order.getId(), conn);

                    for (OrderItem item : items) {
                        bookDao.restoreStock(item.getBookId(), item.getQuantity(), conn);
                    }

                    if (order.getStatus() == 1 || order.getStatus() == 2) {
                        List<FrozenAmount> frozenList = frozenAmountDao.findByOrderId(order.getId(), conn);
                        for (FrozenAmount fa : frozenList) {
                            if (bannedSellerWallet != null
                                    && fa.getReceiverWalletId().equals(bannedSellerWallet.getId())
                                    && fa.getStatus() == 1) {
                                walletDao.addBalance(fa.getPayerWalletId(), fa.getAmount(), conn);
                                frozenAmountDao.updateStatus(fa.getId(), 1, 3, conn);

                                TransactionLog log = new TransactionLog();
                                log.setWalletId(fa.getPayerWalletId());
                                log.setAmount(fa.getAmount());
                                log.setType(4);
                                log.setOrderId(order.getId());
                                log.setDescription("卖家被封禁，订单自动退款, orderId=" + order.getId());
                                transactionLogDao.insert(log, conn);
                                refundedCount++;
                            }
                        }
                    }

                    orderDao.updateStatus(order.getId(), order.getStatus(), 4, conn);
                    cancelledCount++;
                }

                conn.commit();

                LogUtil.info("管理", adminUserId, "封禁用户", "封禁成功, targetUserId=" + targetUserId
                        + ", 下架书籍, 取消订单" + cancelledCount + "个, 退款" + refundedCount + "笔");

                AdminBanDTO dto = new AdminBanDTO();
                dto.setCancelledOrders(cancelledCount);
                dto.setRefundedOrders(refundedCount);
                return Result.ok(dto);
            } catch (Exception e) {
                DButil.rollback(conn);
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (Exception e) {
            return Result.fail(500, "封禁失败: " + e.getMessage());
        }
    }

    public Result<?> unbanUser(Integer adminUserId, Integer targetUserId) {
        try {
            if (!isAdmin(adminUserId)) {
                LogUtil.warn("管理", adminUserId, "解封用户", "无权限，非管理员操作");
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            User targetUser = userDao.findById(targetUserId);
            if (targetUser == null) {
                return Result.fail(400, "用户不存在");
            }

            if (targetUser.getIsBanned() == null || targetUser.getIsBanned() != 1) {
                return Result.fail(400, "该用户未被封禁");
            }

            userDao.updateBanStatus(targetUserId, 0);

            LogUtil.info("管理", adminUserId, "解封用户", "解封成功, targetUserId=" + targetUserId);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "解封失败: " + e.getMessage());
        }
    }

    public Result<?> getUsers(Integer adminUserId, String keyword, int page, int size) {
        try {
            if (!isAdmin(adminUserId)) {
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            List<User> users = userDao.findAll(keyword, page, size);
            List<AdminUserDTO> userList = new ArrayList<>();
            for (User user : users) {
                Wallet wallet = walletDao.findByUserId(user.getId());
                AdminUserDTO dto = new AdminUserDTO();
                dto.setId(user.getId());
                dto.setName(user.getName());
                dto.setRole(user.getRole());
                dto.setIsBanned(user.getIsBanned());
                dto.setPayLocked(wallet != null ? wallet.getPayLocked() : null);
                dto.setCreatedTime(user.getCreatedTime());
                userList.add(dto);
            }

            int total = userDao.countByKeyword(keyword);
            return Result.ok(PageResult.of(userList, total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // ==================== 订单管理 ====================

    public Result<?> getAllOrders(Integer adminUserId, Integer status, int page, int size) {
        try {
            if (!isAdmin(adminUserId)) {
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            List<Order> orders;
            int total;
            if (status != null) {
                orders = orderDao.findAllByStatus(status, page, size);
                total = orderDao.countByStatus(status);
            } else {
                orders = orderDao.findAll(page, size);
                total = orderDao.countAll();
            }

            List<AdminOrderDTO> orderList = new ArrayList<>();
            for (Order order : orders) {
                AdminOrderDTO dto = new AdminOrderDTO();
                dto.setId(order.getId());
                dto.setUserId(order.getUserId());
                dto.setTotalAmount(order.getTotalAmount());
                dto.setStatus(order.getStatus());
                dto.setStatusText(getStatusText(order.getStatus()));
                dto.setOrderTime(order.getOrderTime());
                User buyer = userDao.findById(order.getUserId());
                if (buyer != null) {
                    dto.setBuyerName(buyer.getName());
                }
                orderList.add(dto);
            }

            return Result.ok(PageResult.of(orderList, total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> getOrderDetail(Integer adminUserId, Integer orderId) {
        try {
            if (!isAdmin(adminUserId)) {
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            Order order = orderDao.findById(orderId);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }

            AdminOrderDetailDTO dto = new AdminOrderDetailDTO();
            dto.setId(order.getId());
            dto.setUserId(order.getUserId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setStatusText(getStatusText(order.getStatus()));
            dto.setOrderTime(order.getOrderTime());

            User buyer = userDao.findById(order.getUserId());
            if (buyer != null) {
                dto.setBuyerName(buyer.getName());
            }

            List<OrderItem> items = orderItemDao.findByOrderId(orderId);
            List<AdminOrderItemDTO> itemList = new ArrayList<>();
            for (OrderItem item : items) {
                AdminOrderItemDTO itemDto = new AdminOrderItemDTO();
                itemDto.setId(item.getId());
                itemDto.setBookId(item.getBookId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setPrice(item.getPrice());
                itemDto.setAmount(item.getAmount());
                Book book = bookDao.findByIdIncludingDeleted(item.getBookId());
                if (book != null) {
                    itemDto.setTitle(book.getTitle());
                    itemDto.setSellerId(book.getSellerId());
                    User seller = userDao.findById(book.getSellerId());
                    if (seller != null) {
                        itemDto.setSellerName(seller.getName());
                    }
                }
                itemList.add(itemDto);
            }
            dto.setItems(itemList);

            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // ==================== 交易流水审计 ====================

    public Result<?> getAllTransactions(Integer adminUserId, Integer walletId, Integer type, int page, int size) {
        try {
            if (!isAdmin(adminUserId)) {
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            List<TransactionLog> logs;
            if (walletId != null) {
                logs = transactionLogDao.findByWalletId(walletId, page, size);
            } else if (type != null) {
                logs = transactionLogDao.findByType(type, page, size);
            } else {
                logs = transactionLogDao.findAll(page, size);
            }

            return Result.ok(logs);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // ==================== 平台统计 ====================

    public Result<?> getStatistics(Integer adminUserId) {
        try {
            if (!isAdmin(adminUserId)) {
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            AdminStatisticsDTO stats = new AdminStatisticsDTO();
            stats.setTotalUsers(userDao.countAll());
            stats.setTotalOrders(orderDao.countAll());
            stats.setOrdersByStatus(Map.of(
                "待支付", orderDao.countByStatus(0),
                "待发货", orderDao.countByStatus(1),
                "待收货", orderDao.countByStatus(2),
                "已完成", orderDao.countByStatus(3),
                "已取消", orderDao.countByStatus(4)
            ));
            stats.setTotalRecharge(transactionLogDao.sumAmountByType(1));
            stats.setTotalSales(transactionLogDao.sumAmountByType(3));
            stats.setTotalRefund(transactionLogDao.sumAmountByType(4));
            stats.setTotalWallets(walletDao.countAll());

            return Result.ok(stats);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    // ==================== 图书管理 ====================

    public Result<?> getDeletedBooks(Integer adminUserId) {
        try {
            if (!isAdmin(adminUserId)) {
                LogUtil.warn("管理", adminUserId, "查看删除书籍", "无权限，非管理员操作");
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            List<Book> deletedBooks = bookDao.findDeletedBooks();
            List<DeletedBookDTO> bookList = new ArrayList<>();

            for (Book book : deletedBooks) {
                boolean hasUnfinished = orderDao.hasUnfinishedOrders(book.getId());
                DeletedBookDTO dto = new DeletedBookDTO();
                dto.setId(book.getId());
                dto.setTitle(book.getTitle());
                dto.setSellerId(book.getSellerId());
                dto.setPrice(book.getPrice());
                dto.setQuantity(book.getQuantity());
                dto.setLocate(book.getLocate());
                dto.setHasUnfinishedOrders(hasUnfinished);
                dto.setCanDelete(!hasUnfinished);
                bookList.add(dto);
            }

            LogUtil.info("管理", adminUserId, "查看删除书籍", "查询逻辑删除书籍列表，共" + bookList.size() + "本");
            return Result.ok(bookList);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> permanentDeleteBook(Integer adminUserId, Integer bookId) {
        try {
            if (!isAdmin(adminUserId)) {
                LogUtil.warn("管理", adminUserId, "永久删除书籍", "无权限，非管理员操作");
                return Result.fail(403, "无权限，仅管理员可操作");
            }

            Book book = bookDao.findByIdIncludingDeleted(bookId);
            if (book == null) {
                return Result.fail(404, "书籍不存在");
            }

            if (book.getIsDeleted() == null || book.getIsDeleted() != 1) {
                return Result.fail(400, "只能删除已逻辑删除的书籍");
            }

            boolean hasUnfinished = orderDao.hasUnfinishedOrders(bookId);
            if (hasUnfinished) {
                LogUtil.warn("管理", adminUserId, "永久删除书籍", "书籍存在未完成订单，无法删除, bookId=" + bookId);
                return Result.fail(400, "该书籍存在未完成的订单，无法永久删除");
            }

            boolean hasAnyOrders = orderDao.hasAnyOrders(bookId);
            if (hasAnyOrders) {
                LogUtil.warn("管理", adminUserId, "永久删除书籍", "书籍存在关联订单，无法删除, bookId=" + bookId);
                return Result.fail(400, "该书籍存在关联订单，无法永久删除");
            }

            bookDao.permanentDelete(bookId);

            LogUtil.info("管理", adminUserId, "永久删除书籍", "永久删除成功, bookId=" + bookId + ", title=" + book.getTitle());
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "删除失败: " + e.getMessage());
        }
    }

    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }
}
