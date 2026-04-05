CREATE TABLE `promoter_scan_record` (
                                        `id`               BIGINT       NOT NULL COMMENT '主键',
                                        `promoter_id`      BIGINT       NOT NULL COMMENT '推广员ID',
                                        `promoter_user_id` BIGINT       NOT NULL COMMENT '推广员用户ID',
                                        `promo_code`       VARCHAR(32)  NOT NULL COMMENT '推广码',
                                        `scan_user_id`     BIGINT       DEFAULT NULL COMMENT '扫码用户ID（未登录时为空）',
                                        `created_at`       DATETIME     DEFAULT NULL COMMENT '扫码时间',
                                        `updated_at`       DATETIME     DEFAULT NULL COMMENT '更新时间',
                                        PRIMARY KEY (`id`),
                                        KEY `idx_promoter_user_id` (`promoter_user_id`),
                                        KEY `idx_promo_code` (`promo_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广员扫码记录';




