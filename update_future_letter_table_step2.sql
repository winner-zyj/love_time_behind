-- 第二步：修改delivery_method字段，只保留PARTNER选项
-- Step 2: Modify delivery_method column to only keep PARTNER option

ALTER TABLE future_letter 
MODIFY COLUMN delivery_method ENUM('PARTNER') NOT NULL DEFAULT 'PARTNER' COMMENT '发送方式：情侣对方(PARTNER)';