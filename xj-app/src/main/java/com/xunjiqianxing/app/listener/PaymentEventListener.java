package com.xunjiqianxing.app.listener;

import com.xunjiqianxing.service.member.service.MemberService;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.payment.entity.PaymentRecord;
import com.xunjiqianxing.service.payment.service.impl.PaymentServiceImpl;
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
    private final MemberService memberService;
    private final PromoterService promoterService;

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
