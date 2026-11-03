package com.example.shopping.dao;

import com.example.shopping.entity.Wallet;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WalletDao {

    public int insert(Wallet wallet) throws SQLException {
        String sql = "INSERT INTO wallet (user_id, pay_password, balance, status) VALUES (?, ?, 0.00, 1)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, wallet.getUserId());
            pstmt.setString(2, wallet.getPayPassword());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                wallet.setId(rs.getInt(1));
            }
            return wallet.getId();
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public Wallet findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM wallet WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public Wallet findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM wallet WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public Wallet findByUserIdForUpdate(Integer userId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM wallet WHERE user_id = ? FOR UPDATE";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public String getPayPassword(Integer walletId) throws SQLException {
        String sql = "SELECT pay_password FROM wallet WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("pay_password");
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int updatePayPassword(Integer walletId, String newPayPassword) throws SQLException {
        String sql = "UPDATE wallet SET pay_password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPayPassword);
            pstmt.setInt(2, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    public BigDecimal getBalanceForUpdate(Integer walletId, Connection conn) throws SQLException {
        String sql = "SELECT balance FROM wallet WHERE id = ? FOR UPDATE";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("balance");
            }
            throw new SQLException("钱包不存在: walletId=" + walletId);
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public int deductBalance(Integer walletId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE wallet SET balance = balance - ? WHERE id = ? AND balance >= ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setBigDecimal(1, amount);
            pstmt.setInt(2, walletId);
            pstmt.setBigDecimal(3, amount);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("余额不足或钱包不存在: walletId=" + walletId);
            }
            return rows;
        } finally {
            DButil.close(pstmt);
        }
    }

    public int addBalance(Integer walletId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE wallet SET balance = balance + ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setBigDecimal(1, amount);
            pstmt.setInt(2, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    public int incrementPayFailCount(Integer walletId) throws SQLException {
        String sql = "UPDATE wallet SET pay_fail_count = pay_fail_count + 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    public int incrementPayFailCountAndCheck(Integer walletId, int maxFailCount, Connection conn) throws SQLException {
        String sql = "UPDATE wallet SET pay_fail_count = pay_fail_count + 1 WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }

        String selectSql = "SELECT pay_fail_count FROM wallet WHERE id = ?";
        pstmt = conn.prepareStatement(selectSql);
        ResultSet rs = null;
        try {
            pstmt.setInt(1, walletId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("pay_fail_count");
                if (count >= maxFailCount) {
                    lockPayAccount(walletId, conn);
                }
                return count;
            }
            return 0;
        } finally {
            DButil.close(rs, pstmt);
        }
    }

    public int resetPayFailCount(Integer walletId) throws SQLException {
        String sql = "UPDATE wallet SET pay_fail_count = 0 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    public int resetPayFailCount(Integer walletId, Connection conn) throws SQLException {
        String sql = "UPDATE wallet SET pay_fail_count = 0 WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    public int lockPayAccount(Integer walletId) throws SQLException {
        String sql = "UPDATE wallet SET pay_locked = 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    public int lockPayAccount(Integer walletId, Connection conn) throws SQLException {
        String sql = "UPDATE wallet SET pay_locked = 1 WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    public int unlockPayAccount(Integer walletId) throws SQLException {
        String sql = "UPDATE wallet SET pay_fail_count = 0, pay_locked = 0 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, walletId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 封禁卖家时批量取消订单需要的方法：按卖家ID查找未完成订单中的书籍（用于恢复库存）
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM wallet";
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

    private Wallet mapRow(ResultSet rs) throws SQLException {
        Wallet wallet = new Wallet();
        wallet.setId(rs.getInt("id"));
        wallet.setUserId(rs.getInt("user_id"));
        wallet.setPayPassword(rs.getString("pay_password"));
        wallet.setBalance(rs.getBigDecimal("balance"));
        wallet.setStatus(rs.getInt("status"));
        wallet.setPayFailCount(rs.getInt("pay_fail_count"));
        wallet.setPayLocked(rs.getInt("pay_locked"));
        wallet.setCreatedTime(rs.getTimestamp("created_time") != null
                ? rs.getTimestamp("created_time").toLocalDateTime() : null);
        return wallet;
    }
}
