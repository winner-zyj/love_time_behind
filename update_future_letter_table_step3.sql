-- 第三步：重建索引
-- Step 3: Rebuild indexes

-- 先删除现有索引（如果存在）
-- 注意：请根据实际情况选择执行以下语句
-- 如果索引存在则删除，不存在则跳过

-- 删除索引的语句需要在MySQL命令行中逐个执行，并忽略错误：
-- ALTER TABLE future_letter DROP INDEX idx_sender_status;
-- ALTER TABLE future_letter DROP INDEX idx_receiver_status;
-- ALTER TABLE future_letter DROP INDEX idx_delivery_method;

-- 重新创建必要的索引
ALTER TABLE future_letter 
ADD INDEX idx_sender_status (sender_id, status),
ADD INDEX idx_receiver_status (receiver_id, status);