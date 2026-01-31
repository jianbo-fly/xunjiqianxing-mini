package com.xunjiqianxing.service.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_refund")
public class OrderRefund extends BaseEntity {

    /**
     * 退款编号
     */
    private String refundNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 申请退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 实际退款金额
     */
    private BigDecimal actualAmount;

    /**
     * 退款比例(%)
     */
    private Integer refundRatio;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 状态: 0待审核 1已通过 2已驳回
     */
    private Integer status;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人
     */
    private Long auditBy;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 退款到账时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款流水号
     */
    private String refundTradeNo;
}
