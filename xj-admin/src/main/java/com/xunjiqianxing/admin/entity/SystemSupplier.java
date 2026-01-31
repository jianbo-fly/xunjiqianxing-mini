package com.xunjiqianxing.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system_supplier", autoResultMap = true)
public class SystemSupplier extends BaseEntity {

    /**
     * 商家名称
     */
    private String name;

    /**
     * Logo
     */
    private String logo;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 简介
     */
    private String intro;

    /**
     * 资质证书图片
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> licenseImages;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码(加密)
     */
    private String password;

    /**
     * 状态: 0禁用 1正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}
