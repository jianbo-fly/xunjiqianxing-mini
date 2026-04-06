-- 行程天数字段（与 itinerary JSON 数组长度保持一致，用于筛选）
ALTER TABLE product_route
    ADD COLUMN days INT NULL COMMENT '行程天数' AFTER destination;

ALTER TABLE product_route
    ADD INDEX idx_days (days);

-- 回填已有数据：用 itinerary JSON 数组长度推断天数
UPDATE product_route
SET days = JSON_LENGTH(itinerary)
WHERE itinerary IS NOT NULL
  AND JSON_LENGTH(itinerary) > 0
  AND days IS NULL;
