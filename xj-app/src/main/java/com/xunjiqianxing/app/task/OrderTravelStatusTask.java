package com.xunjiqianxing.app.task;

import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单出行状态自动更新定时任务
 * <p>
 * 每小时整点执行：
 * 1. 已预订 → 出行中：start_date <= 今天
 * 2. 出行中 → 已完成：end_date < 今天
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTravelStatusTask {

    private final OrderService orderService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateTravelStatus() {
        LocalDate today = LocalDate.now();
        log.info("开始执行订单出行状态更新任务，当前日期：{}", today);

        markTraveling(today);
        markCompleted(today);

        log.info("订单出行状态更新任务执行完毕");
    }

    /**
     * 已预订 → 出行中
     */
    private void markTraveling(LocalDate today) {
        List<OrderMain> orders = orderService.getBookedOrdersToTravel(today);
        if (orders.isEmpty()) {
            log.info("无需更新为出行中的订单");
            return;
        }
        List<Long> ids = orders.stream().map(OrderMain::getId).collect(Collectors.toList());
        int rows = orderService.batchMarkTraveling(ids);
        log.info("已预订 → 出行中：共 {} 笔订单更新成功", rows);
    }

    /**
     * 出行中 → 已完成
     */
    private void markCompleted(LocalDate today) {
        List<OrderMain> orders = orderService.getTravelingOrdersToComplete(today);
        if (orders.isEmpty()) {
            log.info("无需更新为已完成的订单");
            return;
        }
        List<Long> ids = orders.stream().map(OrderMain::getId).collect(Collectors.toList());
        int rows = orderService.batchMarkCompleted(ids);
        log.info("出行中 → 已完成：共 {} 笔订单更新成功", rows);
    }
}
