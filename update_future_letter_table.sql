-- 更新未来情书表结构以优化当前功能需求
-- Update Future Letter Table Structure for Current Feature Requirements

-- 删除手机号和微信号相关字段
ALTER TABLE future_letter 
DROP COLUMN receiver_phone,
DROP COLUMN receiver_wechat;

-- 修改delivery_method字段，只保留PARTNER选项
ALTER TABLE future_letter 
MODIFY COLUMN delivery_method ENUM('PARTNER') NOT NULL DEFAULT 'PARTNER' COMMENT '发送方式：情侣对方(PARTNER)';

-- 重新创建必要的索引（先删除再创建）
-- 注意：这里直接尝试删除索引，如果不存在会报错但不影响执行
-- 在实际执行时，请确保先检查索引是否存在
ALTER TABLE future_letter 
DROP INDEX idx_sender_status,
DROP INDEX idx_receiver_status,
DROP INDEX idx_delivery_method;

-- 重新创建必要的索引
ALTER TABLE future_letter 
ADD INDEX idx_sender_status (sender_id, status),
ADD INDEX idx_receiver_status (receiver_id, status);

-- 查询验证
-- SELECT COUNT(*) as total_letters FROM future_letter;
-- SELECT delivery_method, COUNT(*) as count FROM future_letter GROUP BY delivery_method;