package com.example.shopping.scheduler;

import com.example.shopping.dao.BookDao;
import com.example.shopping.dao.OrderDao;
import com.example.shopping.dao.OrderItemDao;
import com.example.shopping.entity.Order;
import com.example.shopping.entity.OrderItem;
import com.example.shopping.log.LogUtil;
import com.example.shopping.utils.DButil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;

@Component
public class OrderScheduler {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final BookDao bookDao;

    public OrderScheduler(OrderDao orderDao, OrderItemDao orderItemDao, BookDao bookDao) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.bookDao = bookDao;
    }

    @Scheduled(fixedRate = 60000)
    public void cancelStaleUnpaidOrders() {
        final int BATCH_SIZE = 100;
        try {
            List<Order> staleOrders;
            do {
                staleOrders = orderDao.findStaleUnpaidOrders(30, BATCH_SIZE);
                for (Order order : staleOrders) {
                    try {
                        cancelOrder(order);
                    } catch (Exception e) {
                        LogUtil.error("系统", null, "自动取消订单", "取消失败, orderId=" + order.getId() + ", error=" + e.getMessage());
                    }
                }
            } while (staleOrders.size() == BATCH_SIZE);
        } catch (Exception e) {
            LogUtil.error("系统", null, "自动取消订单", "扫描失败: " + e.getMessage());
        }
    }

    private void cancelOrder(Order order) throws Exception {
        List<OrderItem> items = orderItemDao.findByOrderId(order.getId());

        Connection conn = DButil.getConnection();
        try {
            conn.setAutoCommit(false);

            for (OrderItem item : items) {
                bookDao.restoreStock(item.getBookId(), item.getQuantity(), conn);
            }

            orderDao.updateStatus(order.getId(), 0, 4, conn);

            conn.commit();
            LogUtil.info("系统", null, "自动取消订单", "超时未支付自动取消, orderId=" + order.getId());
        } catch (Exception e) {
            DButil.rollback(conn);
            throw e;
        } finally {
            DButil.close(conn);
        }
    }
}
