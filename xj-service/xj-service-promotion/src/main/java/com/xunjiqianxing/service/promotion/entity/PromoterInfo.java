package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推广员信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_user")
public class PromoterInfo extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 推广码
     */
    private String promoCode;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 等级: 1普通 2银牌 3金牌
     */
    private Integer level;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 累计佣金
     */
    private BigDecimal totalCommission;

    /**
     * 可提现佣金
     */
    private BigDecimal availableCommission;

    /**
     * 已提现金额
     */
    private BigDecimal withdrawnAmount;

    /**
     * 推广人数
     */
    private Integer promotedCount;

    /**
     * 成交订单数
     */
    private Integer orderCount;

    /**
     * 成交金额
     */
    private BigDecimal orderAmount;

    /**
     * 状态: 0待审核 1正常 2禁用
     */
    private Integer status;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

    /**
     * 备注
     */
    private String remark;

    /**
     *
     */
    private Integer isDeleted;
}
