package com.abc.love_time.dao;

import com.abc.love_time.entity.Answer;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 答案数据访问类
 */
public class AnswerDAO {

    /**
     * 根据ID查询答案
     */
    public Answer findById(Long id) {
        String sql = "SELECT * FROM answers WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnswer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 查询答案失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 根据用户ID和问题ID查询答案
     */
    public Answer findByUserAndQuestion(Long userId, Long questionId) {
        String sql = "SELECT * FROM answers WHERE user_id = ? AND question_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setLong(2, questionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAnswer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 查询用户答案失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 查询用户的所有答案
     */
    public List<Answer> findByUserId(Long userId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE user_id = ? ORDER BY answered_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapResultSetToAnswer(rs));
                }
            }
            
            System.out.println("[AnswerDAO] 用户 " + userId + " 有 " + answers.size() + " 个答案");
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 查询用户所有答案失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return answers;
    }

    /**
     * 插入答案
     */
    public long insert(Answer answer) {
        String sql = "INSERT INTO answers (question_id, user_id, answer_text) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, answer.getQuestionId());
            pstmt.setLong(2, answer.getUserId());
            pstmt.setString(3, answer.getAnswerText());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[AnswerDAO] 答案插入成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 插入答案失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * 更新答案内容
     */
    public boolean update(Answer answer) {
        String sql = "UPDATE answers SET answer_text = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, answer.getAnswerText());
            pstmt.setLong(2, answer.getId());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[AnswerDAO] 更新答案，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 更新答案失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除答案
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM answers WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[AnswerDAO] 删除答案，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 删除答案失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 统计用户已回答的问题数
     */
    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AnswerDAO] 统计用户答案数失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * 将ResultSet映射为Answer对象
     */
    private Answer mapResultSetToAnswer(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setId(rs.getLong("id"));
        answer.setQuestionId(rs.getLong("question_id"));
        answer.setUserId(rs.getLong("user_id"));
        answer.setAnswerText(rs.getString("answer_text"));
        answer.setAnsweredAt(rs.getTimestamp("answered_at"));
        answer.setUpdatedAt(rs.getTimestamp("updated_at"));
        return answer;
    }
}
