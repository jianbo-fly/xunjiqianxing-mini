package com.xunjiqianxing.app.listener;

import com.xunjiqianxing.service.member.service.MemberService;
import com.xunjiqianxing.service.message.service.MessageService;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.payment.entity.PaymentRecord;
import com.xunjiqianxing.service.payment.service.impl.PaymentServiceImpl;
import com.xunjiqianxing.service.product.service.RouteService;
import com.xunjiqianxing.service.promotion.service.PromoterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 支付事件监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    private final RouteService routeService;
    private final MemberService memberService;
    private final PromoterService promoterService;
    private final MessageService messageService;

    /**
     * 处理支付成功事件
     */
    @Async
    @EventListener
    public void onPaymentSuccess(PaymentServiceImpl.PaymentSuccessEvent event) {
        PaymentRecord record = event.getPaymentRecord();
        log.info("处理支付成功事件: bizType={}, bizNo={}", record.getBizType(), record.getBizNo());

        try {
            switch (record.getBizType()) {
                case "order" -> handleOrderPaySuccess(record);
                case "member" -> handleMemberPaySuccess(record);
                default -> log.warn("未知业务类型: {}", record.getBizType());
            }
        } catch (Exception e) {
            log.error("处理支付成功事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理订单支付成功
     */
    private void handleOrderPaySuccess(PaymentRecord record) {
        boolean success = orderService.paySuccess(record.getBizNo(), record.getTransactionId());
        if (success) {
            log.info("订单支付状态更新成功: orderNo={}", record.getBizNo());

            // 确认库存（sold += quantity, locked -= quantity）
            try {
                OrderMain orderForStock = orderService.getByOrderNo(record.getBizNo());
                if (orderForStock != null) {
                    int quantity = (orderForStock.getAdultCount() != null ? orderForStock.getAdultCount() : 0)
                            + (orderForStock.getChildCount() != null ? orderForStock.getChildCount() : 0);
                    routeService.confirmStock(orderForStock.getSkuId(), orderForStock.getStartDate(), quantity);
                }
            } catch (Exception e) {
                log.warn("确认库存失败: orderNo={}, error={}", record.getBizNo(), e.getMessage());
            }

            // 记录推广员佣金
            try {
                promoterService.recordCommission(
                        record.getUserId(),
                        record.getBizNo(),
                        record.getAmount()
                );
            } catch (Exception e) {
                log.warn("记录推广员佣金失败: {}", e.getMessage());
            }

            // 发送支付成功消息
            try {
                OrderMain order = orderService.getByOrderNo(record.getBizNo());
                String productName = order != null ? order.getProductName() : "";
                String content = String.format("您已成功支付订单「%s」，金额¥%s，预订成功请准时出行。",
                        productName, record.getAmount().stripTrailingZeros().toPlainString());
                messageService.sendMessage(
                        record.getUserId(),
                        "payment_success",
                        "order",
                        "支付成功",
                        content,
                        "order",
                        order != null ? order.getId() : null,
                        record.getBizNo(),
                        "/pages/order/detail/index?orderNo=" + record.getBizNo()
                );
            } catch (Exception e) {
                log.warn("发送支付成功消息失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 处理会员支付成功
     */
    private void handleMemberPaySuccess(PaymentRecord record) {
        boolean success = memberService.paySuccess(record.getBizNo());
        if (success) {
            log.info("会员订单支付状态更新成功: orderNo={}", record.getBizNo());
        }
    }
}
