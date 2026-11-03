package com.example.shopping.dao;

import com.example.shopping.entity.BookImage;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookImageDao {

    public int insert(Integer bookId, String imageUrl, Integer sortOrder, Connection conn) throws SQLException {
        String sql = "INSERT INTO book_image (book_id, image_url, sort_order) VALUES (?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            pstmt.setString(2, imageUrl);
            pstmt.setInt(3, sortOrder);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    public List<BookImage> findByBookId(Integer bookId) throws SQLException {
        String sql = "SELECT * FROM book_image WHERE book_id = ? ORDER BY sort_order";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BookImage> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookImage img = new BookImage();
                img.setId(rs.getInt("id"));
                img.setBookId(rs.getInt("book_id"));
                img.setImageUrl(rs.getString("image_url"));
                img.setSortOrder(rs.getInt("sort_order"));
                list.add(img);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int deleteByBookId(Integer bookId, Connection conn) throws SQLException {
        String sql = "DELETE FROM book_image WHERE book_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }
}
