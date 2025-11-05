package com.abc.love_time.dao;

import com.abc.love_time.entity.CoupleRelationship;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 情侣关系数据访问类
 */
public class CoupleRelationshipDAO {

    /**
     * 根据用户ID查询情侣关系
     * @param userId 用户ID
     * @return 情侣关系对象，如果不存在返回null
     */
    public CoupleRelationship findByUserId(Long userId) {
        String sql = "SELECT * FROM couple_relationships WHERE (user1_id = ? OR user2_id = ?) AND status = 'active'";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRelationship(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CoupleRelationshipDAO] 查询情侣关系失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据ID查询情侣关系
     * @param id 关系ID
     * @return 情侣关系对象，如果不存在返回null
     */
    public CoupleRelationship findById(Long id) {
        String sql = "SELECT * FROM couple_relationships WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRelationship(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CoupleRelationshipDAO] 根据ID查询情侣关系失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 插入新的情侣关系请求
     * @param relationship 情侣关系对象
     * @return 插入成功返回自增ID，失败返回-1
     */
    public long insert(CoupleRelationship relationship) {
        String sql = "INSERT INTO couple_relationships (user1_id, user2_id, initiator_id, receiver_id, request_message, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // 确保 user1_id < user2_id
            long user1Id = Math.min(relationship.getUser1Id(), relationship.getUser2Id());
            long user2Id = Math.max(relationship.getUser1Id(), relationship.getUser2Id());
            
            pstmt.setLong(1, user1Id);
            pstmt.setLong(2, user2Id);
            pstmt.setLong(3, relationship.getInitiatorId());
            pstmt.setLong(4, relationship.getReceiverId());
            pstmt.setString(5, relationship.getRequestMessage());
            pstmt.setString(6, relationship.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[CoupleRelationshipDAO] 情侣关系插入成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CoupleRelationshipDAO] 插入情侣关系失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * 更新情侣关系状态
     * @param id 关系ID
     * @param status 新状态
     * @return 更新成功返回true
     */
    public boolean updateStatus(Long id, String status) {
        String sql = "UPDATE couple_relationships SET status = ?, " + 
                    (status.equals("active") ? "confirmed_at = NOW(), " : "") +
                    (status.equals("rejected") ? "rejected_at = NOW(), " : "") +
                    (status.equals("broken") ? "broken_at = NOW(), " : "") +
                    "updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setLong(2, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[CoupleRelationshipDAO] 更新情侣关系状态，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[CoupleRelationshipDAO] 更新情侣关系状态失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除情侣关系（逻辑删除，设置为broken状态）
     * @param id 关系ID
     * @return 删除成功返回true
     */
    public boolean deleteById(Long id) {
        return updateStatus(id, "broken");
    }

    /**
     * 根据用户ID获取伴侣ID
     * @param userId 用户ID
     * @return 伴侣ID，如果没有伴侣返回null
     */
    public Long getPartnerId(Long userId) {
        String sql = "SELECT get_partner_id(?) AS partner_id";
        
        try (Connection conn = DBUtil.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long partnerId = rs.getLong("partner_id");
                    if (partnerId > 0) {
                        return partnerId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CoupleRelationshipDAO] 获取伴侣ID失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 检查两个用户是否是情侣
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 是否是情侣
     */
    public boolean isCouple(Long userId1, Long userId2) {
        String sql = "SELECT is_couple(?, ?) AS is_coupled";
        
        try (Connection conn = DBUtil.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.setLong(1, userId1);
            stmt.setLong(2, userId2);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_coupled");
                }
            }
        } catch (SQLException e) {
            System.err.println("[CoupleRelationshipDAO] 检查是否是情侣失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * 将ResultSet映射为CoupleRelationship对象
     */
    private CoupleRelationship mapResultSetToRelationship(ResultSet rs) throws SQLException {
        CoupleRelationship relationship = new CoupleRelationship();
        relationship.setId(rs.getLong("id"));
        relationship.setUser1Id(rs.getLong("user1_id"));
        relationship.setUser2Id(rs.getLong("user2_id"));
        relationship.setStatus(rs.getString("status"));
        relationship.setInitiatorId(rs.getLong("initiator_id"));
        relationship.setReceiverId(rs.getLong("receiver_id"));
        relationship.setRelationshipName(rs.getString("relationship_name"));
        relationship.setAnniversaryDate(rs.getDate("anniversary_date"));
        relationship.setRequestMessage(rs.getString("request_message"));
        relationship.setCreatedAt(rs.getTimestamp("created_at"));
        relationship.setConfirmedAt(rs.getTimestamp("confirmed_at"));
        relationship.setRejectedAt(rs.getTimestamp("rejected_at"));
        relationship.setBrokenAt(rs.getTimestamp("broken_at"));
        relationship.setUpdatedAt(rs.getTimestamp("updated_at"));
        return relationship;
    }
}