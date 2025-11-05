package com.abc.love_time.dao;

import com.abc.love_time.entity.Question;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题数据访问类
 */
public class QuestionDAO {

    /**
     * 根据ID查询问题
     */
    public Question findById(Long id) {
        String sql = "SELECT * FROM questions WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToQuestion(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 查询问题失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * 查询所有启用的问题（按排序序号排序）
     */
    public List<Question> findAllActive() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE is_active = TRUE ORDER BY order_index ASC";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
            
            System.out.println("[QuestionDAO] 查询到 " + questions.size() + " 个启用问题");
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 查询所有问题失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return questions;
    }

    /**
     * 查询所有问题（包括预设+自定义）
     */
    public List<Question> findAllQuestions() {
        return findAllActive();
    }

    /**
     * 根据类型查询问题
     * @param category preset 或 custom
     */
    public List<Question> findByCategory(String category) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE category = ? AND is_active = TRUE ORDER BY order_index ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
            
            System.out.println("[QuestionDAO] 查询到 " + questions.size() + " 个 " + category + " 类型问题");
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 按类型查询问题失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return questions;
    }

    /**
     * 根据用户ID查询其创建的自定义问题
     */
    public List<Question> findByUserId(Long userId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE created_by = ? AND category = 'custom' AND is_active = TRUE ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapResultSetToQuestion(rs));
                }
            }
            
            System.out.println("[QuestionDAO] 用户 " + userId + " 创建了 " + questions.size() + " 个自定义问题");
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 查询用户问题失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return questions;
    }

    /**
     * 插入自定义问题
     */
    public long insert(Question question) {
        String sql = "INSERT INTO questions (question_text, category, created_by, is_active, order_index) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, question.getQuestionText());
            pstmt.setString(2, question.getCategory());
            pstmt.setObject(3, question.getCreatedBy());
            pstmt.setBoolean(4, question.getIsActive() != null ? question.getIsActive() : true);
            pstmt.setInt(5, question.getOrderIndex() != null ? question.getOrderIndex() : 0);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[QuestionDAO] 问题插入成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 插入问题失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * 删除问题（逻辑删除，设置为不启用）
     */
    public boolean deleteById(Long id) {
        String sql = "UPDATE questions SET is_active = FALSE WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[QuestionDAO] 删除问题，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 删除问题失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 统计启用问题总数
     */
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM questions WHERE is_active = TRUE";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] 统计问题数失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * 将ResultSet映射为Question对象
     */
    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getLong("id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setCategory(rs.getString("category"));
        
        Long createdBy = rs.getLong("created_by");
        question.setCreatedBy(rs.wasNull() ? null : createdBy);
        
        question.setIsActive(rs.getBoolean("is_active"));
        question.setOrderIndex(rs.getInt("order_index"));
        question.setCreatedAt(rs.getTimestamp("created_at"));
        question.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        return question;
    }
}
