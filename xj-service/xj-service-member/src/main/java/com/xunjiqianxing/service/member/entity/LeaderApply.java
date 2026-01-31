package com.xunjiqianxing.service.member.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 领队申请实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "leader_apply", autoResultMap = true)
public class LeaderApply extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 擅长领域
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> expertise;

    /**
     * 个人简介
     */
    private String intro;

    /**
     * 资质证书图片
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> certificateImages;

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
     * 支付状态: 0未支付 1已支付
     */
    private Integer payStatus;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;
}
