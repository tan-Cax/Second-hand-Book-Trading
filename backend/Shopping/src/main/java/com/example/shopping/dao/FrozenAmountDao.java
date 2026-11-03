package com.example.shopping.dao;

import com.example.shopping.entity.FrozenAmount;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FrozenAmountDao {

    public int insert(FrozenAmount fa) throws SQLException {
        String sql = "INSERT INTO frozen_amount (order_id, payer_wallet_id, receiver_wallet_id, amount, status) " +
                     "VALUES (?, ?, ?, ?, 1)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, fa.getOrderId());
            pstmt.setInt(2, fa.getPayerWalletId());
            pstmt.setInt(3, fa.getReceiverWalletId());
            pstmt.setBigDecimal(4, fa.getAmount());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                fa.setId(rs.getInt(1));
            }
            return fa.getId();
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int insertWithConn(FrozenAmount fa, Connection conn) throws SQLException {
        String sql = "INSERT INTO frozen_amount (order_id, payer_wallet_id, receiver_wallet_id, amount, status) " +
                     "VALUES (?, ?, ?, ?, 1)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, fa.getOrderId());
            pstmt.setInt(2, fa.getPayerWalletId());
            pstmt.setInt(3, fa.getReceiverWalletId());
            pstmt.setBigDecimal(4, fa.getAmount());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                fa.setId(rs.getInt(1));
            }
            return fa.getId();
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public List<FrozenAmount> findByOrderId(Integer orderId) throws SQLException {
        String sql = "SELECT * FROM frozen_amount WHERE order_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FrozenAmount> list = new ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public List<FrozenAmount> findByOrderId(Integer orderId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM frozen_amount WHERE order_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FrozenAmount> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public BigDecimal getFrozenTotalByWallet(Integer walletId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM frozen_amount WHERE payer_wallet_id = ? AND status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int updateStatus(Integer id, Integer expectedStatus, Integer newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE frozen_amount SET status = ?, updated_at = NOW() WHERE id = ? AND status = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newStatus);
            pstmt.setInt(2, id);
            pstmt.setInt(3, expectedStatus);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("冻结状态转移失败: id=" + id + ", expected=" + expectedStatus);
            }
            return rows;
        } finally {
            DButil.close(pstmt);
        }
    }

    public List<FrozenAmount> findByPayerWallet(Integer walletId, int page, int size) throws SQLException {
        String sql = "SELECT * FROM frozen_amount WHERE payer_wallet_id = ? " +
                     "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FrozenAmount> list = new ArrayList<>();
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

    public List<FrozenAmount> findByReceiverWallet(Integer walletId, int page, int size) throws SQLException {
        String sql = "SELECT * FROM frozen_amount WHERE receiver_wallet_id = ? " +
                     "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FrozenAmount> list = new ArrayList<>();
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

    private FrozenAmount mapRow(ResultSet rs) throws SQLException {
        FrozenAmount fa = new FrozenAmount();
        fa.setId(rs.getInt("id"));
        fa.setOrderId(rs.getInt("order_id"));
        fa.setPayerWalletId(rs.getInt("payer_wallet_id"));
        fa.setReceiverWalletId(rs.getInt("receiver_wallet_id"));
        fa.setAmount(rs.getBigDecimal("amount"));
        fa.setStatus(rs.getInt("status"));
        fa.setCreatedAt(rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        fa.setUpdatedAt(rs.getTimestamp("updated_at") != null
                ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return fa;
    }

    public int countByPayerWallet(Integer walletId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM frozen_amount WHERE payer_wallet_id = ?";
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

    public int countByReceiverWallet(Integer walletId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM frozen_amount WHERE receiver_wallet_id = ?";
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
