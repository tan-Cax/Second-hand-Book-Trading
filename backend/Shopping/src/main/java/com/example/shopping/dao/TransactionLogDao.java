package com.example.shopping.dao;

import com.example.shopping.entity.TransactionLog;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionLogDao {

    public int insert(TransactionLog log, Connection conn) throws SQLException {
        String sql = "INSERT INTO transaction_log (wallet_id, amount, type, order_id, description) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, log.getWalletId());
            pstmt.setBigDecimal(2, log.getAmount());
            pstmt.setInt(3, log.getType());
            if (log.getOrderId() != null) {
                pstmt.setInt(4, log.getOrderId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setString(5, log.getDescription());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                log.setId(rs.getInt(1));
            }
            return log.getId();
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public List<TransactionLog> findByWalletId(Integer walletId, int page, int size) throws SQLException {
        String sql = "SELECT * FROM transaction_log WHERE wallet_id = ? " +
                     "ORDER BY created_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TransactionLog> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
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

    public List<TransactionLog> findByWalletId(Integer walletId) throws SQLException {
        String sql = "SELECT * FROM transaction_log WHERE wallet_id = ? ORDER BY created_time DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TransactionLog> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    private TransactionLog mapRow(ResultSet rs) throws SQLException {
        TransactionLog log = new TransactionLog();
        log.setId(rs.getInt("id"));
        log.setWalletId(rs.getInt("wallet_id"));
        log.setAmount(rs.getBigDecimal("amount"));
        log.setType(rs.getInt("type"));
        log.setOrderId(rs.getObject("order_id") != null ? rs.getInt("order_id") : null);
        log.setDescription(rs.getString("description"));
        log.setCreatedTime(rs.getTimestamp("created_time") != null
                ? rs.getTimestamp("created_time").toLocalDateTime() : null);
        return log;
    }

    // 管理员查询全部交易流水（JOIN wallet+user 显示用户名）
    public java.util.List<TransactionLog> findAll(int page, int size) throws SQLException {
        String sql = "SELECT tl.* FROM transaction_log tl " +
                     "JOIN wallet w ON tl.wallet_id = w.id " +
                     "JOIN user u ON w.user_id = u.id " +
                     "ORDER BY tl.created_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<TransactionLog> list = new java.util.ArrayList<>();
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

    // 按类型筛选交易流水
    public java.util.List<TransactionLog> findByType(Integer type, int page, int size) throws SQLException {
        String sql = "SELECT * FROM transaction_log WHERE type = ? ORDER BY created_time DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<TransactionLog> list = new java.util.ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, type);
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

    // 按类型汇总金额
    public java.math.BigDecimal sumAmountByType(Integer type) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transaction_log WHERE type = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, type);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return java.math.BigDecimal.ZERO;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 交易流水总数
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transaction_log";
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

    public int countByWalletId(Integer walletId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transaction_log WHERE wallet_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }
}
