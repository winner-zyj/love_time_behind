-- 第一步：删除手机号和微信号相关字段（手动检查执行）
-- Step 1: Drop phone and WeChat related columns (manually check before execution)

-- 请先执行以下查询来检查字段是否存在：
-- SELECT column_name FROM information_schema.columns 
-- WHERE table_schema = 'lovetime' 
-- AND table_name = 'future_letter' 
-- AND column_name IN ('receiver_phone', 'receiver_wechat');

-- 如果字段存在，则执行以下语句：
-- ALTER TABLE future_letter DROP COLUMN receiver_phone;
-- ALTER TABLE future_letter DROP COLUMN receiver_wechat;

-- 如果字段不存在，则跳过上述语句
SELECT '请手动检查字段是否存在，然后决定是否执行删除操作' as instruction;
