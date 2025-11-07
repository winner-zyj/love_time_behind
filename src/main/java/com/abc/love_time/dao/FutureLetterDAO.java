package com.abc.love_time.dao;

import com.abc.love_time.entity.FutureLetter;
import com.abc.love_time.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 未来情书数据访问对象
 */
public class FutureLetterDAO {

    /**
     * 插入新的未来情书
     * @param letter 未来情书对象
     * @return 插入成功返回情书ID，失败返回-1
     */
    public long insert(FutureLetter letter) {
        // 验证deliveryMethod，当前只支持PARTNER
        if (letter.getDeliveryMethod() == null || !"PARTNER".equals(letter.getDeliveryMethod())) {
            letter.setDeliveryMethod("PARTNER"); // 默认设置为PARTNER
        }
        
        String sql = "INSERT INTO future_letter (sender_id, receiver_id, title, content, delivery_method, scheduled_date, scheduled_time, status, background_image, background_opacity, background_width, background_height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, letter.getSenderId());
            pstmt.setObject(2, letter.getReceiverId(), Types.BIGINT);
            pstmt.setString(3, letter.getTitle());
            pstmt.setString(4, letter.getContent());
            pstmt.setString(5, letter.getDeliveryMethod());
            pstmt.setDate(6, letter.getScheduledDate());
            pstmt.setTime(7, letter.getScheduledTime());
            pstmt.setString(8, letter.getStatus());
            pstmt.setString(9, letter.getBackgroundImage());
            pstmt.setObject(10, letter.getBackgroundOpacity(), Types.DECIMAL);
            pstmt.setObject(11, letter.getBackgroundWidth(), Types.INTEGER);
            pstmt.setObject(12, letter.getBackgroundHeight(), Types.INTEGER);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        System.out.println("[FutureLetterDAO] 插入未来情书成功，ID: " + id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 插入未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * 根据ID查询未来情书
     * @param id 情书ID
     * @return 未来情书对象，如果不存在返回null
     */
    public FutureLetter findById(Long id) {
        String sql = "SELECT * FROM future_letter WHERE id = ? AND is_deleted = 0";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFutureLetter(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 根据ID查询未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 根据发送者ID查询未来情书列表
     * @param senderId 发送者ID
     * @return 未来情书列表
     */
    public List<FutureLetter> findBySenderId(Long senderId) {
        List<FutureLetter> letters = new ArrayList<>();
        String sql = "SELECT * FROM future_letter WHERE sender_id = ? AND is_deleted = 0 ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, senderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    letters.add(mapResultSetToFutureLetter(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 根据发送者ID查询未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[FutureLetterDAO] 根据发送者ID查询到 " + letters.size() + " 封未来情书");
        return letters;
    }
    
    /**
     * 根据接收者ID查询未来情书列表
     * @param receiverId 接收者ID
     * @return 未来情书列表
     */
    public List<FutureLetter> findByReceiverId(Long receiverId) {
        List<FutureLetter> letters = new ArrayList<>();
        String sql = "SELECT * FROM future_letter WHERE receiver_id = ? AND is_deleted = 0 AND status IN ('SCHEDULED', 'SENT') ORDER BY scheduled_date ASC, scheduled_time ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, receiverId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    letters.add(mapResultSetToFutureLetter(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 根据接收者ID查询未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[FutureLetterDAO] 根据接收者ID查询到 " + letters.size() + " 封未来情书");
        return letters;
    }
    
    /**
     * 查询情侣对方收到的未来情书（deliveryMethod为PARTNER的情况）
     * @param partnerId 情侣对方ID
     * @return 未来情书列表
     */
    public List<FutureLetter> findPartnerLetters(Long partnerId) {
        List<FutureLetter> letters = new ArrayList<>();
        String sql = "SELECT fl.* FROM future_letter fl " +
                     "JOIN couple_relationships cr ON (fl.sender_id = cr.user1_id OR fl.sender_id = cr.user2_id) " +
                     "WHERE fl.delivery_method = 'PARTNER' " +
                     "AND (cr.user1_id = ? OR cr.user2_id = ?) " +
                     "AND cr.status = 'active' " +
                     "AND fl.is_deleted = 0 " +
                     "AND fl.status = 'SENT' " +
                     "ORDER BY fl.sent_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, partnerId);
            pstmt.setLong(2, partnerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    letters.add(mapResultSetToFutureLetter(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 查询情侣对方收到的未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[FutureLetterDAO] 查询情侣对方收到的未来情书 " + letters.size() + " 封");
        return letters;
    }
    
    /**
     * 根据状态查询未来情书列表
     * @param senderId 发送者ID
     * @param status 状态
     * @return 未来情书列表
     */
    public List<FutureLetter> findByStatus(Long senderId, String status) {
        List<FutureLetter> letters = new ArrayList<>();
        String sql = "SELECT * FROM future_letter WHERE sender_id = ? AND status = ? AND is_deleted = 0 ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, senderId);
            pstmt.setString(2, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    letters.add(mapResultSetToFutureLetter(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 根据状态查询未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[FutureLetterDAO] 根据状态查询到 " + letters.size() + " 封未来情书");
        return letters;
    }
    
    /**
     * 更新未来情书
     * @param letter 未来情书对象
     * @return 更新成功返回true
     */
    public boolean update(FutureLetter letter) {
        // 验证deliveryMethod，当前只支持PARTNER
        if (letter.getDeliveryMethod() == null || !"PARTNER".equals(letter.getDeliveryMethod())) {
            letter.setDeliveryMethod("PARTNER"); // 默认设置为PARTNER
        }
        
        String sql = "UPDATE future_letter SET receiver_id = ?, title = ?, content = ?, delivery_method = ?, scheduled_date = ?, scheduled_time = ?, status = ?, background_image = ?, background_opacity = ?, background_width = ?, background_height = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, letter.getReceiverId(), Types.BIGINT);
            pstmt.setString(2, letter.getTitle());
            pstmt.setString(3, letter.getContent());
            pstmt.setString(4, letter.getDeliveryMethod());
            pstmt.setDate(5, letter.getScheduledDate());
            pstmt.setTime(6, letter.getScheduledTime());
            pstmt.setString(7, letter.getStatus());
            pstmt.setString(8, letter.getBackgroundImage());
            pstmt.setObject(9, letter.getBackgroundOpacity(), Types.DECIMAL);
            pstmt.setObject(10, letter.getBackgroundWidth(), Types.INTEGER);
            pstmt.setObject(11, letter.getBackgroundHeight(), Types.INTEGER);
            pstmt.setLong(12, letter.getId());
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[FutureLetterDAO] 更新未来情书，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 更新未来情书失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 更新未来情书状态
     * @param id 情书ID
     * @param status 状态
     * @param sentAt 发送时间（可选）
     * @param readAt 阅读时间（可选）
     * @return 更新成功返回true
     */
    public boolean updateStatus(Long id, String status, Timestamp sentAt, Timestamp readAt) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE future_letter SET status = ?");
        if (sentAt != null) {
            sqlBuilder.append(", sent_at = ?");
        }
        if (readAt != null) {
            sqlBuilder.append(", read_at = ?");
        }
        sqlBuilder.append(" WHERE id = ?");
        
        String sql = sqlBuilder.toString();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setString(paramIndex++, status);
            if (sentAt != null) {
                pstmt.setTimestamp(paramIndex++, sentAt);
            }
            if (readAt != null) {
                pstmt.setTimestamp(paramIndex++, readAt);
            }
            pstmt.setLong(paramIndex++, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[FutureLetterDAO] 更新未来情书状态，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 更新未来情书状态失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 软删除未来情书
     * @param id 情书ID
     * @return 删除成功返回true
     */
    public boolean deleteById(Long id) {
        String sql = "UPDATE future_letter SET is_deleted = 1, deleted_at = NOW() WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("[FutureLetterDAO] 软删除未来情书，影响行数: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 软删除未来情书失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 查询需要发送的未来情书（定时任务使用）
     * @return 需要发送的未来情书列表
     */
    public List<FutureLetter> findLettersToSend() {
        List<FutureLetter> letters = new ArrayList<>();
        String sql = "SELECT * FROM future_letter WHERE status = 'SCHEDULED' AND is_deleted = 0 AND (scheduled_date < CURDATE() OR (scheduled_date = CURDATE() AND scheduled_time <= CURTIME()))";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                letters.add(mapResultSetToFutureLetter(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FutureLetterDAO] 查询需要发送的未来情书失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[FutureLetterDAO] 查询到 " + letters.size() + " 封需要发送的未来情书");
        return letters;
    }
    
    /**
     * 将ResultSet映射为FutureLetter对象
     */
    private FutureLetter mapResultSetToFutureLetter(ResultSet rs) throws SQLException {
        FutureLetter letter = new FutureLetter();
        letter.setId(rs.getLong("id"));
        letter.setSenderId(rs.getLong("sender_id"));
        letter.setReceiverId(rs.getObject("receiver_id", Long.class));
        letter.setTitle(rs.getString("title"));
        letter.setContent(rs.getString("content"));
        letter.setDeliveryMethod(rs.getString("delivery_method"));
        letter.setScheduledDate(rs.getDate("scheduled_date"));
        letter.setScheduledTime(rs.getTime("scheduled_time"));
        letter.setCreatedAt(rs.getTimestamp("created_at"));
        letter.setUpdatedAt(rs.getTimestamp("updated_at"));
        letter.setStatus(rs.getString("status"));
        letter.setSentAt(rs.getTimestamp("sent_at"));
        letter.setReadAt(rs.getTimestamp("read_at"));
        letter.setBackgroundImage(rs.getString("background_image"));
        letter.setBackgroundOpacity(rs.getObject("background_opacity", Double.class));
        letter.setBackgroundWidth(rs.getObject("background_width", Integer.class));
        letter.setBackgroundHeight(rs.getObject("background_height", Integer.class));
        letter.setIsDeleted(rs.getBoolean("is_deleted"));
        letter.setDeletedAt(rs.getTimestamp("deleted_at"));
        return letter;
    }
}