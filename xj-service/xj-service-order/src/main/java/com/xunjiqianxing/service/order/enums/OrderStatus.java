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
    PENDING_CONFIRM(1, "待确认"),
    CONFIRMED(2, "已确认"),
    TRAVELING(3, "出行中"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消"),
    REFUND_APPLY(6, "退款申请中"),
    REFUNDED(7, "已退款"),
    CLOSED(8, "已关闭");

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
                PENDING_CONFIRM.code,
                CONFIRMED.code,
                TRAVELING.code
        );
    }
}
