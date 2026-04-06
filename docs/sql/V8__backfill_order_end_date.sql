-- 回填 order_main 中缺失的 days 和 end_date
-- 逻辑：从 product_route.days 取行程天数，end_date = start_date + days - 1
UPDATE order_main o
    JOIN product_route pr ON pr.product_id = o.product_id
SET
    o.days     = pr.days,
    o.end_date = DATE_ADD(o.start_date, INTERVAL (pr.days - 1) DAY)
WHERE o.is_deleted = 0
  AND o.start_date IS NOT NULL
  AND pr.days IS NOT NULL
  AND pr.days > 0
  AND (o.end_date IS NULL OR o.days IS NULL);
