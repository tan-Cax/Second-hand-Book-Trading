package com.example.shopping.dao;

import com.example.shopping.entity.User;
import com.example.shopping.utils.DButil;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
public class UserDao {

    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
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

    public User findByName(String name) throws SQLException {
        String sql = "SELECT * FROM user WHERE name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO user (name, password, role, created_time) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.getRole() != null ? user.getRole() : 0);
            pstmt.setTimestamp(4, user.getCreatedTime() != null
                    ? Timestamp.valueOf(user.getCreatedTime()) : null);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
            }
            return user.getId();
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int updatePassword(Integer userId, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    // 管理员查询用户列表（支持按用户名搜索）
    public java.util.List<User> findAll(String keyword, int page, int size) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM user");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" WHERE name LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY created_time DESC LIMIT ? OFFSET ?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<User> list = new java.util.ArrayList<>();
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.setInt(params.size() + 1, size);
            pstmt.setInt(params.size() + 2, Math.max(0, (page - 1) * size));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    public int countByKeyword(String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM user");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" WHERE name LIKE ?");
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(1, "%" + keyword.trim() + "%");
            }
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } finally {
            DButil.close(rs, pstmt, conn);
        }
    }

    // 更新用户封禁状态（带连接，用于事务）
    public int updateBanStatus(Integer userId, Integer banned) throws SQLException {
        String sql = "UPDATE user SET is_banned = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DButil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, banned);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt, conn);
        }
    }

    public int updateBanStatus(Integer userId, Integer banned, Connection conn) throws SQLException {
        String sql = "UPDATE user SET is_banned = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, banned);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } finally {
            DButil.close(pstmt);
        }
    }

    // 用户总数
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user";
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

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getInt("role"));
        user.setIsBanned(rs.getInt("is_banned"));
        user.setCreatedTime(rs.getTimestamp("created_time") != null
                ? rs.getTimestamp("created_time").toLocalDateTime() : null);
        return user;
    }
}
