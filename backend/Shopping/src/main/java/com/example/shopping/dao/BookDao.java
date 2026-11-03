package com.example.shopping.dao;

import com.example.shopping.dto.BookListDTO;
import com.example.shopping.dto.SellerBookDTO;
import com.example.shopping.dto.BookSearchParam;
import com.example.shopping.entity.Book;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class BookDao {

    // 查询所有书籍，返回id, 图片, 价格, 标题, locate（排除封禁卖家的书籍）
    public List<BookListDTO> findAllBooks() throws SQLException {
        String sql = "SELECT b.id, b.price, b.title, b.locate, bi.image_url AS imageUrl " +
                     "FROM book b " +
                     "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
                     "JOIN user u ON b.seller_id = u.id " +
                     "WHERE b.is_deleted = 0 AND u.is_banned = 0 " +
                     "ORDER BY b.created_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BookListDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookListDTO dto = new BookListDTO();
                dto.setId(rs.getInt("id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 根据 ID 查询书籍（所有内容）
    public Book findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM book WHERE id = ? AND is_deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setSellerId(rs.getInt("seller_id"));
                book.setTitle(rs.getString("title"));
                book.setPublisher(rs.getString("publisher"));
                book.setEdition(rs.getString("edition"));
                book.setAuthor(rs.getString("author"));
                book.setDescription(rs.getString("description"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                book.setLocate(rs.getString("locate"));
                book.setIsDeleted(rs.getInt("is_deleted"));
                book.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
                return book;
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 插入书籍
    public int insert(Book book) throws SQLException {
        String sql = "INSERT INTO book (seller_id, title, description, edition, author, publisher, price, quantity, locate, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, book.getSellerId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getDescription() != null ? book.getDescription() : "");
            pstmt.setString(4, book.getEdition() != null ? book.getEdition() : "");
            pstmt.setString(5, book.getAuthor() != null ? book.getAuthor() : "");
            pstmt.setString(6, book.getPublisher() != null ? book.getPublisher() : "");
            pstmt.setBigDecimal(7, book.getPrice());
            pstmt.setInt(8, book.getQuantity());
            pstmt.setString(9, book.getLocate());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                book.setId(rs.getInt(1));
            }
            return book.getId();
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 事务内插入书籍，返回自增 bookId
    public int insertWithConn(Book book, Connection conn) throws SQLException {
        String sql = "INSERT INTO book (seller_id, title, description, edition, author, publisher, price, quantity, locate, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, book.getSellerId());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getDescription() != null ? book.getDescription() : "");
            pstmt.setString(4, book.getEdition() != null ? book.getEdition() : "");
            pstmt.setString(5, book.getAuthor() != null ? book.getAuthor() : "");
            pstmt.setString(6, book.getPublisher() != null ? book.getPublisher() : "");
            pstmt.setBigDecimal(7, book.getPrice());
            pstmt.setInt(8, book.getQuantity());
            pstmt.setString(9, book.getLocate());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("插入书籍失败");
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    // 根据关键词模糊搜索书籍（排除封禁卖家）
    public List<BookListDTO> searchByKeyword(String keyword) throws SQLException {
        String sql = "SELECT b.id, b.price, b.title, b.locate, bi.image_url AS imageUrl " +
                     "FROM book b " +
                     "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
                     "JOIN user u ON b.seller_id = u.id " +
                     "WHERE b.is_deleted = 0 AND u.is_banned = 0 " +
                     "AND (b.title LIKE ? OR b.publisher LIKE ? OR b.edition LIKE ? OR b.author LIKE ?) " +
                     "ORDER BY b.created_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BookListDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            String like = "%" + keyword + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            pstmt.setString(3, like);
            pstmt.setString(4, like);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookListDTO dto = new BookListDTO();
                dto.setId(rs.getInt("id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 根据地点精确查找书籍（排除封禁卖家）
    public List<BookListDTO> findByLocate(String locate) throws SQLException {
        String sql = "SELECT b.id, b.price, b.title, b.locate, bi.image_url AS imageUrl " +
                     "FROM book b " +
                     "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
                     "JOIN user u ON b.seller_id = u.id " +
                     "WHERE b.is_deleted = 0 AND u.is_banned = 0 AND b.locate LIKE ? " +
                     "ORDER BY b.created_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BookListDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + locate + "%");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookListDTO dto = new BookListDTO();
                dto.setId(rs.getInt("id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 多条件组合搜索（排除封禁卖家）
    public List<BookListDTO> searchBooks(BookSearchParam param) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.price, b.title, b.locate, bi.image_url AS imageUrl " +
            "FROM book b " +
            "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
            "JOIN user u ON b.seller_id = u.id " +
            "WHERE b.is_deleted = 0 AND u.is_banned = 0"
        );
        List<Object> params = new ArrayList<>();

        if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
            sql.append(" AND (b.title LIKE ? OR b.publisher LIKE ? OR b.edition LIKE ? OR b.author LIKE ?)");
            String like = "%" + param.getKeyword() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (param.getLocate() != null && !param.getLocate().trim().isEmpty()) {
            sql.append(" AND b.locate LIKE ?");
            params.add("%" + param.getLocate() + "%");
        }
        if (param.getMinPrice() != null) {
            sql.append(" AND b.price >= ?");
            params.add(param.getMinPrice());
        }
        if (param.getMaxPrice() != null) {
            sql.append(" AND b.price <= ?");
            params.add(param.getMaxPrice());
        }
        if (param.getSortDirection() != null && !param.getSortDirection().isEmpty()) {
            String dir = param.getSortDirection().toUpperCase();
            if ("ASC".equals(dir) || "DESC".equals(dir)) {
                sql.append(" ORDER BY b.price ").append(dir);
            } else {
                sql.append(" ORDER BY b.created_time DESC");
            }
        } else {
            sql.append(" ORDER BY b.created_time DESC");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BookListDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookListDTO dto = new BookListDTO();
                dto.setId(rs.getInt("id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 查询所有有效书籍的 ID（排除封禁卖家）
    public List<Integer> findAllBookIds() throws SQLException {
        String sql = "SELECT b.id FROM book b JOIN user u ON b.seller_id = u.id WHERE b.is_deleted = 0 AND u.is_banned = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            return ids;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 随机查询 n 本书籍
    public List<BookListDTO> findRandomBooks(int n) throws SQLException {
        List<Integer> allIds = findAllBookIds();
        if (allIds.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.shuffle(allIds);
        int count = Math.min(n, allIds.size());
        List<Integer> selectedIds = allIds.subList(0, count);

        StringBuilder sql = new StringBuilder(
            "SELECT b.id, b.price, b.title, b.locate, bi.image_url AS imageUrl " +
            "FROM book b " +
            "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
            "WHERE b.id IN ("
        );
        for (int i = 0; i < selectedIds.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(") ORDER BY b.created_time DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BookListDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < selectedIds.size(); i++) {
                pstmt.setInt(i + 1, selectedIds.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BookListDTO dto = new BookListDTO();
                dto.setId(rs.getInt("id"));
                dto.setTitle(rs.getString("title"));
                dto.setPrice(rs.getBigDecimal("price"));
                dto.setLocate(rs.getString("locate"));
                dto.setImageUrl(rs.getString("imageUrl"));
                list.add(dto);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 逻辑删除书籍（标记 is_deleted = 1）
    public int deleteById(Integer id) throws SQLException {
        String sql = "UPDATE book SET is_deleted = 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 按卖家ID批量下架书籍（带连接，用于事务）
    public int delistBySellerId(Integer sellerId) throws SQLException {
        String sql = "UPDATE book SET is_deleted = 1 WHERE seller_id = ? AND is_deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    public int delistBySellerId(Integer sellerId, Connection conn) throws SQLException {
        String sql = "UPDATE book SET is_deleted = 1 WHERE seller_id = ? AND is_deleted = 0";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    // 查询所有逻辑删除的书籍
    public List<Book> findDeletedBooks() throws SQLException {
        String sql = "SELECT * FROM book WHERE is_deleted = 1 ORDER BY created_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Book> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setSellerId(rs.getInt("seller_id"));
                book.setTitle(rs.getString("title"));
                book.setPublisher(rs.getString("publisher"));
                book.setEdition(rs.getString("edition"));
                book.setAuthor(rs.getString("author"));
                book.setDescription(rs.getString("description"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                book.setLocate(rs.getString("locate"));
                book.setIsDeleted(rs.getInt("is_deleted"));
                book.setCreatedTime(rs.getTimestamp("created_time") != null
                        ? rs.getTimestamp("created_time").toLocalDateTime() : null);
                list.add(book);
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 物理删除书籍
    public int permanentDelete(Integer bookId) throws SQLException {
        String sql = "DELETE FROM book WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 按ID查询书籍（包含逻辑删除的）
    public Book findByIdIncludingDeleted(Integer id) throws SQLException {
        String sql = "SELECT * FROM book WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setSellerId(rs.getInt("seller_id"));
                book.setTitle(rs.getString("title"));
                book.setPublisher(rs.getString("publisher"));
                book.setEdition(rs.getString("edition"));
                book.setAuthor(rs.getString("author"));
                book.setDescription(rs.getString("description"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                book.setLocate(rs.getString("locate"));
                book.setIsDeleted(rs.getInt("is_deleted"));
                book.setCreatedTime(rs.getTimestamp("created_time") != null
                        ? rs.getTimestamp("created_time").toLocalDateTime() : null);
                return book;
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 悲观锁查书（下单时校验库存用）
    public Book findByIdForUpdate(Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM book WHERE id = ? AND is_deleted = 0 FOR UPDATE";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setSellerId(rs.getInt("seller_id"));
                book.setTitle(rs.getString("title"));
                book.setPublisher(rs.getString("publisher"));
                book.setEdition(rs.getString("edition"));
                book.setAuthor(rs.getString("author"));
                book.setDescription(rs.getString("description"));
                book.setPrice(rs.getBigDecimal("price"));
                book.setQuantity(rs.getInt("quantity"));
                book.setLocate(rs.getString("locate"));
                book.setIsDeleted(rs.getInt("is_deleted"));
                book.setCreatedTime(rs.getTimestamp("created_time") != null
                        ? rs.getTimestamp("created_time").toLocalDateTime() : null);
                return book;
            }
            return null;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    // 扣减库存（WHERE quantity >= ? 防超卖）
    public int deductStock(Integer bookId, Integer quantity, Connection conn) throws SQLException {
        String sql = "UPDATE book SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, bookId);
            pstmt.setInt(3, quantity);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("库存不足或书籍不存在: bookId=" + bookId);
            }
            return rows;
        } finally {
            DButil.close(pstmt);
        }
    }

    // 恢复库存（取消订单时调用）
    public int restoreStock(Integer bookId, Integer quantity, Connection conn) throws SQLException {
        String sql = "UPDATE book SET quantity = quantity + ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, bookId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    // 事务内修改书籍信息
    public int updateBook(Book book, Connection conn) throws SQLException {
        String sql = "UPDATE book SET title = ?, description = ?, edition = ?, author = ?, " +
                     "publisher = ?, price = ?, quantity = ?, locate = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getDescription() != null ? book.getDescription() : "");
            pstmt.setString(3, book.getEdition() != null ? book.getEdition() : "");
            pstmt.setString(4, book.getAuthor() != null ? book.getAuthor() : "");
            pstmt.setString(5, book.getPublisher() != null ? book.getPublisher() : "");
            pstmt.setBigDecimal(6, book.getPrice());
            pstmt.setInt(7, book.getQuantity());
            pstmt.setString(8, book.getLocate());
            pstmt.setInt(9, book.getId());
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    // 按卖家查询已发布书籍（含首图）
    public List<SellerBookDTO> findBySellerId(Integer sellerId) throws SQLException {
        String sql = "SELECT b.id, b.price, b.title, b.locate, bi.image_url AS imageUrl, b.quantity " +
                     "FROM book b " +
                     "LEFT JOIN book_image bi ON b.id = bi.book_id AND bi.sort_order = 0 " +
                     "JOIN user u ON b.seller_id = u.id " +
                     "WHERE b.seller_id = ? AND b.is_deleted = 0 AND u.is_banned = 0 " +
                     "ORDER BY b.created_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SellerBookDTO> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sellerId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                SellerBookDTO dto = new SellerBookDTO();
                dto.setId(rs.getInt("id"));
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
}
