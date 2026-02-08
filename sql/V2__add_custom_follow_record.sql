-- 定制需求跟进记录表
CREATE TABLE IF NOT EXISTS `custom_follow_record` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `demand_id` BIGINT NOT NULL COMMENT '定制需求ID',
    `content` TEXT NOT NULL COMMENT '跟进内容',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(50) COMMENT '操作人名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_demand_id` (`demand_id`),
    INDEX `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定制需求跟进记录表';

-- 系统配置表（如果不存在则创建）
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '配置类型: string, number, json, boolean',
    `remark` VARCHAR(255) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 初始化系统配置
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `remark`) VALUES
(1, 'service_phone', '400-888-8888', 'string', '客服电话'),
(2, 'wecom_service_url', '', 'string', '企微客服链接'),
(3, 'payment_timeout', '30', 'number', '支付超时时间（分钟）'),
(4, 'child_age_limit', '12', 'number', '儿童年龄上限'),
(5, 'order_points_rate', '1', 'number', '下单积分比例'),
(6, 'promoter_points_rate', '5', 'number', '推广积分比例'),
(7, 'refund_rules', '[{"daysBeforeStart":7,"refundRate":100},{"daysBeforeStart":3,"refundRate":70},{"daysBeforeStart":1,"refundRate":50}]', 'json', '退款规则'),
(8, 'member_price', '99', 'number', '会员价格（元/年）'),
(9, 'leader_apply_fee', '2000', 'number', '领队申请费用（元）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
