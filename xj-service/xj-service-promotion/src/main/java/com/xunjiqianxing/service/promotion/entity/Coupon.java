package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
public class Coupon extends BaseEntity {

    /**
     * 券名称
     */
    private String name;

    /**
     * 券类型: 1满减券 2折扣券 3无门槛券
     */
    private Integer type;

    /**
     * 满减门槛金额
     */
    private BigDecimal threshold;

    /**
     * 优惠金额（满减券/无门槛券）
     */
    private BigDecimal amount;

    /**
     * 折扣率（折扣券，如0.9表示9折）
     */
    private BigDecimal discount;

    /**
     * 最高减免金额（折扣券用）
     */
    private BigDecimal maxAmount;

    /**
     * 适用范围: 0全场 1指定线路 2指定分类
     */
    private Integer scope;

    /**
     * 适用线路/分类ID列表，逗号分隔
     */
    private String scopeIds;

    /**
     * 有效期类型: 1固定时间 2领取后N天
     */
    private Integer validType;

    /**
     * 有效期开始时间
     */
    private LocalDateTime validStart;

    /**
     * 有效期结束时间
     */
    private LocalDateTime validEnd;

    /**
     * 领取后有效天数
     */
    private Integer validDays;

    /**
     * 发行总量
     */
    private Integer totalCount;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 每人限领数量
     */
    private Integer perLimit;

    /**
     * 状态: 0停用 1启用
     */
    private Integer status;

    /**
     * 是否仅会员可用
     */
    private Integer memberOnly;

    /**
     * 描述
     */
    private String description;

    private Integer isDeleted;
}
