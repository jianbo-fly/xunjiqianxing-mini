package com.xunjiqianxing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    /**
     * 待支付
     */
    PENDING_PAY("pending_pay", "待支付"),

    /**
     * 待确认
     */
    PENDING_CONFIRM("pending_confirm", "待确认"),

    /**
     * 已确认
     */
    CONFIRMED("confirmed", "已确认"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("cancelled", "已取消"),

    /**
     * 退款中
     */
    REFUNDING("refunding", "退款中"),

    /**
     * 已退款
     */
    REFUNDED("refunded", "已退款"),

    /**
     * 已拒绝
     */
    REJECTED("rejected", "已拒绝"),

    ;

    private final String code;
    private final String name;

    /**
     * 根据code获取枚举
     */
    public static OrderStatus of(String code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING_PAY;
    }

    /**
     * 是否可以申请退款
     */
    public boolean canRefund() {
        return this == PENDING_CONFIRM || this == CONFIRMED;
    }

    /**
     * 是否可以支付
     */
    public boolean canPay() {
        return this == PENDING_PAY;
    }

    /**
     * 是否已结束
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED || this == REJECTED;
    }
}
