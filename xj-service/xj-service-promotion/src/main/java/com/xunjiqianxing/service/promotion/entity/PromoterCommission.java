package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推广员佣金记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_commission")
public class PromoterCommission extends BaseEntity {

    /**
     * 推广员ID
     */
    private Long promoterId;

    /**
     * 推广员用户ID
     */
    private Long promoterUserId;

    /**
     * 被推广用户ID
     */
    private Long fromUserId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 佣金金额
     */
    private BigDecimal commissionAmount;

    /**
     * 状态: 0待结算 1已结算 2已取消
     */
    private Integer status;

    /**
     * 结算时间
     */
    private LocalDateTime settleAt;

    /**
     * 备注
     */
    private String remark;
}
