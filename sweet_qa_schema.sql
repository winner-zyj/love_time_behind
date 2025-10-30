-- ============================================
-- 甜蜜问答功能 - 数据库初始化脚本
-- 数据库名称: lovetime
-- 创建时间: 2025-10-29
-- ============================================

-- 使用现有数据库
USE lovetime;

-- ============================================
-- 1. 问题表 (questions)
-- 用途：存储所有问题（预设问题和用户自定义问题）
-- ============================================
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问题ID',
    question_text VARCHAR(500) NOT NULL COMMENT '问题内容',
    category ENUM('preset', 'custom') NOT NULL DEFAULT 'preset' COMMENT '问题类型：preset=预设问题, custom=自定义问题',
    created_by BIGINT COMMENT '创建者用户ID（自定义问题时使用）',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    order_index INT DEFAULT 0 COMMENT '排序序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_created_by (created_by),
    INDEX idx_order (order_index),
    INDEX idx_active (is_active),
    CONSTRAINT fk_questions_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- ============================================
-- 2. 答案表 (answers)
-- 用途：存储用户对问题的回答
-- ============================================
CREATE TABLE IF NOT EXISTS answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    question_id BIGINT NOT NULL COMMENT '问题ID',
    user_id BIGINT NOT NULL COMMENT '回答用户ID',
    answer_text TEXT NOT NULL COMMENT '答案内容',
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '回答时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_question_id (question_id),
    INDEX idx_user_id (user_id),
    INDEX idx_answered_at (answered_at),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_question (user_id, question_id) COMMENT '同一用户对同一问题只能有一个答案'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案表';

-- ============================================
-- 3. 用户答题进度表 (user_question_progress)
-- 用途：跟踪每个用户的答题进度
-- ============================================
CREATE TABLE IF NOT EXISTS user_question_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    current_question_id BIGINT COMMENT '当前问题ID',
    completed_count INT DEFAULT 0 COMMENT '已完成问题数',
    total_count INT DEFAULT 0 COMMENT '总问题数',
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_question FOREIGN KEY (current_question_id) REFERENCES questions(id) ON DELETE SET NULL,
    UNIQUE KEY unique_user_progress (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户答题进度表';

-- ============================================
-- 4. 插入预设问题数据（5个甜蜜问题）
-- ============================================
INSERT INTO questions (question_text, category, order_index, is_active) VALUES
('我们第一次约会的地点是哪里？', 'preset', 1, TRUE),
('你最喜欢我做的哪道菜？', 'preset', 2, TRUE),
('如果周末只做一件事，你希望是什么？', 'preset', 3, TRUE),
('你心中的完美旅行是什么样的？', 'preset', 4, TRUE),
('这一年里，你最感动的一刻是什么？', 'preset', 5, TRUE);

-- ============================================
-- 5. 创建视图：获取情侣双方的答案对比
-- ============================================
CREATE OR REPLACE VIEW v_couple_answers AS
SELECT 
    q.id AS question_id,
    q.question_text,
    q.category,
    -- 用户1的信息
    cr.user1_id,
    a1.id AS user1_answer_id,
    a1.answer_text AS user1_answer,
    a1.answered_at AS user1_answered_at,
    -- 用户2的信息
    cr.user2_id,
    a2.id AS user2_answer_id,
    a2.answer_text AS user2_answer,
    a2.answered_at AS user2_answered_at,
    -- 关系信息
    cr.id AS relationship_id,
    cr.status AS relationship_status
FROM couple_relationships cr
CROSS JOIN questions q
LEFT JOIN answers a1 ON q.id = a1.question_id AND cr.user1_id = a1.user_id
LEFT JOIN answers a2 ON q.id = a2.question_id AND cr.user2_id = a2.user_id
WHERE cr.status = 'active' AND q.is_active = TRUE;

-- ============================================
-- 6. 创建函数：获取伴侣对某个问题的答案
-- ============================================
DROP FUNCTION IF EXISTS get_partner_answer;

DELIMITER //

CREATE FUNCTION get_partner_answer(uid BIGINT, qid BIGINT) RETURNS TEXT
DETERMINISTIC
BEGIN
    DECLARE partner_answer TEXT DEFAULT NULL;
    DECLARE partner_uid BIGINT;
    
    -- 获取伴侣ID
    SET partner_uid = get_partner_id(uid);
    
    -- 如果没有伴侣，返回NULL
    IF partner_uid IS NULL THEN
        RETURN NULL;
    END IF;
    
    -- 获取伴侣的答案
    SELECT answer_text INTO partner_answer
    FROM answers
    WHERE user_id = partner_uid AND question_id = qid
    LIMIT 1;
    
    RETURN partner_answer;
END//

DELIMITER ;

-- ============================================
-- 7. 创建函数：检查伴侣是否已回答某个问题
-- ============================================
DROP FUNCTION IF EXISTS partner_has_answered;

DELIMITER //

CREATE FUNCTION partner_has_answered(uid BIGINT, qid BIGINT) RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE has_answered BOOLEAN DEFAULT FALSE;
    DECLARE partner_uid BIGINT;
    DECLARE answer_count INT;
    
    -- 获取伴侣ID
    SET partner_uid = get_partner_id(uid);
    
    -- 如果没有伴侣，返回FALSE
    IF partner_uid IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- 检查伴侣是否已回答
    SELECT COUNT(*) INTO answer_count
    FROM answers
    WHERE user_id = partner_uid AND question_id = qid;
    
    IF answer_count > 0 THEN
        SET has_answered = TRUE;
    END IF;
    
    RETURN has_answered;
END//

DELIMITER ;

-- ============================================
-- 8. 验证数据
-- ============================================
-- 查看所有预设问题
SELECT * FROM questions WHERE category = 'preset' ORDER BY order_index;

-- 查看表结构
SHOW TABLES;

-- 查看函数
SHOW FUNCTION STATUS WHERE Db = 'lovetime' AND Name LIKE '%partner%';

-- ============================================
-- 9. 使用示例
-- ============================================

-- 示例1：查询某对情侣的所有问答对比
-- SELECT * FROM v_couple_answers WHERE user1_id = 1 OR user2_id = 1;

-- 示例2：获取伴侣对某个问题的答案
-- SELECT get_partner_answer(1, 1) AS partner_answer;

-- 示例3：检查伴侣是否已回答某个问题
-- SELECT partner_has_answered(1, 1) AS has_answered;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 甜蜜问答数据库初始化完成！' AS status,
       '已创建 questions, answers, user_question_progress 三张表' AS info,
       '已创建 v_couple_answers 视图和辅助函数' AS function_info,
       '已插入 5 个预设问题' AS data_info;
