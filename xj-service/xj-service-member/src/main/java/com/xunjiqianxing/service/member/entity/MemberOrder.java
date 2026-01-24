package com.xunjiqianxing.service.member.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员订单表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_order")
public class MemberOrder extends BaseEntity {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 会员开始日期
     */
    private LocalDate startDate;

    /**
     * 会员结束日期
     */
    private LocalDate endDate;

    /**
     * 购买月数
     */
    private Integer durationMonths;

    /**
     * 状态: 0待支付 1已支付 2已取消
     */
    private Integer status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;
}
