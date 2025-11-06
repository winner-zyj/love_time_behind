-- ============================================
-- 轨迹记录表更新脚本 - 添加visit_time字段
-- ============================================

USE lovetime;

-- 添加visit_time字段
ALTER TABLE trajectories 
ADD COLUMN visit_time TIMESTAMP NULL COMMENT '访问时间' AFTER longitude;

-- 为现有数据设置默认值（使用created_at作为visit_time）
UPDATE trajectories 
SET visit_time = created_at 
WHERE visit_time IS NULL;

-- 修改visit_time字段为非空
ALTER TABLE trajectories 
MODIFY COLUMN visit_time TIMESTAMP NOT NULL COMMENT '访问时间';

-- 添加索引以优化按时间查询
ALTER TABLE trajectories 
ADD INDEX idx_visit_time (visit_time);

-- 添加复合索引以优化用户和时间的联合查询
ALTER TABLE trajectories 
ADD INDEX idx_user_visit_time (user_id, visit_time);

-- ============================================
-- 验证更新
-- ============================================

-- 查看表结构
DESCRIBE trajectories;

-- ============================================
-- 执行完成提示
-- ============================================
SELECT '✅ 轨迹记录表更新完成！' AS status,
       '已添加 visit_time 字段及相关索引' AS info;