package com.xunjiqianxing.app.task;

import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.product.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动关闭定时任务
 * 每分钟扫描一次超时未支付订单，自动关闭并释放锁定库存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;
    private final RouteService routeService;

    @Scheduled(fixedDelay = 60_000)
    public void closeExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<OrderMain> expiredOrders = orderService.getExpiredPendingOrders(now);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("发现 {} 笔超时未支付订单，开始自动关闭", expiredOrders.size());

        for (OrderMain order : expiredOrders) {
            try {
                boolean closed = orderService.closeExpiredOrder(order.getId());
                if (closed) {
                    // 释放锁定库存
                    int quantity = (order.getAdultCount() != null ? order.getAdultCount() : 0)
                            + (order.getChildCount() != null ? order.getChildCount() : 0);
                    if (order.getSkuId() != null && order.getStartDate() != null && quantity > 0) {
                        try {
                            routeService.releaseStock(order.getSkuId(), order.getStartDate(), quantity);
                        } catch (Exception e) {
                            log.warn("释放库存失败: orderId={}, error={}", order.getId(), e.getMessage());
                        }
                    }
                    log.info("订单自动关闭成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
                }
            } catch (Exception e) {
                log.error("自动关闭订单失败: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }
    }
}
