
ALTER TABLE product_route ADD COLUMN days INT COMMENT '行程天数' AFTER destination;
ALTER TABLE product_route ADD INDEX idx_days (days);
