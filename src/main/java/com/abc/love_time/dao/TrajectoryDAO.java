package com.abc.love_time.dao;

import com.abc.love_time.entity.Trajectory;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 轨迹点数据访问对象
 */
public class TrajectoryDAO {
    
    /**
     * 插入新的轨迹点
     * @param trajectory 轨迹点对象
     * @return 插入成功返回轨迹点ID，失败返回-1
     */
    public long insert(Trajectory trajectory) {
        String sql = "INSERT INTO trajectories (user_id, latitude, longitude, address, place_name, description, photo_url, is_shared) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, trajectory.getUserId());
            pstmt.setDouble(2, trajectory.getLatitude());
            pstmt.setDouble(3, trajectory.getLongitude());
            pstmt.setString(4, trajectory.getAddress());
            pstmt.setString(5, trajectory.getPlaceName());
            pstmt.setString(6, trajectory.getDescription());
            pstmt.setString(7, trajectory.getPhotoUrl());
            pstmt.setBoolean(8, trajectory.getIsShared() != null ? trajectory.getIsShared() : false);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[TrajectoryDAO] 插入轨迹点成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 插入轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * 根据用户ID查询轨迹点列表
     * @param userId 用户ID
     * @param daysBack 查询天数
     * @return 轨迹点列表
     */
    public List<Trajectory> findByUserId(Long userId, int daysBack) {
        List<Trajectory> trajectories = new ArrayList<>();
        String sql = "SELECT * FROM trajectories WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setInt(2, daysBack);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trajectories.add(mapResultSetToTrajectory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 查询轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TrajectoryDAO] 查询到 " + trajectories.size() + " 个轨迹点");
        return trajectories;
    }
    
    /**
     * 根据ID查询轨迹点
     * @param id 轨迹点ID
     * @return 轨迹点对象，如果不存在返回null
     */
    public Trajectory findById(Long id) {
        String sql = "SELECT * FROM trajectories WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrajectory(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 根据ID查询轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 更新轨迹点
     * @param trajectory 轨迹点对象
     * @return 更新成功返回true
     */
    public boolean update(Trajectory trajectory) {
        String sql = "UPDATE trajectories SET latitude = ?, longitude = ?, address = ?, place_name = ?, description = ?, photo_url = ?, is_shared = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, trajectory.getLatitude());
            pstmt.setDouble(2, trajectory.getLongitude());
            pstmt.setString(3, trajectory.getAddress());
            pstmt.setString(4, trajectory.getPlaceName());
            pstmt.setString(5, trajectory.getDescription());
            pstmt.setString(6, trajectory.getPhotoUrl());
            pstmt.setBoolean(7, trajectory.getIsShared() != null ? trajectory.getIsShared() : false);
            pstmt.setLong(8, trajectory.getId());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[TrajectoryDAO] 更新轨迹点，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 更新轨迹点失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除轨迹点
     * @param id 轨迹点ID
     * @return 删除成功返回true
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM trajectories WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[TrajectoryDAO] 删除轨迹点，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 删除轨迹点失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取情侣的共享轨迹点
     * @param partnerId 情侣用户ID
     * @param daysBack 查询天数
     * @return 轨迹点列表
     */
    public List<Trajectory> findSharedByPartnerId(Long partnerId, int daysBack) {
        List<Trajectory> trajectories = new ArrayList<>();
        String sql = "SELECT * FROM trajectories WHERE user_id = ? AND is_shared = TRUE AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, partnerId);
            pstmt.setInt(2, daysBack);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trajectories.add(mapResultSetToTrajectory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 查询情侣共享轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TrajectoryDAO] 查询到 " + trajectories.size() + " 个情侣共享轨迹点");
        return trajectories;
    }
    
    /**
     * 获取用户的最新轨迹点
     * @param userId 用户ID
     * @return 最新的轨迹点对象，如果不存在返回null
     */
    public Trajectory findLatestByUserId(Long userId) {
        String sql = "SELECT * FROM trajectories WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrajectory(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 查询用户最新轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 将ResultSet映射为Trajectory对象
     */
    private Trajectory mapResultSetToTrajectory(ResultSet rs) throws SQLException {
        Trajectory trajectory = new Trajectory();
        trajectory.setId(rs.getLong("id"));
        trajectory.setUserId(rs.getLong("user_id"));
        trajectory.setLatitude(rs.getDouble("latitude"));
        trajectory.setLongitude(rs.getDouble("longitude"));
        trajectory.setAddress(rs.getString("address"));
        trajectory.setPlaceName(rs.getString("place_name"));
        trajectory.setDescription(rs.getString("description"));
        trajectory.setPhotoUrl(rs.getString("photo_url"));
        trajectory.setIsShared(rs.getBoolean("is_shared"));
        trajectory.setCreatedAt(rs.getTimestamp("created_at"));
        trajectory.setUpdatedAt(rs.getTimestamp("updated_at"));
        return trajectory;
    }
}