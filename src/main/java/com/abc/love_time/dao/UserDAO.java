package com.abc.love_time.dao;

import com.abc.love_time.entity.User;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问类
 */
public class UserDAO {

    /**
     * 根据code查询用户
     * @param code 用户code（邀请码）
     * @return User对象，如果不存在返回null
     */
    public User findByCode(String code) {
        String sql = "SELECT * FROM users WHERE code = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] 查询用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据openid查询用户
     * @param openid 用户openid
     * @return User对象，如果不存在返回null
     */
    public User findByOpenId(String openid) {
        String sql = "SELECT * FROM users WHERE openid = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, openid);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] 根据openid查询用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 插入成功返回自增ID，失败返回-1
     */
    public long insert(User user) {
        String sql = "INSERT INTO users (openid, code, nickName, avatarUrl, created_at) VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // 确保 openid 不为空
            if (user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
                System.err.println("[UserDAO] openid 不能为空");
                return -1;
            }
            
            pstmt.setString(1, user.getOpenid());
            pstmt.setString(2, user.getCode());
            pstmt.setString(3, user.getNickName());
            pstmt.setString(4, user.getAvatarUrl());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[UserDAO] 用户插入成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] 插入用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 更新成功返回true
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET nickName = ?, avatarUrl = ?, code = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getNickName());
            pstmt.setString(2, user.getAvatarUrl());
            pstmt.setString(3, user.getCode());
            pstmt.setLong(4, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[UserDAO] 更新用户，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] 更新用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 删除成功返回true
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[UserDAO] 删除用户，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] 删除用户失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return User对象，如果不存在返回null
     */
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] 根据ID查询用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 查询所有用户
     * @return 用户列表
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
            System.out.println("[UserDAO] 查询到 " + users.size() + " 个用户");
        } catch (SQLException e) {
            System.err.println("[UserDAO] 查询所有用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }

    /**
     * 将ResultSet映射为User对象
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setOpenid(rs.getString("openid"));
        user.setCode(rs.getString("code"));
        user.setNickName(rs.getString("nickName"));
        user.setAvatarUrl(rs.getString("avatarUrl"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
