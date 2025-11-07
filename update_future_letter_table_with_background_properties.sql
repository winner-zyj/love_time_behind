-- 更新未来情书表，添加背景图片透明度和尺寸字段
-- Update future_letter table to add background image opacity and size fields

-- 添加背景图片透明度字段
ALTER TABLE future_letter 
ADD COLUMN background_opacity DECIMAL(3,2) DEFAULT 1.0 COMMENT '背景图片透明度 (0.0-1.0)' AFTER background_image;

-- 添加背景图片宽度字段
ALTER TABLE future_letter 
ADD COLUMN background_width INT DEFAULT 300 COMMENT '背景图片宽度' AFTER background_opacity;

-- 添加背景图片高度字段
ALTER TABLE future_letter 
ADD COLUMN background_height INT DEFAULT 400 COMMENT '背景图片高度' AFTER background_width;

-- 创建索引以提高查询性能
ALTER TABLE future_letter 
ADD INDEX idx_background_props (background_opacity, background_width, background_height);

-- 更新现有数据的默认值（可选）
UPDATE future_letter 
SET background_opacity = 1.0, 
    background_width = 300, 
    background_height = 400 
WHERE background_opacity IS NULL;

-- 验证表结构
DESCRIBE future_letter;