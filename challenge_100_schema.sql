-- ============================================
-- 情侣100件小事挑战功能 - 数据库初始化脚本
-- 数据库名称: lovetime
-- 创建时间: 2025-10-29
-- ============================================

-- 使用现有数据库
USE lovetime;

-- ============================================
-- 1. 挑战任务表 (challenge_tasks)
-- 用途：存储预设任务和用户自定义任务
-- ============================================
DROP TABLE IF EXISTS user_challenge_progress;
DROP TABLE IF EXISTS user_challenge_records;
DROP TABLE IF EXISTS challenge_tasks;

CREATE TABLE IF NOT EXISTS challenge_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_description TEXT COMMENT '任务描述',
    task_index INT COMMENT '任务序号（预设任务1-12，自定义任务为NULL）',
    category ENUM('preset', 'custom') DEFAULT 'custom' COMMENT '任务类型：preset=预设任务, custom=自定义任务',
    created_by BIGINT COMMENT '创建者用户ID（自定义任务时必填）',
    icon_url VARCHAR(500) COMMENT '任务图标URL',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_created_by (created_by),
    INDEX idx_task_index (task_index),
    INDEX idx_active (is_active),
    CONSTRAINT fk_task_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挑战任务表';

-- ============================================
-- 2. 用户挑战记录表 (user_challenge_records)
-- 用途：记录用户完成任务的情况
-- ============================================
CREATE TABLE IF NOT EXISTS user_challenge_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status ENUM('pending', 'completed') DEFAULT 'pending' COMMENT '完成状态',
    photo_url VARCHAR(500) COMMENT '上传的照片URL',
    note TEXT COMMENT '备注说明',
    is_favorited BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_task (user_id, task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_favorited (is_favorited),
    CONSTRAINT fk_challenge_task FOREIGN KEY (task_id) REFERENCES challenge_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_challenge_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_task (user_id, task_id) COMMENT '同一用户对同一任务只能有一条记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户挑战记录表';

-- ============================================
-- 3. 用户挑战进度表 (user_challenge_progress)
-- 用途：跟踪用户的整体挑战进度
-- ============================================
CREATE TABLE IF NOT EXISTS user_challenge_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_tasks INT DEFAULT 0 COMMENT '总任务数（包含预设和自定义）',
    completed_count INT DEFAULT 0 COMMENT '已完成数量',
    favorited_count INT DEFAULT 0 COMMENT '收藏数量',
    completion_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '完成率（百分比）',
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_progress (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户挑战进度表';

-- ============================================
-- 4. 插入12件小事预设任务数据
-- ============================================

INSERT INTO challenge_tasks (task_name, task_description, task_index, category) VALUES
('一起看日出', '找一个美好的清晨，一起迎接第一缕阳光', 1, 'preset'),
('一起看日落', '在夕阳西下时，享受彼此的陪伴', 2, 'preset'),
('一起去教堂', '在神圣的地方许下承诺', 3, 'preset'),
('一起看星星', '在晴朗的夜晚，一起数星星、许愿望', 4, 'preset'),
('一起看电影', '选一部你们都喜欢的电影，共度美好时光', 5, 'preset'),
('一起牵手逛街', '手牵手逛街，为对方挑选礼物', 6, 'preset'),
('一起做饭', '一起准备食材，一起烹饪美食', 7, 'preset'),
('一起逛超市', '像老夫老妻一样逛超市，挑选生活用品', 8, 'preset'),
('一起逛家', '一起逛家居店，布置温馨的家', 9, 'preset'),
('一起看相声', '欣赏传统曲艺，开怀大笑', 10, 'preset'),
('一起打票', '一起去看演出，提前买票期待', 11, 'preset'),
('一起躺雨', '在雨中漫步，感受浪漫', 12, 'preset');

-- ============================================
-- 5. 创建触发器：自动更新用户进度（支持自定义任务）
-- ============================================
DROP TRIGGER IF EXISTS after_challenge_update;
DROP TRIGGER IF EXISTS after_task_insert;
DROP TRIGGER IF EXISTS after_task_delete;

DELIMITER //

-- 当用户完成任务时，自动更新进度表
CREATE TRIGGER after_challenge_update
AFTER UPDATE ON user_challenge_records
FOR EACH ROW
BEGIN
    -- 计算用户可见的总任务数（预设任务 + 该用户创建的自定义任务）
    UPDATE user_challenge_progress 
    SET 
        total_tasks = (
            SELECT COUNT(*) 
            FROM challenge_tasks 
            WHERE category = 'preset' OR created_by = NEW.user_id
        ),
        completed_count = (
            SELECT COUNT(*) 
            FROM user_challenge_records 
            WHERE user_id = NEW.user_id AND status = 'completed'
        ),
        favorited_count = (
            SELECT COUNT(*) 
            FROM user_challenge_records 
            WHERE user_id = NEW.user_id AND is_favorited = TRUE
        ),
        completion_rate = (
            SELECT (COUNT(*) * 100.0) / NULLIF(
                (SELECT COUNT(*) FROM challenge_tasks WHERE category = 'preset' OR created_by = NEW.user_id),
                0
            )
            FROM user_challenge_records 
            WHERE user_id = NEW.user_id AND status = 'completed'
        )
    WHERE user_id = NEW.user_id;
    
    -- 如果进度记录不存在，则创建
    INSERT INTO user_challenge_progress (user_id, total_tasks, completed_count, favorited_count, completion_rate)
    SELECT NEW.user_id, 
        (SELECT COUNT(*) FROM challenge_tasks WHERE category = 'preset' OR created_by = NEW.user_id),
        (SELECT COUNT(*) FROM user_challenge_records WHERE user_id = NEW.user_id AND status = 'completed'),
        (SELECT COUNT(*) FROM user_challenge_records WHERE user_id = NEW.user_id AND is_favorited = TRUE),
        (SELECT (COUNT(*) * 100.0) / NULLIF(
            (SELECT COUNT(*) FROM challenge_tasks WHERE category = 'preset' OR created_by = NEW.user_id),
            0
        ) FROM user_challenge_records WHERE user_id = NEW.user_id AND status = 'completed')
    WHERE NOT EXISTS (SELECT 1 FROM user_challenge_progress WHERE user_id = NEW.user_id);
END//

-- 当用户添加新任务时，自动更新总任务数
CREATE TRIGGER after_task_insert
AFTER INSERT ON challenge_tasks
FOR EACH ROW
BEGIN
    IF NEW.category = 'custom' AND NEW.created_by IS NOT NULL THEN
        UPDATE user_challenge_progress 
        SET total_tasks = total_tasks + 1
        WHERE user_id = NEW.created_by;
        
        -- 如果进度记录不存在，则创建
        INSERT INTO user_challenge_progress (user_id, total_tasks)
        SELECT NEW.created_by, 1
        WHERE NOT EXISTS (SELECT 1 FROM user_challenge_progress WHERE user_id = NEW.created_by);
    END IF;
END//

-- 当删除任务时，自动更新总任务数
CREATE TRIGGER after_task_delete
AFTER DELETE ON challenge_tasks
FOR EACH ROW
BEGIN
    IF OLD.category = 'custom' AND OLD.created_by IS NOT NULL THEN
        UPDATE user_challenge_progress 
        SET total_tasks = GREATEST(0, total_tasks - 1)
        WHERE user_id = OLD.created_by;
    END IF;
END//

DELIMITER ;

-- ============================================
-- 6. 验证数据
-- ============================================

-- 查看预设任务
SELECT * FROM challenge_tasks WHERE category = 'preset' ORDER BY task_index;

-- 查看所有表
SHOW TABLES LIKE '%challenge%';

-- ============================================
-- 7. 执行完成提示
-- ============================================
SELECT '✅ 情侣100件小事挑战数据库初始化完成！' AS status,
       '已创建 challenge_tasks, user_challenge_records, user_challenge_progress 三张表' AS info,
       '已插入 12 个预设任务，可通过添加小事功能扩展到100件' AS data_info,
       '已创建自动更新进度的触发器（支持自定义任务）' AS trigger_info;
