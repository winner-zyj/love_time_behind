-- 测试轨迹当前位置接口所需的数据
-- Test data for trajectory current location endpoint

-- 查看用户表结构
DESCRIBE users;

-- 查看轨迹表结构
DESCRIBE trajectories;

-- 查看现有用户数据
SELECT id, openid, code, nickName, avatarUrl, created_at FROM users;

-- 查看现有轨迹数据
SELECT id, user_id, latitude, longitude, created_at FROM trajectories ORDER BY created_at DESC LIMIT 10;

-- 检查特定用户的轨迹数据
SELECT id, user_id, latitude, longitude, created_at FROM trajectories WHERE user_id = 1 ORDER BY created_at DESC LIMIT 5;