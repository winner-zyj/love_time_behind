package com.abc.love_time.dao;

import com.abc.love_time.entity.HeartWallPhoto;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HeartWallPhotoDAO {
    
    public HeartWallPhoto insert(HeartWallPhoto photo) throws SQLException {
        String sql = "INSERT INTO heart_wall_photos (project_id, user_id, photo_url, thumbnail_url, position_index, caption, taken_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        // 添加重试机制以处理死锁情况
        int maxRetries = 3;
        SQLException lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setLong(1, photo.getProjectId());
                stmt.setLong(2, photo.getUserId());
                stmt.setString(3, photo.getPhotoUrl());
                stmt.setString(4, photo.getThumbnailUrl());
                stmt.setInt(5, photo.getPositionIndex());
                stmt.setString(6, photo.getCaption());
                stmt.setDate(7, photo.getTakenDate());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("创建心形墙照片失败，没有行受到影响。");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        photo.setId(generatedKeys.getLong(1));
                        return photo; // 成功时直接返回
                    } else {
                        throw new SQLException("创建心形墙照片失败，没有获得ID。");
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
    
    public HeartWallPhoto findById(Long id) throws SQLException {
        String sql = "SELECT * FROM heart_wall_photos WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPhoto(rs);
                }
            }
        }
        return null;
    }
    
    public List<HeartWallPhoto> findByProjectId(Long projectId) throws SQLException {
        List<HeartWallPhoto> photos = new ArrayList<>();
        String sql = "SELECT * FROM heart_wall_photos WHERE project_id = ? ORDER BY position_index";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    photos.add(mapResultSetToPhoto(rs));
                }
            }
        }
        return photos;
    }
    
    public List<HeartWallPhoto> findByProjectIdWithLimit(Long projectId, int offset, int limit) throws SQLException {
        List<HeartWallPhoto> photos = new ArrayList<>();
        String sql = "SELECT * FROM heart_wall_photos WHERE project_id = ? ORDER BY position_index LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, projectId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    photos.add(mapResultSetToPhoto(rs));
                }
            }
        }
        return photos;
    }
    
    public boolean update(HeartWallPhoto photo) throws SQLException {
        String sql = "UPDATE heart_wall_photos SET photo_url = ?, thumbnail_url = ?, position_index = ?, caption = ?, taken_date = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, photo.getPhotoUrl());
            stmt.setString(2, photo.getThumbnailUrl());
            stmt.setInt(3, photo.getPositionIndex());
            stmt.setString(4, photo.getCaption());
            stmt.setDate(5, photo.getTakenDate());
            stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            stmt.setLong(7, photo.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM heart_wall_photos WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteByProjectIdAndPosition(Long projectId, int positionIndex) throws SQLException {
        String sql = "DELETE FROM heart_wall_photos WHERE project_id = ? AND position_index = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, projectId);
            stmt.setInt(2, positionIndex);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public int getPhotoCountByProjectId(Long projectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM heart_wall_photos WHERE project_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    public int getNextAvailablePosition(Long projectId) throws SQLException {
        // 先尝试直接查询数据库，而不是依赖存储函数
        String sql = "SELECT position_index FROM heart_wall_photos WHERE project_id = ? ORDER BY position_index";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                // 创建一个布尔数组来跟踪哪些位置已被使用
                boolean[] positions = new boolean[41]; // 位置索引从1到40
                
                while (rs.next()) {
                    int position = rs.getInt("position_index");
                    if (position >= 1 && position <= 40) {
                        positions[position] = true;
                    }
                }
                
                // 查找第一个未使用的位置
                for (int i = 1; i <= 40; i++) {
                    if (!positions[i]) {
                        return i;
                    }
                }
                
                // 如果所有位置都被占用，返回-1
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("获取下一个可用位置时出错: " + e.getMessage());
            throw e;
        }
    }
    
    private HeartWallPhoto mapResultSetToPhoto(ResultSet rs) throws SQLException {
        HeartWallPhoto photo = new HeartWallPhoto();
        photo.setId(rs.getLong("id"));
        photo.setProjectId(rs.getLong("project_id"));
        photo.setUserId(rs.getLong("user_id"));
        photo.setPhotoUrl(rs.getString("photo_url"));
        photo.setThumbnailUrl(rs.getString("thumbnail_url"));
        photo.setPositionIndex(rs.getInt("position_index"));
        photo.setCaption(rs.getString("caption"));
        photo.setTakenDate(rs.getDate("taken_date"));
        photo.setUploadedAt(rs.getTimestamp("uploaded_at"));
        photo.setUpdatedAt(rs.getTimestamp("updated_at"));
        return photo;
    }
}