package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_user")
public class UserCoupon extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 券名称（冗余）
     */
    private String couponName;

    /**
     * 券类型: 1满减券 2折扣券 3无门槛券
     */
    private Integer couponType;

    /**
     * 满减门槛
     */
    private BigDecimal threshold;

    /**
     * 优惠金额
     */
    private BigDecimal amount;

    /**
     * 折扣率
     */
    private BigDecimal discount;

    /**
     * 最高减免金额
     */
    private BigDecimal maxAmount;

    /**
     * 有效期开始
     */
    private LocalDateTime validStart;

    /**
     * 有效期结束
     */
    private LocalDateTime validEnd;

    /**
     * 状态: 0未使用 1已使用 2已过期
     */
    private Integer status;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    /**
     * 使用订单号
     */
    private String usedOrderNo;

    /**
     *
     */
    private Integer isDeleted;
}
