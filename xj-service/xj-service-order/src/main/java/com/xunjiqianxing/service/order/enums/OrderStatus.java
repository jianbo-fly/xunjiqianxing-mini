package com.xunjiqianxing.service.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING_PAY(0, "待支付"),
    BOOKED(1, "已预订"),
    TRAVELING(2, "出行中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REFUND_APPLY(5, "退款申请中"),
    REFUNDED(6, "已退款"),
    CLOSED(7, "已关闭");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static OrderStatus of(Integer code) {
        if (code == null) return null;
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取描述
     */
    public static String getDesc(Integer code) {
        OrderStatus status = of(code);
        return status != null ? status.desc : "";
    }

    /**
     * 允许退款的状态
     */
    public static List<Integer> refundableStatuses() {
        return Arrays.asList(
                BOOKED.code
        );
    }
}
