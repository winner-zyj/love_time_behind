-- ============================================
-- 轨迹记录 - 数据库初始化脚本
-- 数据库名称: lovetime
-- ============================================

USE lovetime;

-- ============================================
-- 1. 轨迹点表 (trajectories)
-- 用途：存储用户的轨迹点信息
-- ============================================
CREATE TABLE IF NOT EXISTS trajectories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轨迹点ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    latitude DECIMAL(10, 7) NOT NULL COMMENT '纬度',
    longitude DECIMAL(10, 7) NOT NULL COMMENT '经度',
    address VARCHAR(255) COMMENT '地址名称',
    place_name VARCHAR(100) COMMENT '地点名称',
    description TEXT COMMENT '描述',
    photo_url VARCHAR(500) COMMENT '照片URL',
    is_shared BOOLEAN DEFAULT FALSE COMMENT '是否共享给情侣',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    CONSTRAINT fk_trajectories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_shared (is_shared)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轨迹点表';

-- ============================================
-- 2. 验证数据
-- ============================================

-- 查看创建的表
SHOW TABLES LIKE 'trajectories';

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 轨迹记录数据库初始化完成！' AS status,
       '已创建 trajectories 表' AS table_info;