ALTER TABLE promotion_user ADD COLUMN scan_count INT NOT NULL DEFAULT 0 COMMENT '累计扫码次数' AFTER promo_code;


