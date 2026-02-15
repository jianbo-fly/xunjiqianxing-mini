-- 修复 order_traveler 表缺少 updated_at 字段的问题
ALTER TABLE `order_traveler`
ADD COLUMN `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
AFTER `created_at`;

-- 同时更新 init.sql 中的表结构也需要修改（这里只是记录）
-- 已同步修改 docs/sql/init.sql 中的 order_traveler 表定义
