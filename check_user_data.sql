-- 检查用户数据
-- Check user data

-- 查看用户表中的所有用户
SELECT id, openid, code, nickName, avatarUrl, created_at FROM users;

-- 查看特定openid的用户
SELECT id, openid, code, nickName, avatarUrl, created_at FROM users WHERE openid = 'odBB1xHIGMxjjk-tX695GWHDMmus';

-- 查看用户数量
SELECT COUNT(*) as user_count FROM users;

-- 查看最近创建的用户
SELECT id, openid, code, nickName, avatarUrl, created_at FROM users ORDER BY created_at DESC LIMIT 5;