package com.example.shopping.dao;

import com.example.shopping.entity.Order;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDao {

    public int insert(Integer userId, Integer walletId, java.math.BigDecimal totalAmount, Connection conn) throws SQLException {
        String sql = "INSERT INTO `order` (user_id, wallet_id, total_amount, status, order_time) VALUES (?, ?, ?, 0, NOW())";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, walletId);
            pstmt.setBigDecimal(3, totalAmount);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("创建订单失败");
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public int updateStatus(Integer orderId, Integer expectedStatus, Integer newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE `order` SET status = ? WHERE id = ? AND status = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newStatus);
            pstmt.setInt(2, orderId);
            pstmt.setInt(3, expectedStatus);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("订单状态转移失败: orderId=" + orderId
                        + ", expected=" + expectedStatus + ", target=" + newStatus);
            }
            return rows;
        } finally {
            DButil.close(pstmt);
        }
    }

    public Order findById(Integer orderId) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public Order findById(Integer orderId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public List<Order> findByUserId(Integer userId, int page, int size) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE user_id = ? ORDER BY order_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, size);
            pstmt.setInt(3, (page - 1) * size);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public List<Order> findByUserIdAndStatus(Integer userId, int status, int page, int size) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE user_id = ? AND status = ? ORDER BY order_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, status);
            pstmt.setInt(3, size);
            pstmt.setInt(4, (page - 1) * size);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 卖家查看自己的订单（通过 order_item JOIN book 找到卖家的订单）
    public List<Order> findBySellerId(Integer sellerId, int page, int size) throws SQLException {
        String sql = "SELECT DISTINCT o.* FROM `order` o " +
                     "JOIN order_item oi ON o.id = oi.order_id " +
                     "JOIN book b ON oi.book_id = b.id " +
                     "WHERE b.seller_id = ? " +
                     "ORDER BY o.order_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            pstmt.setInt(2, size);
            pstmt.setInt(3, (page - 1) * size);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setWalletId(rs.getInt("wallet_id"));
        order.setOrderTime(rs.getTimestamp("order_time") != null
                ? rs.getTimestamp("order_time").toLocalDateTime() : null);
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getInt("status"));
        return order;
    }

    public int countByUserId(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int countByUserIdAndStatus(Integer userId, int status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE user_id = ? AND status = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, status);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int countBySellerId(Integer sellerId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT o.id) FROM `order` o " +
                     "JOIN order_item oi ON o.id = oi.order_id " +
                     "JOIN book b ON oi.book_id = b.id " +
                     "WHERE b.seller_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 检查书籍是否存在未完成的订单（status: 0未支付, 1已支付待发货, 2已发货待收货）
    public boolean hasUnfinishedOrders(Integer bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_item oi " +
                     "JOIN `order` o ON oi.order_id = o.id " +
                     "WHERE oi.book_id = ? AND o.status IN (0, 1, 2)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public boolean hasAnyOrders(Integer bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_item WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 管理员查询全部订单（JOIN user 显示买家名）
    public List<Order> findAll(int page, int size) throws SQLException {
        String sql = "SELECT o.* FROM `order` o ORDER BY o.order_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, size);
            pstmt.setInt(2, (page - 1) * size);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 管理员按状态筛选订单
    public List<Order> findAllByStatus(Integer status, int page, int size) throws SQLException {
        String sql = "SELECT o.* FROM `order` o WHERE o.status = ? ORDER BY o.order_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, size);
            pstmt.setInt(3, (page - 1) * size);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 订单总数
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order`";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 按状态统计订单数
    public int countByStatus(Integer status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE status = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 总交易额（已支付的订单）
    public java.math.BigDecimal sumTotalAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM `order` WHERE status IN (1, 2, 3)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return java.math.BigDecimal.ZERO;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 按卖家ID查找未完成订单（用于封禁卖家时取消订单）
    public List<Order> findUnfinishedBySellerId(Integer sellerId, Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT o.* FROM `order` o " +
                     "JOIN order_item oi ON o.id = oi.order_id " +
                     "JOIN book b ON oi.book_id = b.id " +
                     "WHERE b.seller_id = ? AND o.status IN (0, 1, 2) " +
                     "ORDER BY o.order_time DESC";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public List<Order> findUnfinishedBySellerId(Integer sellerId) throws SQLException {
        String sql = "SELECT DISTINCT o.* FROM `order` o " +
                     "JOIN order_item oi ON o.id = oi.order_id " +
                     "JOIN book b ON oi.book_id = b.id " +
                     "WHERE b.seller_id = ? AND o.status IN (0, 1, 2) " +
                     "ORDER BY o.order_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public List<Order> findStaleUnpaidOrders(int minutes) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE status = 0 AND order_time < DATE_SUB(NOW(), INTERVAL ? MINUTE)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, minutes);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public List<Order> findStaleUnpaidOrders(int minutes, int limit) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE status = 0 AND order_time < DATE_SUB(NOW(), INTERVAL ? MINUTE) LIMIT ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Order> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, minutes);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }
}
