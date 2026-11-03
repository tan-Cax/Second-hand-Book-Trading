package com.example.shopping.dao;

import com.example.shopping.dto.CartItemDTO;
import com.example.shopping.entity.Cart;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CartDao {

    // 添加商品到购物车（已有则累加数量）
    public int addToCart(Integer userId, Integer bookId, Integer quantity) throws SQLException {
        String sql = "INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, quantity);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 删除购物车中的某一项
    public int removeItem(Integer userId, Integer bookId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 修改购物车中某本书的数量
    public int updateQuantity(Integer userId, Integer bookId, Integer quantity) throws SQLException {
        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, bookId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 查看购物车（含书名、价格、图片，排除封禁卖家的商品）
    public List<CartItemDTO> findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT c.id, c.book_id, b.title, b.price, b.locate, bi.image_url AS imageUrl, c.quantity " +
                     "FROM cart c " +
                     "JOIN book b ON c.book_id = b.id " +
                     "JOIN user u ON b.seller_id = u.id " +
                     "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
                     "WHERE c.user_id = ? AND b.is_deleted = 0 AND u.is_banned = 0 " +
                     "ORDER BY c.added_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CartItemDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CartItemDTO dto = new CartItemDTO();
                dto.setId(rs.getInt("id"));
                dto.setBookId(rs.getInt("book_id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                dto.setQuantity(rs.getInt("quantity"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public List<CartItemDTO> findByUserId(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT c.id, c.book_id, b.title, b.price, b.locate, bi.image_url AS imageUrl, c.quantity " +
                     "FROM cart c " +
                     "JOIN book b ON c.book_id = b.id " +
                     "JOIN user u ON b.seller_id = u.id " +
                     "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
                     "WHERE c.user_id = ? AND b.is_deleted = 0 AND u.is_banned = 0 " +
                     "ORDER BY c.added_time DESC";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CartItemDTO> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CartItemDTO dto = new CartItemDTO();
                dto.setId(rs.getInt("id"));
                dto.setBookId(rs.getInt("book_id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                dto.setQuantity(rs.getInt("quantity"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    // 清空购物车
    public int clearCart(Integer userId) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 事务内清空购物车（下单时调用）
    public int clearCart(Integer userId, Connection conn) throws SQLException {
        String sql = "DELETE FROM cart WHERE user_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    // 购物车商品种类数
    public int countItems(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cart WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 判断某书是否已在购物车（且卖家未被封禁）
    public boolean existsItem(Integer userId, Integer bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cart c JOIN book b ON c.book_id = b.id JOIN user u ON b.seller_id = u.id " +
                     "WHERE c.user_id = ? AND c.book_id = ? AND u.is_banned = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 查询购物车中某本书的当前数量
    public int getCartQuantity(Integer userId, Integer bookId) throws SQLException {
        String sql = "SELECT quantity FROM cart WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }
}
