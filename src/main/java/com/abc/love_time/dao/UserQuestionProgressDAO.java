package com.abc.love_time.dao;

import com.abc.love_time.entity.UserQuestionProgress;
import com.abc.love_time.util.DBUtil;

import java.sql.*;

/**
 * 用户答题进度数据访问类
 */
public class UserQuestionProgressDAO {

    /**
     * 根据用户ID查询进度
     */
    public UserQuestionProgress findByUserId(Long userId) {
        String sql = "SELECT * FROM user_question_progress WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProgress(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProgressDAO] 查询进度失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 插入用户进度记录
     */
    public long insert(UserQuestionProgress progress) {
        String sql = "INSERT INTO user_question_progress (user_id, current_question_id, completed_count, total_count) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, progress.getUserId());
            pstmt.setObject(2, progress.getCurrentQuestionId());
            pstmt.setInt(3, progress.getCompletedCount());
            pstmt.setInt(4, progress.getTotalCount());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[ProgressDAO] 进度插入成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProgressDAO] 插入进度失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * 更新用户进度
     */
    public boolean update(UserQuestionProgress progress) {
        String sql = "UPDATE user_question_progress SET current_question_id = ?, completed_count = ?, total_count = ? WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, progress.getCurrentQuestionId());
            pstmt.setInt(2, progress.getCompletedCount());
            pstmt.setInt(3, progress.getTotalCount());
            pstmt.setLong(4, progress.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[ProgressDAO] 更新进度，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[ProgressDAO] 更新进度失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 初始化或更新用户进度
     */
    public boolean initOrUpdate(Long userId, Long currentQuestionId, int totalCount) {
        UserQuestionProgress existing = findByUserId(userId);
        
        if (existing == null) {
            // 创建新进度
            UserQuestionProgress progress = new UserQuestionProgress(userId);
            progress.setCurrentQuestionId(currentQuestionId);
            progress.setCompletedCount(0);
            progress.setTotalCount(totalCount);
            return insert(progress) > 0;
        } else {
            // 更新现有进度
            existing.setCurrentQuestionId(currentQuestionId);
            existing.setTotalCount(totalCount);
            return update(existing);
        }
    }

    /**
     * 增加用户完成数并更新当前问题
     */
    public boolean incrementCompleted(Long userId, Long nextQuestionId) {
        UserQuestionProgress progress = findByUserId(userId);
        
        if (progress != null) {
            progress.setCompletedCount(progress.getCompletedCount() + 1);
            progress.setCurrentQuestionId(nextQuestionId);
            return update(progress);
        }
        
        return false;
    }

    /**
     * 将ResultSet映射为UserQuestionProgress对象
     */
    private UserQuestionProgress mapResultSetToProgress(ResultSet rs) throws SQLException {
        UserQuestionProgress progress = new UserQuestionProgress();
        progress.setId(rs.getLong("id"));
        progress.setUserId(rs.getLong("user_id"));
        
        Long currentQuestionId = rs.getLong("current_question_id");
        progress.setCurrentQuestionId(rs.wasNull() ? null : currentQuestionId);
        
        progress.setCompletedCount(rs.getInt("completed_count"));
        progress.setTotalCount(rs.getInt("total_count"));
        progress.setLastActiveAt(rs.getTimestamp("last_active_at"));
        progress.setCreatedAt(rs.getTimestamp("created_at"));
        
        return progress;
    }
}
