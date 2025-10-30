-- ============================================
-- 情侣关系管理 - 数据库初始化脚本
-- 数据库名称: lovetime
-- 创建时间: 2025-10-30
-- 功能：实现情侣双向绑定关系，确保一对一关系
-- ============================================

USE lovetime;

-- ============================================
-- 1. 情侣关系表 (couple_relationships)
-- 用途：存储情侣绑定关系，确保一个用户只能与一个人绑定
-- ============================================
CREATE TABLE IF NOT EXISTS couple_relationships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    user1_id BIGINT NOT NULL COMMENT '用户1 ID',
    user2_id BIGINT NOT NULL COMMENT '用户2 ID',
    status ENUM('pending', 'active', 'rejected', 'broken') DEFAULT 'pending' COMMENT '关系状态：待确认/已绑定/已拒绝/已解绑',
    initiator_id BIGINT NOT NULL COMMENT '发起绑定的用户ID',
    receiver_id BIGINT NOT NULL COMMENT '接收请求的用户ID',
    relationship_name VARCHAR(100) COMMENT '关系昵称（如：我的宝贝）',
    anniversary_date DATE COMMENT '纪念日（恋爱开始日期）',
    request_message VARCHAR(500) COMMENT '绑定请求留言',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    confirmed_at TIMESTAMP NULL COMMENT '确认绑定时间',
    rejected_at TIMESTAMP NULL COMMENT '拒绝时间',
    broken_at TIMESTAMP NULL COMMENT '解绑时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    CONSTRAINT fk_couple_user1 FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_couple_user2 FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_couple_initiator FOREIGN KEY (initiator_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_couple_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id),
    INDEX idx_status (status),
    INDEX idx_initiator (initiator_id),
    INDEX idx_receiver (receiver_id),
    
    -- 约束：确保user1_id < user2_id，避免重复关系
    CONSTRAINT chk_user_order CHECK (user1_id < user2_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣关系表';

-- ============================================
-- 2. 创建触发器：确保一个用户只能有一个活跃的情侣关系
-- ============================================
DROP TRIGGER IF EXISTS before_couple_insert;
DROP TRIGGER IF EXISTS before_couple_update;

DELIMITER //

-- 插入前检查是否已有活跃关系
CREATE TRIGGER before_couple_insert
BEFORE INSERT ON couple_relationships
FOR EACH ROW
BEGIN
    DECLARE existing_count INT;
    
    -- 检查user1是否已有活跃关系
    SELECT COUNT(*) INTO existing_count
    FROM couple_relationships
    WHERE (user1_id = NEW.user1_id OR user2_id = NEW.user1_id)
      AND status = 'active';
    
    IF existing_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '用户1已存在活跃的情侣关系';
    END IF;
    
    -- 检查user2是否已有活跃关系
    SELECT COUNT(*) INTO existing_count
    FROM couple_relationships
    WHERE (user1_id = NEW.user2_id OR user2_id = NEW.user2_id)
      AND status = 'active';
    
    IF existing_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '用户2已存在活跃的情侣关系';
    END IF;
    
    -- 确保user1_id < user2_id
    IF NEW.user1_id > NEW.user2_id THEN
        SET @temp = NEW.user1_id;
        SET NEW.user1_id = NEW.user2_id;
        SET NEW.user2_id = @temp;
    END IF;
END//

-- 更新前检查状态变更
CREATE TRIGGER before_couple_update
BEFORE UPDATE ON couple_relationships
FOR EACH ROW
BEGIN
    DECLARE existing_count INT;
    
    -- 如果要将状态改为active，检查是否已有其他活跃关系
    IF NEW.status = 'active' AND OLD.status != 'active' THEN
        -- 检查user1
        SELECT COUNT(*) INTO existing_count
        FROM couple_relationships
        WHERE (user1_id = NEW.user1_id OR user2_id = NEW.user1_id)
          AND status = 'active'
          AND id != NEW.id;
        
        IF existing_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '用户1已存在其他活跃的情侣关系';
        END IF;
        
        -- 检查user2
        SELECT COUNT(*) INTO existing_count
        FROM couple_relationships
        WHERE (user1_id = NEW.user2_id OR user2_id = NEW.user2_id)
          AND status = 'active'
          AND id != NEW.id;
        
        IF existing_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '用户2已存在其他活跃的情侣关系';
        END IF;
        
        -- 设置确认时间
        SET NEW.confirmed_at = CURRENT_TIMESTAMP;
    END IF;
    
    -- 如果状态改为broken，设置解绑时间
    IF NEW.status = 'broken' AND OLD.status != 'broken' THEN
        SET NEW.broken_at = CURRENT_TIMESTAMP;
    END IF;
END//

DELIMITER ;

-- ============================================
-- 3. 创建辅助函数：检查用户是否有待处理的请求
-- ============================================
DROP FUNCTION IF EXISTS has_pending_request;

DELIMITER //

CREATE FUNCTION has_pending_request(uid BIGINT) RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE has_request BOOLEAN DEFAULT FALSE;
    DECLARE count_result INT;
    
    SELECT COUNT(*) INTO count_result
    FROM couple_relationships
    WHERE (initiator_id = uid OR receiver_id = uid)
      AND status = 'pending';
    
    IF count_result > 0 THEN
        SET has_request = TRUE;
    END IF;
    
    RETURN has_request;
END//

DELIMITER ;

-- ============================================
-- 4. 创建视图：方便查询用户的情侣关系
-- ============================================
CREATE OR REPLACE VIEW v_user_couple_relationship AS
SELECT 
    cr.id AS relationship_id,
    cr.status,
    cr.relationship_name,
    cr.anniversary_date,
    cr.created_at,
    cr.confirmed_at,
    -- 用户1的信息
    cr.user1_id,
    u1.code AS user1_code,
    u1.nickName AS user1_nickname,
    u1.avatarUrl AS user1_avatar,
    -- 用户2的信息
    cr.user2_id,
    u2.code AS user2_code,
    u2.nickName AS user2_nickname,
    u2.avatarUrl AS user2_avatar,
    -- 发起人信息
    cr.initiator_id,
    CASE 
        WHEN cr.initiator_id = cr.user1_id THEN u1.nickName
        ELSE u2.nickName
    END AS initiator_name
FROM couple_relationships cr
JOIN users u1 ON cr.user1_id = u1.id
JOIN users u2 ON cr.user2_id = u2.id
WHERE cr.status = 'active';

-- ============================================
-- 5. 创建辅助函数：获取用户的伴侣ID
-- ============================================
DROP FUNCTION IF EXISTS get_partner_id;

DELIMITER //

CREATE FUNCTION get_partner_id(uid BIGINT) RETURNS BIGINT
DETERMINISTIC
BEGIN
    DECLARE partner_id BIGINT DEFAULT NULL;
    
    SELECT 
        CASE 
            WHEN user1_id = uid THEN user2_id
            WHEN user2_id = uid THEN user1_id
            ELSE NULL
        END INTO partner_id
    FROM couple_relationships
    WHERE (user1_id = uid OR user2_id = uid)
      AND status = 'active'
    LIMIT 1;
    
    RETURN partner_id;
END//

DELIMITER ;

-- ============================================
-- 6. 创建辅助函数：检查两个用户是否是情侣
-- ============================================
DROP FUNCTION IF EXISTS is_couple;

DELIMITER //

CREATE FUNCTION is_couple(uid1 BIGINT, uid2 BIGINT) RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE is_coupled BOOLEAN DEFAULT FALSE;
    DECLARE count_result INT;
    
    SELECT COUNT(*) INTO count_result
    FROM couple_relationships
    WHERE ((user1_id = uid1 AND user2_id = uid2) 
        OR (user1_id = uid2 AND user2_id = uid1))
      AND status = 'active';
    
    IF count_result > 0 THEN
        SET is_coupled = TRUE;
    END IF;
    
    RETURN is_coupled;
END//

DELIMITER ;

-- ============================================
-- 7. 验证数据
-- ============================================

-- 查看创建的表
SHOW TABLES LIKE 'couple%';

-- 查看触发器
SHOW TRIGGERS LIKE 'couple_relationships';

-- 查看存储过程
SHOW PROCEDURE STATUS WHERE Db = 'lovetime' AND Name = 'generate_invite_code';

-- 查看函数
SHOW FUNCTION STATUS WHERE Db = 'lovetime';

-- ============================================
-- 8. 测试数据（可选）
-- ============================================

-- 测试插入绑定请求（需要先有用户数据）
-- 用户1向用户2发送绑定请求
-- INSERT INTO couple_relationships (user1_id, user2_id, initiator_id, receiver_id, request_message, status)
-- VALUES (1, 2, 1, 2, '我们在一起吧！', 'pending');

-- 用户2接受请求
-- UPDATE couple_relationships SET status = 'active' WHERE id = 1;

-- 用户2拒绝请求
-- UPDATE couple_relationships SET status = 'rejected' WHERE id = 1;

-- 测试查询伴侣ID
-- SELECT get_partner_id(1) AS partner_id;

-- 测试检查是否是情侣
-- SELECT is_couple(1, 2) AS is_coupled;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 情侣关系数据库初始化完成！' AS status,
       '已创建 couple_relationships 表' AS table_info,
       '已创建触发器确保一对一关系' AS trigger_info,
       '已创建辅助函数和视图' AS function_info;
