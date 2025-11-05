package com.abc.love_time.dao;

import com.abc.love_time.entity.ChallengeTask;
import com.abc.love_time.entity.UserChallengeRecord;
import com.abc.love_time.entity.UserChallengeProgress;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 一百事挑战数据访问类
 */
public class ChallengeDAO {

    /**
     * 获取用户可见的所有任务（预设任务 + 用户自定义任务）
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<ChallengeTask> getTasksByUser(Long userId) {
        List<ChallengeTask> tasks = new ArrayList<>();
        String sql = "SELECT ct.*, ucr.status, ucr.photo_url, ucr.note, ucr.is_favorited, ucr.completed_at " +
                "FROM challenge_tasks ct " +
                "LEFT JOIN user_challenge_records ucr ON ct.id = ucr.task_id AND ucr.user_id = ? " +
                "WHERE ct.category = 'preset' OR ct.created_by = ? " +
                "ORDER BY ct.category DESC, ct.task_index ASC, ct.created_at ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChallengeTask task = mapResultSetToTask(rs);
                    // 设置用户完成状态
                    if (rs.getObject("status") != null) {
                        UserChallengeRecord record = new UserChallengeRecord();
                        record.setStatus(rs.getString("status"));
                        record.setPhotoUrl(rs.getString("photo_url"));
                        record.setNote(rs.getString("note"));
                        record.setIsFavorited(rs.getBoolean("is_favorited"));
                        record.setCompletedAt(rs.getTimestamp("completed_at"));
                        task.setUserRecord(record);
                    }
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] 获取用户任务列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tasks;
    }

    /**
     * 获取用户进度
     * @param userId 用户ID
     * @return 用户进度信息
     */
    public UserChallengeProgress getProgressByUser(Long userId) {
        String sql = "SELECT * FROM user_challenge_progress WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProgress(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] 获取用户进度失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 添加自定义任务
     * @param task 任务对象
     * @return 任务ID
     */
    public long addCustomTask(ChallengeTask task) {
        String sql = "INSERT INTO challenge_tasks (task_name, task_description, category, created_by, is_active, created_at, updated_at) " +
                "VALUES (?, ?, 'custom', ?, TRUE, NOW(), NOW())";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, task.getTaskName());
            pstmt.setString(2, task.getTaskDescription());
            pstmt.setLong(3, task.getCreatedBy());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] 添加自定义任务失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * 删除自定义任务
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    public boolean deleteCustomTask(Long taskId, Long userId) {
        String sql = "DELETE FROM challenge_tasks WHERE id = ? AND category = 'custom' AND created_by = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, taskId);
            pstmt.setLong(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] 删除自定义任务失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新任务完成状态
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param completed 是否完成
     * @param photoUrl 照片URL（可选）
     * @param note 备注（可选）
     * @return 是否更新成功
     */
    public boolean updateTaskCompletion(Long userId, Long taskId, boolean completed, String photoUrl, String note) {
        if (completed) {
            // 标记完成 - 插入或更新记录
            String sql = "INSERT INTO user_challenge_records (user_id, task_id, status, photo_url, note, completed_at, updated_at) " +
                    "VALUES (?, ?, 'completed', ?, ?, NOW(), NOW()) " +
                    "ON DUPLICATE KEY UPDATE status = 'completed', photo_url = VALUES(photo_url), note = VALUES(note), " +
                    "completed_at = VALUES(completed_at), updated_at = VALUES(updated_at)";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, userId);
                pstmt.setLong(2, taskId);
                pstmt.setString(3, photoUrl);
                pstmt.setString(4, note);
                
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                System.err.println("[ChallengeDAO] 标记任务完成失败: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            // 取消完成 - 更新状态
            String sql = "UPDATE user_challenge_records SET status = 'pending', completed_at = NULL, updated_at = NOW() " +
                    "WHERE user_id = ? AND task_id = ?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, userId);
                pstmt.setLong(2, taskId);
                
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                System.err.println("[ChallengeDAO] 取消任务完成失败: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 更新任务收藏状态
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param favorited 是否收藏
     * @return 是否更新成功
     */
    public boolean updateTaskFavorite(Long userId, Long taskId, boolean favorited) {
        // 先检查记录是否存在
        String checkSql = "SELECT id FROM user_challenge_records WHERE user_id = ? AND task_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setLong(1, userId);
            checkStmt.setLong(2, taskId);
            
            boolean recordExists = false;
            try (ResultSet rs = checkStmt.executeQuery()) {
                recordExists = rs.next();
            }
            
            if (recordExists) {
                // 更新现有记录
                String updateSql = "UPDATE user_challenge_records SET is_favorited = ?, updated_at = NOW() WHERE user_id = ? AND task_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBoolean(1, favorited);
                    updateStmt.setLong(2, userId);
                    updateStmt.setLong(3, taskId);
                    
                    int affectedRows = updateStmt.executeUpdate();
                    return affectedRows > 0;
                }
            } else {
                // 插入新记录
                String insertSql = "INSERT INTO user_challenge_records (user_id, task_id, is_favorited, status, created_at, updated_at) " +
                        "VALUES (?, ?, ?, 'pending', NOW(), NOW())";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setLong(1, userId);
                    insertStmt.setLong(2, taskId);
                    insertStmt.setBoolean(3, favorited);
                    
                    int affectedRows = insertStmt.executeUpdate();
                    return affectedRows > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ChallengeDAO] 更新任务收藏状态失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将ResultSet映射为ChallengeTask对象
     */
    private ChallengeTask mapResultSetToTask(ResultSet rs) throws SQLException {
        ChallengeTask task = new ChallengeTask();
        task.setId(rs.getLong("id"));
        task.setTaskName(rs.getString("task_name"));
        task.setTaskDescription(rs.getString("task_description"));
        task.setCategory(rs.getString("category"));
        task.setTaskIndex(rs.getObject("task_index") != null ? rs.getInt("task_index") : null);
        task.setCreatedBy(rs.getObject("created_by") != null ? rs.getLong("created_by") : null);
        task.setIconUrl(rs.getString("icon_url"));
        task.setIsActive(rs.getBoolean("is_active"));
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setUpdatedAt(rs.getTimestamp("updated_at"));
        return task;
    }

    /**
     * 将ResultSet映射为UserChallengeProgress对象
     */
    private UserChallengeProgress mapResultSetToProgress(ResultSet rs) throws SQLException {
        UserChallengeProgress progress = new UserChallengeProgress();
        progress.setId(rs.getLong("id"));
        progress.setUserId(rs.getLong("user_id"));
        progress.setTotalTasks(rs.getInt("total_tasks"));
        progress.setCompletedCount(rs.getInt("completed_count"));
        progress.setFavoritedCount(rs.getInt("favorited_count"));
        progress.setCompletionRate(rs.getDouble("completion_rate"));
        progress.setLastActiveAt(rs.getTimestamp("last_active_at"));
        progress.setCreatedAt(rs.getTimestamp("created_at"));
        progress.setUpdatedAt(rs.getTimestamp("updated_at"));
        return progress;
    }
}