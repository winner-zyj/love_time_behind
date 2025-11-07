package com.abc.love_time.dao;

import com.abc.love_time.entity.HeartWallProject;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HeartWallProjectDAO {
    
    public HeartWallProject insert(HeartWallProject project) throws SQLException {
        String sql = "INSERT INTO heart_wall_projects (user_id, project_name, description, max_photos, is_public) VALUES (?, ?, ?, ?, ?)";
        
        // 添加重试机制以处理死锁情况
        int maxRetries = 3;
        SQLException lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setLong(1, project.getUserId());
                stmt.setString(2, project.getProjectName());
                stmt.setString(3, project.getDescription());
                stmt.setInt(4, project.getMaxPhotos());
                stmt.setBoolean(5, project.isPublic());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("创建心形墙项目失败，没有行受到影响。");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setId(generatedKeys.getLong(1));
                        System.out.println("[HeartWallProjectDAO] 创建心形墙项目成功，ID: " + project.getId() + "，用户ID: " + project.getUserId());
                        return project; // 成功时直接返回
                    } else {
                        throw new SQLException("创建心形墙项目失败，没有获得ID。");
                    }
                }
            } catch (SQLException e) {
                lastException = e;
                // 如果是死锁错误，等待一段时间后重试
                if (e.getMessage().contains("Deadlock found when trying to get lock") && attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(100 * (attempt + 1)); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("重试过程中被中断", ie);
                    }
                    continue; // 重试
                }
                // 如果不是死锁错误或者已经是最后一次尝试，抛出异常
                throw e;
            }
        }
        
        // 如果所有重试都失败了，抛出最后一个异常
        throw lastException;
    }
    
    public HeartWallProject findById(Long id) throws SQLException {
        String sql = "SELECT * FROM heart_wall_projects WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
        }
        return null;
    }
    
    public List<HeartWallProject> findByUserId(Long userId) throws SQLException {
        List<HeartWallProject> projects = new ArrayList<>();
        String sql = "SELECT * FROM heart_wall_projects WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }
        }
        return projects;
    }
    
    /**
     * 获取用户及其情侣的所有项目
     * @param userId 用户ID
     * @param partnerId 情侣ID（可为空）
     * @return 项目列表
     * @throws SQLException
     */
    public List<HeartWallProject> findByUserIdOrPartnerId(Long userId, Long partnerId) throws SQLException {
        List<HeartWallProject> projects = new ArrayList<>();
        
        // 如果没有情侣，只查询用户自己的项目
        if (partnerId == null) {
            String sql = "SELECT * FROM heart_wall_projects WHERE user_id = ? ORDER BY created_at DESC";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        projects.add(mapResultSetToProject(rs));
                    }
                }
            }
            return projects;
        }
        
        // 查询用户自己和情侣的项目
        String sql = "SELECT * FROM heart_wall_projects WHERE user_id = ? OR user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            stmt.setLong(2, partnerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }
        }
        return projects;
    }
    
    public boolean update(HeartWallProject project) throws SQLException {
        String sql = "UPDATE heart_wall_projects SET project_name = ?, description = ?, is_public = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, project.getProjectName());
            stmt.setString(2, project.getDescription());
            stmt.setBoolean(3, project.isPublic());
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(5, project.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM heart_wall_projects WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 清空项目中的所有照片
     * @param projectId 项目ID
     * @return 删除成功返回true
     */
    public boolean clearAllPhotos(Long projectId) throws SQLException {
        String sql = "DELETE FROM heart_wall_photos WHERE project_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, projectId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("[HeartWallProjectDAO] 清空项目照片，影响行数: " + affectedRows);
            return true;
        }
    }
    
    private HeartWallProject mapResultSetToProject(ResultSet rs) throws SQLException {
        HeartWallProject project = new HeartWallProject();
        project.setId(rs.getLong("id"));
        project.setUserId(rs.getLong("user_id"));
        project.setProjectName(rs.getString("project_name"));
        project.setDescription(rs.getString("description"));
        project.setPhotoCount(rs.getInt("photo_count"));
        project.setMaxPhotos(rs.getInt("max_photos"));
        project.setCoverPhotoUrl(rs.getString("cover_photo_url"));
        project.setPublic(rs.getBoolean("is_public"));
        project.setCreatedAt(rs.getTimestamp("created_at"));
        project.setUpdatedAt(rs.getTimestamp("updated_at"));
        return project;
    }
}