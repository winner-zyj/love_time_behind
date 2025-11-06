-- 未来情书功能数据库表设计（新版本）
-- Future Letter Feature Database Schema (New Version)

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS future_letter;

-- 未来情书表
CREATE TABLE future_letter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '信件ID',
    sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
    receiver_id BIGINT COMMENT '接收者用户ID（情侣关系中的对方）',
    
    -- 信件基本信息
    title VARCHAR(200) NOT NULL COMMENT '信件主题',
    content TEXT NOT NULL COMMENT '信件内容（最大1000字）',
    
    -- 接收方式（只支持情侣对方）
    delivery_method ENUM('PARTNER') NOT NULL DEFAULT 'PARTNER' COMMENT '发送方式：情侣对方',
    
    -- 时间相关
    scheduled_date DATE NOT NULL COMMENT '预计发送日期',
    scheduled_time TIME NOT NULL DEFAULT '00:00:00' COMMENT '预计发送时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 状态相关
    status ENUM('DRAFT', 'SCHEDULED', 'SENT', 'READ', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态：草稿/已安排/已发送/已读/已取消',
    sent_at DATETIME COMMENT '实际发送时间',
    read_at DATETIME COMMENT '阅读时间',
    
    -- 附加信息
    background_image VARCHAR(500) COMMENT '背景图片URL',
    background_opacity DECIMAL(3,2) DEFAULT 1.0 COMMENT '背景图片透明度 (0.0-1.0)',
    background_width INT DEFAULT 300 COMMENT '背景图片宽度',
    background_height INT DEFAULT 400 COMMENT '背景图片高度',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    deleted_at DATETIME COMMENT '删除时间',
    
    -- 索引
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_scheduled_date (scheduled_date),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_sender_status (sender_id, status),
    INDEX idx_receiver_status (receiver_id, status),
    
    -- 外键约束（引用users表）
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='未来情书表';

-- 插入测试数据示例
INSERT INTO future_letter (
    sender_id, 
    receiver_id, 
    title, 
    content, 
    delivery_method,
    scheduled_date,
    scheduled_time,
    status,
    background_image,
    background_opacity,
    background_width,
    background_height
) VALUES (
    1,
    2,
    '给未来的你',
    '也许想对他说的话\n写成一封信\n在未来的某天会给他吧',
    'PARTNER',
    '2025-07-22',
    '00:00:00',
    'SCHEDULED',
    'https://example.com/rose-background.jpg',
    0.8,
    300,
    400
);