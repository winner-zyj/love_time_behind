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
        String sql = "INSERT INTO trajectories (user_id, latitude, longitude, visit_time, address, place_name, description, photo_url, is_shared) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, trajectory.getUserId());
            pstmt.setDouble(2, trajectory.getLatitude());
            pstmt.setDouble(3, trajectory.getLongitude());
            // 如果visit_time为null，则使用当前时间
            pstmt.setTimestamp(4, trajectory.getVisitTime() != null ? trajectory.getVisitTime() : new Timestamp(System.currentTimeMillis()));
            pstmt.setString(5, trajectory.getAddress());
            pstmt.setString(6, trajectory.getPlaceName());
            pstmt.setString(7, trajectory.getDescription());
            pstmt.setString(8, trajectory.getPhotoUrl());
            pstmt.setBoolean(9, trajectory.getIsShared() != null ? trajectory.getIsShared() : false);
            
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
        String sql = "UPDATE trajectories SET latitude = ?, longitude = ?, visit_time = ?, address = ?, place_name = ?, description = ?, photo_url = ?, is_shared = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, trajectory.getLatitude());
            pstmt.setDouble(2, trajectory.getLongitude());
            pstmt.setTimestamp(3, trajectory.getVisitTime());
            pstmt.setString(4, trajectory.getAddress());
            pstmt.setString(5, trajectory.getPlaceName());
            pstmt.setString(6, trajectory.getDescription());
            pstmt.setString(7, trajectory.getPhotoUrl());
            pstmt.setBoolean(8, trajectory.getIsShared() != null ? trajectory.getIsShared() : false);
            pstmt.setLong(9, trajectory.getId());
            
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
     * 获取情侣的所有轨迹点（不仅限于共享的）
     * @param partnerId 情侣用户ID
     * @param daysBack 查询天数
     * @return 轨迹点列表
     */
    public List<Trajectory> findAllByPartnerId(Long partnerId, int daysBack) {
        List<Trajectory> trajectories = new ArrayList<>();
        String sql = "SELECT * FROM trajectories WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY created_at DESC";
        
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
            System.err.println("[TrajectoryDAO] 查询情侣所有轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TrajectoryDAO] 查询到 " + trajectories.size() + " 个情侣轨迹点");
        return trajectories;
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
     * 根据日期范围查询用户轨迹点
     * @param userId 用户ID
     * @param startDate 开始日期 (格式: YYYY-MM-DD)
     * @param endDate 结束日期 (格式: YYYY-MM-DD)
     * @return 轨迹点列表
     */
    public List<Trajectory> findByUserIdAndDateRange(Long userId, String startDate, String endDate) {
        List<Trajectory> trajectories = new ArrayList<>();
        String sql = "SELECT * FROM trajectories WHERE user_id = ? AND DATE(visit_time) BETWEEN ? AND ? ORDER BY visit_time ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trajectories.add(mapResultSetToTrajectory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 根据日期范围查询用户轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TrajectoryDAO] 根据日期范围查询到 " + trajectories.size() + " 个用户轨迹点");
        return trajectories;
    }
    
    /**
     * 根据日期范围查询情侣的所有轨迹点
     * @param partnerId 情侣用户ID
     * @param startDate 开始日期 (格式: YYYY-MM-DD)
     * @param endDate 结束日期 (格式: YYYY-MM-DD)
     * @return 轨迹点列表
     */
    public List<Trajectory> findAllByPartnerIdAndDateRange(Long partnerId, String startDate, String endDate) {
        List<Trajectory> trajectories = new ArrayList<>();
        String sql = "SELECT * FROM trajectories WHERE user_id = ? AND DATE(visit_time) BETWEEN ? AND ? ORDER BY visit_time ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, partnerId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trajectories.add(mapResultSetToTrajectory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 根据日期范围查询情侣所有轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TrajectoryDAO] 根据日期范围查询到 " + trajectories.size() + " 个情侣轨迹点");
        return trajectories;
    }
    
    /**
     * 根据日期范围查询情侣的共享轨迹点
     * @param partnerId 情侣用户ID
     * @param startDate 开始日期 (格式: YYYY-MM-DD)
     * @param endDate 结束日期 (格式: YYYY-MM-DD)
     * @return 轨迹点列表
     */
    public List<Trajectory> findSharedByPartnerIdAndDateRange(Long partnerId, String startDate, String endDate) {
        List<Trajectory> trajectories = new ArrayList<>();
        String sql = "SELECT * FROM trajectories WHERE user_id = ? AND is_shared = TRUE AND DATE(visit_time) BETWEEN ? AND ? ORDER BY visit_time ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, partnerId);
            pstmt.setString(2, startDate);
            pstmt.setString(3, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trajectories.add(mapResultSetToTrajectory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrajectoryDAO] 根据日期范围查询情侣共享轨迹点失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TrajectoryDAO] 根据日期范围查询到 " + trajectories.size() + " 个情侣共享轨迹点");
        return trajectories;
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
        trajectory.setVisitTime(rs.getTimestamp("visit_time")); // 添加visit_time字段映射
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