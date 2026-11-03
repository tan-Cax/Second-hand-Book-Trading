package com.example.shopping.service;

import com.example.shopping.dao.*;
import com.example.shopping.dto.*;
import com.example.shopping.entity.*;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Service
public class OrderService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final BookDao bookDao;
    private final WalletDao walletDao;
    private final CartDao cartDao;
    private final FrozenAmountDao frozenAmountDao;
    private final TransactionLogDao transactionLogDao;
    private final RechargeService rechargeService;
    private final UserDao userDao;

    public OrderService(OrderDao orderDao, OrderItemDao orderItemDao, BookDao bookDao,
                        WalletDao walletDao, CartDao cartDao, FrozenAmountDao frozenAmountDao,
                        TransactionLogDao transactionLogDao, RechargeService rechargeService,
                        UserDao userDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.bookDao = bookDao;
        this.walletDao = walletDao;
        this.cartDao = cartDao;
        this.frozenAmountDao = frozenAmountDao;
        this.transactionLogDao = transactionLogDao;
        this.rechargeService = rechargeService;
        this.userDao = userDao;
    }

    public Result<?> createOrderFromCart(Integer userId, List<Integer> bookIds) {
        try {
            List<CartItemDTO> cartItems = cartDao.findByUserId(userId);
            if (bookIds != null && !bookIds.isEmpty()) {
                List<CartItemDTO> filtered = new ArrayList<>();
                for (CartItemDTO item : cartItems) {
                    if (bookIds.contains(item.getBookId())) {
                        filtered.add(item);
                    }
                }
                cartItems = filtered;
            }
            if (cartItems.isEmpty()) {
                return Result.fail(400, "购物车为空");
            }

            Wallet wallet = walletDao.findByUserId(userId);
            if (wallet == null) {
                return Result.fail(400, "钱包不存在");
            }
            if (wallet.getPayLocked() != null && wallet.getPayLocked() == 1) {
                return Result.fail(400, "钱包已被封禁，无法下单");
            }

            Set<Integer> sellerIds = new HashSet<>();
            for (CartItemDTO cartItem : cartItems) {
                Book book = bookDao.findById(cartItem.getBookId());
                if (book != null) {
                    sellerIds.add(book.getSellerId());
                }
            }
            if (sellerIds.size() > 1) {
                return Result.fail(400, "不能同时购买不同卖家的书籍，请分别下单");
            }

            Connection conn = DButil.getConnection();
            try {
                conn.setAutoCommit(false);

                List<CartItemDTO> currentCartItems = cartDao.findByUserId(userId, conn);
                if (bookIds != null && !bookIds.isEmpty()) {
                    List<CartItemDTO> filtered = new ArrayList<>();
                    for (CartItemDTO item : currentCartItems) {
                        if (bookIds.contains(item.getBookId())) {
                            filtered.add(item);
                        }
                    }
                    currentCartItems = filtered;
                }
                if (currentCartItems.isEmpty()) {
                    DButil.rollback(conn);
                    return Result.fail(400, "购物车为空");
                }

                BigDecimal totalAmount = BigDecimal.ZERO;
                List<OrderItem> orderItems = new ArrayList<>();
                Map<Integer, Integer> bookQuantityMap = new LinkedHashMap<>();

                for (CartItemDTO cartItem : currentCartItems) {
                    Book book = bookDao.findByIdForUpdate(cartItem.getBookId(), conn);
                    if (book == null) {
                        throw new SQLException("书籍不存在或已下架: bookId=" + cartItem.getBookId());
                    }
                    if (book.getQuantity() < cartItem.getQuantity()) {
                        throw new SQLException("库存不足: " + book.getTitle() + ", 库存=" + book.getQuantity());
                    }

                    BigDecimal itemAmount = book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                    totalAmount = totalAmount.add(itemAmount);

                    OrderItem item = new OrderItem();
                    item.setBookId(cartItem.getBookId());
                    item.setQuantity(cartItem.getQuantity());
                    item.setPrice(book.getPrice());
                    item.setAmount(itemAmount);
                    orderItems.add(item);

                    bookQuantityMap.put(cartItem.getBookId(), cartItem.getQuantity());
                }

                int orderId = orderDao.insert(userId, wallet.getId(), totalAmount, conn);
                orderItemDao.batchInsert(orderId, orderItems, conn);

                for (Map.Entry<Integer, Integer> entry : bookQuantityMap.entrySet()) {
                    bookDao.deductStock(entry.getKey(), entry.getValue(), conn);
                }

                cartDao.clearCart(userId, conn);

                conn.commit();
                LogUtil.info("订单", userId, "创建订单", "订单创建成功, orderId=" + orderId + ", 总金额=" + totalAmount);
                OrderCreateDTO dto = new OrderCreateDTO();
                dto.setOrderId(orderId);
                dto.setTotalAmount(totalAmount);
                return Result.ok(dto);
            } catch (Exception e) {
                DButil.rollback(conn);
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (Exception e) {
            LogUtil.warn("订单", userId, "创建订单", "下单失败: " + e.getMessage());
            return Result.fail(500, "下单失败: " + e.getMessage());
        }
    }

    public Result<?> payOrder(Integer userId, Integer orderId, String rawPayPassword) {
        try {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }
            if (!order.getUserId().equals(userId)) {
                return Result.fail(403, "无权操作此订单");
            }
            if (order.getStatus() != 0) {
                return Result.fail(400, "订单状态不正确，当前状态: " + order.getStatus());
            }

            Wallet buyerWallet = walletDao.findByUserId(userId);
            if (buyerWallet == null) {
                return Result.fail(400, "钱包不存在");
            }
            if (buyerWallet.getPayLocked() != null && buyerWallet.getPayLocked() == 1) {
                return Result.fail(400, "您的钱包已被封禁，无法支付");
            }

            Result<?> verifyResult = rechargeService.verifyPayPassword(userId, rawPayPassword);
            if (verifyResult.getCode() != 200) {
                return verifyResult;
            }

            List<OrderItem> items = orderItemDao.findByOrderId(orderId);
            if (items.isEmpty()) {
                return Result.fail(400, "订单项为空");
            }

            Map<Integer, List<OrderItem>> sellerItemsMap = new LinkedHashMap<>();
            for (OrderItem item : items) {
                Book book = bookDao.findById(item.getBookId());
                if (book == null) {
                    return Result.fail(400, "书籍不存在: bookId=" + item.getBookId());
                }
                Integer sellerId = book.getSellerId();
                sellerItemsMap.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(item);
            }

            Connection conn = DButil.getConnection();
            try {
                conn.setAutoCommit(false);

                for (Map.Entry<Integer, List<OrderItem>> entry : sellerItemsMap.entrySet()) {
                    Integer sellerId = entry.getKey();
                    User seller = userDao.findById(sellerId);
                    if (seller != null && seller.getIsBanned() != null && seller.getIsBanned() == 1) {
                        cancelOrderDueToSellerBanned(userId, orderId, items, conn);
                        conn.commit();
                        return Result.fail(400, "卖家已被封禁，订单已自动取消");
                    }

                    List<OrderItem> sellerItems = entry.getValue();
                    Wallet sellerWallet = walletDao.findByUserIdForUpdate(sellerId, conn);
                    if (sellerWallet == null) {
                        DButil.rollback(conn);
                        return Result.fail(400, "卖家暂未开通钱包，无法购买其书籍");
                    }
                    if (sellerWallet.getPayLocked() != null && sellerWallet.getPayLocked() == 1) {
                        DButil.rollback(conn);
                        return Result.fail(400, "卖家钱包已被封禁，无法购买其书籍");
                    }
                }

                walletDao.getBalanceForUpdate(buyerWallet.getId(), conn);
                walletDao.deductBalance(buyerWallet.getId(), order.getTotalAmount(), conn);

                for (Map.Entry<Integer, List<OrderItem>> entry : sellerItemsMap.entrySet()) {
                    Integer sellerId = entry.getKey();
                    List<OrderItem> sellerItems = entry.getValue();
                    Wallet sellerWallet = walletDao.findByUserIdForUpdate(sellerId, conn);

                    BigDecimal sellerAmount = BigDecimal.ZERO;
                    for (OrderItem item : sellerItems) {
                        sellerAmount = sellerAmount.add(item.getAmount());
                    }

                    FrozenAmount fa = new FrozenAmount();
                    fa.setOrderId(orderId);
                    fa.setPayerWalletId(buyerWallet.getId());
                    fa.setReceiverWalletId(sellerWallet.getId());
                    fa.setAmount(sellerAmount);
                    frozenAmountDao.insertWithConn(fa, conn);
                }

                TransactionLog log = new TransactionLog();
                log.setWalletId(buyerWallet.getId());
                log.setAmount(order.getTotalAmount());
                log.setType(2);
                log.setOrderId(orderId);
                log.setDescription("订单支付, orderId=" + orderId);
                transactionLogDao.insert(log, conn);

                orderDao.updateStatus(orderId, 0, 1, conn);

                conn.commit();
                LogUtil.info("订单", userId, "支付订单", "支付成功, orderId=" + orderId + ", 金额=" + order.getTotalAmount());
                return Result.ok();
            } catch (Exception e) {
                DButil.rollback(conn);
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (Exception e) {
            LogUtil.warn("订单", userId, "支付订单", "支付失败: " + e.getMessage());
            return Result.fail(500, "支付失败: " + e.getMessage());
        }
    }

    public Result<?> shipOrder(Integer sellerId, Integer orderId) {
        try {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }
            if (order.getStatus() != 1) {
                return Result.fail(400, "订单状态不正确，当前状态: " + order.getStatus());
            }

            List<OrderItem> items = orderItemDao.findByOrderId(orderId);
            boolean isSeller = items.stream().anyMatch(item -> {
                try {
                    Book book = bookDao.findById(item.getBookId());
                    return book != null && book.getSellerId().equals(sellerId);
                } catch (Exception e) {
                    return false;
                }
            });

            if (!isSeller) {
                return Result.fail(403, "无权操作此订单");
            }

            Connection conn = DButil.getConnection();
            try {
                conn.setAutoCommit(false);
                orderDao.updateStatus(orderId, 1, 2, conn);
                conn.commit();
                LogUtil.info("订单", sellerId, "发货", "发货成功, orderId=" + orderId);
                return Result.ok();
            } catch (Exception e) {
                DButil.rollback(conn);
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (Exception e) {
            return Result.fail(500, "发货失败: " + e.getMessage());
        }
    }

    public Result<?> confirmReceipt(Integer userId, Integer orderId) {
        try {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }
            if (!order.getUserId().equals(userId)) {
                return Result.fail(403, "无权操作此订单");
            }
            if (order.getStatus() != 2) {
                return Result.fail(400, "订单状态不正确，当前状态: " + order.getStatus());
            }

            Connection conn = DButil.getConnection();
            try {
                conn.setAutoCommit(false);

                List<FrozenAmount> frozenList = frozenAmountDao.findByOrderId(orderId, conn);
                if (frozenList.isEmpty()) {
                    DButil.rollback(conn);
                    return Result.fail(400, "冻结金额记录不存在");
                }

                for (FrozenAmount fa : frozenList) {
                    walletDao.addBalance(fa.getReceiverWalletId(), fa.getAmount(), conn);
                    frozenAmountDao.updateStatus(fa.getId(), 1, 2, conn);

                    TransactionLog log = new TransactionLog();
                    log.setWalletId(fa.getReceiverWalletId());
                    log.setAmount(fa.getAmount());
                    log.setType(3);
                    log.setOrderId(orderId);
                    log.setDescription("订单确认收货, 资金入账, orderId=" + orderId);
                    transactionLogDao.insert(log, conn);
                }

                orderDao.updateStatus(orderId, 2, 3, conn);

                conn.commit();
                LogUtil.info("订单", userId, "确认收货", "确认收货成功, orderId=" + orderId);
                return Result.ok();
            } catch (Exception e) {
                DButil.rollback(conn);
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (Exception e) {
            return Result.fail(500, "确认收货失败: " + e.getMessage());
        }
    }

    public Result<?> cancelOrder(Integer userId, Integer orderId) {
        try {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }
            if (!order.getUserId().equals(userId)) {
                return Result.fail(403, "无权操作此订单");
            }
            if (order.getStatus() != 0 && order.getStatus() != 1) {
                return Result.fail(400, "当前订单状态不可取消");
            }

            Connection conn = DButil.getConnection();
            try {
                conn.setAutoCommit(false);

                Order currentOrder = orderDao.findById(orderId, conn);

                List<OrderItem> items = orderItemDao.findByOrderId(orderId, conn);

                for (OrderItem item : items) {
                    bookDao.restoreStock(item.getBookId(), item.getQuantity(), conn);
                }

                if (currentOrder.getStatus() == 1) {
                    List<FrozenAmount> frozenList = frozenAmountDao.findByOrderId(orderId, conn);
                    for (FrozenAmount fa : frozenList) {
                        walletDao.addBalance(fa.getPayerWalletId(), fa.getAmount(), conn);
                        frozenAmountDao.updateStatus(fa.getId(), 1, 3, conn);

                        TransactionLog log = new TransactionLog();
                        log.setWalletId(fa.getPayerWalletId());
                        log.setAmount(fa.getAmount());
                        log.setType(4);
                        log.setOrderId(orderId);
                        log.setDescription("订单取消退款, orderId=" + orderId);
                        transactionLogDao.insert(log, conn);
                    }
                }

                orderDao.updateStatus(orderId, currentOrder.getStatus(), 4, conn);

                conn.commit();
                LogUtil.info("订单", userId, "取消订单", "取消成功, orderId=" + orderId);
                return Result.ok();
            } catch (Exception e) {
                DButil.rollback(conn);
                throw e;
            } finally {
                DButil.close(conn);
            }
        } catch (Exception e) {
            return Result.fail(500, "取消失败: " + e.getMessage());
        }
    }

    public Result<?> getMyOrders(Integer userId, Integer status, int page, int size) {
        try {
            List<Order> orders;
            int total;
            if (status == null) {
                orders = orderDao.findByUserId(userId, page, size);
                total = orderDao.countByUserId(userId);
            } else {
                orders = orderDao.findByUserIdAndStatus(userId, status, page, size);
                total = orderDao.countByUserIdAndStatus(userId, status);
            }
            List<OrderListItemDTO> list = new ArrayList<>();
            for (Order order : orders) {
                list.add(convertToListItem(order));
            }
            return Result.ok(PageResult.of(list, total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> getSellerOrders(Integer sellerId, int page, int size) {
        try {
            List<Order> orders = orderDao.findBySellerId(sellerId, page, size);
            List<OrderListItemDTO> list = new ArrayList<>();
            for (Order order : orders) {
                list.add(convertToListItem(order));
            }
            int total = orderDao.countBySellerId(sellerId);
            return Result.ok(PageResult.of(list, total, page, size));
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> getOrderDetail(Integer userId, Integer orderId) {
        try {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                return Result.fail(404, "订单不存在");
            }

            List<OrderItem> items = orderItemDao.findByOrderId(orderId);
            boolean isBuyer = order.getUserId().equals(userId);
            boolean isSeller = false;
            for (OrderItem item : items) {
                Book book = bookDao.findByIdIncludingDeleted(item.getBookId());
                if (book != null && book.getSellerId().equals(userId)) {
                    isSeller = true;
                    break;
                }
            }

            if (!isBuyer && !isSeller) {
                return Result.fail(403, "无权查看此订单");
            }

            OrderDetailDTO dto = new OrderDetailDTO();
            dto.setId(order.getId());
            dto.setUserId(order.getUserId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setStatusText(getStatusText(order.getStatus()));
            dto.setOrderTime(order.getOrderTime());

            List<OrderItemDetailDTO> itemList = new ArrayList<>();
            for (OrderItem item : items) {
                OrderItemDetailDTO itemDto = new OrderItemDetailDTO();
                itemDto.setId(item.getId());
                itemDto.setBookId(item.getBookId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setPrice(item.getPrice());
                itemDto.setAmount(item.getAmount());
                Book book = bookDao.findByIdIncludingDeleted(item.getBookId());
                if (book != null) {
                    itemDto.setTitle(book.getTitle());
                }
                itemList.add(itemDto);
            }
            dto.setItems(itemList);

            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    private OrderListItemDTO convertToListItem(Order order) {
        OrderListItemDTO dto = new OrderListItemDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setStatusText(getStatusText(order.getStatus()));
        dto.setOrderTime(order.getOrderTime());
        return dto;
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

    private void cancelOrderDueToSellerBanned(Integer userId, Integer orderId,
                                               List<OrderItem> items, Connection conn) throws SQLException {
        for (OrderItem item : items) {
            bookDao.restoreStock(item.getBookId(), item.getQuantity(), conn);
        }

        List<FrozenAmount> frozenList = frozenAmountDao.findByOrderId(orderId, conn);
        for (FrozenAmount fa : frozenList) {
            walletDao.addBalance(fa.getPayerWalletId(), fa.getAmount(), conn);
            frozenAmountDao.updateStatus(fa.getId(), 1, 3, conn);

            TransactionLog log = new TransactionLog();
            log.setWalletId(fa.getPayerWalletId());
            log.setAmount(fa.getAmount());
            log.setType(4);
            log.setOrderId(orderId);
            log.setDescription("卖家被封禁，订单自动取消退款, orderId=" + orderId);
            transactionLogDao.insert(log, conn);
        }

        orderDao.updateStatus(orderId, 0, 4, conn);
        LogUtil.warn("订单", userId, "支付订单", "卖家已被封禁，订单自动取消, orderId=" + orderId);
    }
}
