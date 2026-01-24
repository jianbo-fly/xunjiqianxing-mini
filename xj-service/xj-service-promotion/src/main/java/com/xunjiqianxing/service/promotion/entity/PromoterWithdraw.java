package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推广员提现记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_withdraw")
public class PromoterWithdraw extends BaseEntity {

    /**
     * 推广员ID
     */
    private Long promoterId;

    /**
     * 推广员用户ID
     */
    private Long promoterUserId;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 提现方式: 1微信 2支付宝 3银行卡
     */
    private Integer withdrawType;

    /**
     * 收款账号
     */
    private String account;

    /**
     * 收款人姓名
     */
    private String accountName;

    /**
     * 状态: 0待审核 1审核通过 2已打款 3已拒绝
     */
    private Integer status;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

    /**
     * 打款时间
     */
    private LocalDateTime payAt;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 备注
     */
    private String remark;
}
