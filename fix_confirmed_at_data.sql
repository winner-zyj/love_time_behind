-- 修复现有数据，确保所有状态为"active"的情侣关系都有正确的confirmed_at时间
-- Fix existing data to ensure all "active" couple relationships have correct confirmed_at time

-- 更新所有状态为active但confirmed_at为空的记录
-- 将confirmed_at设置为created_at的值
UPDATE couple_relationships 
SET confirmed_at = created_at 
WHERE status = 'active' AND confirmed_at IS NULL;

-- 验证更新结果
SELECT id, status, created_at, confirmed_at 
FROM couple_relationships 
WHERE status = 'active' 
ORDER BY created_at DESC 
LIMIT 10;

-- 检查是否还有confirmed_at为空的active记录
SELECT COUNT(*) as remaining_null_confirmed_at
FROM couple_relationships 
WHERE status = 'active' AND confirmed_at IS NULL;