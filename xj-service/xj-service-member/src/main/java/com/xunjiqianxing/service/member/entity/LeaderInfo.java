package com.xunjiqianxing.service.member.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领队信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "leader_info", autoResultMap = true)
public class LeaderInfo extends BaseEntity {

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
     * 头像
     */
    private String avatar;

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
     * 带队次数
     */
    private Integer tripCount;

    /**
     * 累计佣金
     */
    private BigDecimal totalCommission;

    /**
     * 状态: 0禁用 1正常
     */
    private Integer status;
}
