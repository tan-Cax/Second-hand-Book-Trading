package com.example.shopping.dao;

import com.example.shopping.entity.OrderItem;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderItemDao {

    public int batchInsert(Integer orderId, List<OrderItem> items, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "INSERT INTO order_item (order_id, book_id, quantity, price, amount) VALUES "
        );
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("(?,?,?,?,?)");
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                int base = i * 5;
                pstmt.setInt(base + 1, orderId);
                pstmt.setInt(base + 2, item.getBookId());
                pstmt.setInt(base + 3, item.getQuantity());
                pstmt.setBigDecimal(base + 4, item.getPrice());
                pstmt.setBigDecimal(base + 5, item.getAmount());
            }
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    public List<OrderItem> findByOrderId(Integer orderId) throws SQLException {
        String sql = "SELECT oi.*, b.title AS book_title FROM order_item oi " +
                     "JOIN book b ON oi.book_id = b.id " +
                     "WHERE oi.order_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderItem> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setAmount(rs.getBigDecimal("amount"));
                list.add(item);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public List<OrderItem> findByOrderId(Integer orderId, Connection conn) throws SQLException {
        String sql = "SELECT oi.*, b.title AS book_title FROM order_item oi " +
                     "JOIN book b ON oi.book_id = b.id " +
                     "WHERE oi.order_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderItem> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setBookId(rs.getInt("book_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setAmount(rs.getBigDecimal("amount"));
                list.add(item);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt);
        }
    }
}
