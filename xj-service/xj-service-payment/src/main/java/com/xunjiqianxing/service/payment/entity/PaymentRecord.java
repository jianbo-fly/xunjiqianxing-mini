package com.xunjiqianxing.service.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 业务类型: order/member/leader
     */
    private String bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 业务编号
     */
    private String bizNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付渠道: wechat
     */
    private String payChannel;

    /**
     * 支付方式: jsapi/native/app
     */
    private String payType;

    /**
     * 状态: 0待支付 1已支付 2已关闭
     */
    private Integer status;

    /**
     * 预支付ID
     */
    private String prepayId;

    /**
     * 微信支付订单号
     */
    private String transactionId;

    /**
     * 支付成功时间
     */
    private LocalDateTime payTime;

    /**
     * 回调原始数据
     */
    private String notifyData;

    /**
     * 超时时间
     */
    private LocalDateTime expireAt;
}
