-- ============================================
-- 心型墙功能 - 数据库初始化脚本
-- 数据库名称: lovetime
-- 创建时间: 2025-10-30
-- 功能：情侣照片墙，支持上传最多40张照片排列成心形
-- ============================================

USE lovetime;

-- ============================================
-- 1. 心型墙项目表 (heart_wall_projects)
-- 用途：存储心型墙项目（一个用户可以创建多个心型墙项目）
-- ============================================
DROP TABLE IF EXISTS heart_wall_photos;
DROP TABLE IF EXISTS heart_wall_projects;

CREATE TABLE IF NOT EXISTS heart_wall_projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    project_name VARCHAR(200) DEFAULT '我们的回忆' COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    photo_count INT DEFAULT 0 COMMENT '已上传照片数量',
    max_photos INT DEFAULT 40 COMMENT '最大照片数量',
    cover_photo_url VARCHAR(500) COMMENT '封面照片URL',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_heart_wall_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心型墙项目表';

-- ============================================
-- 2. 心型墙照片表 (heart_wall_photos)
-- 用途：存储心型墙中的照片
-- ============================================
CREATE TABLE IF NOT EXISTS heart_wall_photos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '照片ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    photo_url VARCHAR(500) NOT NULL COMMENT '照片URL',
    thumbnail_url VARCHAR(500) COMMENT '缩略图URL',
    position_index INT NOT NULL COMMENT '照片位置索引（1-40）',
    caption TEXT COMMENT '照片说明',
    taken_date DATE COMMENT '拍摄日期',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project (project_id),
    INDEX idx_user (user_id),
    INDEX idx_position (project_id, position_index),
    CONSTRAINT fk_photo_project FOREIGN KEY (project_id) REFERENCES heart_wall_projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_photo_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_project_position (project_id, position_index) COMMENT '同一项目中位置不能重复'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心型墙照片表';

-- ============================================
-- 3. 创建触发器：自动更新照片数量
-- ============================================
DROP TRIGGER IF EXISTS after_photo_insert;
DROP TRIGGER IF EXISTS after_photo_delete;

DELIMITER //

-- 当插入照片时，自动更新项目照片数量
CREATE TRIGGER after_photo_insert
AFTER INSERT ON heart_wall_photos
FOR EACH ROW
BEGIN
    UPDATE heart_wall_projects 
    SET photo_count = (
        SELECT COUNT(*) 
        FROM heart_wall_photos 
        WHERE project_id = NEW.project_id
    )
    WHERE id = NEW.project_id;
    
    -- 如果是第一张照片，设置为封面
    UPDATE heart_wall_projects 
    SET cover_photo_url = NEW.photo_url
    WHERE id = NEW.project_id AND cover_photo_url IS NULL;
END//

-- 当删除照片时，自动更新项目照片数量
CREATE TRIGGER after_photo_delete
AFTER DELETE ON heart_wall_photos
FOR EACH ROW
BEGIN
    UPDATE heart_wall_projects 
    SET photo_count = (
        SELECT COUNT(*) 
        FROM heart_wall_photos 
        WHERE project_id = OLD.project_id
    )
    WHERE id = OLD.project_id;
    
    -- 如果删除的是封面照片，重新设置封面
    UPDATE heart_wall_projects p
    SET cover_photo_url = (
        SELECT photo_url 
        FROM heart_wall_photos 
        WHERE project_id = OLD.project_id 
        ORDER BY position_index 
        LIMIT 1
    )
    WHERE id = OLD.project_id AND cover_photo_url = OLD.photo_url;
END//

DELIMITER ;

-- ============================================
-- 4. 创建视图：方便查询心型墙完整信息
-- ============================================
CREATE OR REPLACE VIEW v_heart_wall_details AS
SELECT 
    p.id AS project_id,
    p.user_id,
    p.project_name,
    p.description,
    p.photo_count,
    p.max_photos,
    p.cover_photo_url,
    p.is_public,
    p.created_at,
    p.updated_at,
    u.nickName AS creator_name,
    u.avatarUrl AS creator_avatar
FROM heart_wall_projects p
JOIN users u ON p.user_id = u.id;

-- ============================================
-- 5. 创建辅助函数：检查项目是否已满
-- ============================================
DROP FUNCTION IF EXISTS is_heart_wall_full;

DELIMITER //

CREATE FUNCTION is_heart_wall_full(pid BIGINT) RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE is_full BOOLEAN DEFAULT FALSE;
    DECLARE current_count INT;
    DECLARE max_count INT;
    
    SELECT photo_count, max_photos INTO current_count, max_count
    FROM heart_wall_projects
    WHERE id = pid;
    
    IF current_count >= max_count THEN
        SET is_full = TRUE;
    END IF;
    
    RETURN is_full;
END//

DELIMITER ;

-- ============================================
-- 6. 创建辅助函数：获取下一个可用位置
-- ============================================
DROP FUNCTION IF EXISTS get_next_available_position;

DELIMITER //

CREATE FUNCTION get_next_available_position(pid BIGINT) RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE next_pos INT DEFAULT 1;
    DECLARE pos_exists INT;
    
    -- 查找1-40之间第一个未使用的位置
    WHILE next_pos <= 40 DO
        SELECT COUNT(*) INTO pos_exists
        FROM heart_wall_photos
        WHERE project_id = pid AND position_index = next_pos;
        
        IF pos_exists = 0 THEN
            RETURN next_pos;
        END IF;
        
        SET next_pos = next_pos + 1;
    END WHILE;
    
    -- 如果所有位置都被占用，返回-1
    RETURN -1;
END//

DELIMITER ;

-- ============================================
-- 7. 验证数据
-- ============================================

-- 查看创建的表
SHOW TABLES LIKE 'heart_wall%';

-- 查看触发器
SHOW TRIGGERS LIKE 'heart_wall_photos';

-- 查看函数
SHOW FUNCTION STATUS WHERE Db = 'lovetime' AND Name LIKE '%heart_wall%';

-- ============================================
-- 8. 测试数据（可选）
-- ============================================

-- 测试创建心型墙项目
-- INSERT INTO heart_wall_projects (user_id, project_name, description)
-- VALUES (1, '我们的甜蜜回忆', '记录我们在一起的美好时光');

-- 测试上传照片
-- INSERT INTO heart_wall_photos (project_id, user_id, photo_url, position_index, caption)
-- VALUES (1, 1, 'https://example.com/photo1.jpg', 1, '第一次约会');

-- 测试查询项目详情
-- SELECT * FROM v_heart_wall_details WHERE user_id = 1;

-- 测试检查是否已满
-- SELECT is_heart_wall_full(1) AS is_full;

-- 测试获取下一个可用位置
-- SELECT get_next_available_position(1) AS next_position;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 心型墙数据库初始化完成！' AS status,
       '已创建 heart_wall_projects, heart_wall_photos 两张表' AS info,
       '已创建自动更新照片数量的触发器' AS trigger_info,
       '已创建辅助函数和视图' AS function_info,
       '支持最多40张照片的心形排列' AS feature_info;
