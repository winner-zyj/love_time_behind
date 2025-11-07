-- 测试confirmed_at修复脚本
-- Test confirmed_at fix script

-- 查看当前情侣关系数据
SELECT 
    id,
    user1_id,
    user2_id,
    status,
    created_at,
    confirmed_at,
    updated_at
FROM couple_relationships 
ORDER BY created_at DESC;

-- 检查active状态的关系是否有confirmed_at
SELECT 
    id,
    status,
    created_at,
    confirmed_at
FROM couple_relationships 
WHERE status = 'active' 
ORDER BY created_at DESC;

-- 检查是否有confirmed_at为空的active记录
SELECT 
    id,
    status,
    created_at,
    confirmed_at
FROM couple_relationships 
WHERE status = 'active' AND confirmed_at IS NULL;