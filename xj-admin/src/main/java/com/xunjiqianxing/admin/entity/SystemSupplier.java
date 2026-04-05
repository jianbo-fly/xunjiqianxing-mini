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

    // ==================== 公司信息 ====================

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司地址
     */
    private String companyAddress;

    // ==================== 联系人1 ====================

    /**
     * 联系人1姓名
     */
    private String contact1Name;

    /**
     * 联系人1职务
     */
    private String contact1Title;

    /**
     * 联系人1电话
     */
    private String contact1Phone;

    // ==================== 联系人2（选填）====================

    /**
     * 联系人2姓名
     */
    private String contact2Name;

    /**
     * 联系人2职务
     */
    private String contact2Title;

    /**
     * 联系人2电话
     */
    private String contact2Phone;

    // ==================== 资质证件 ====================

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 旅游业务许可证
     */
    private String tourismLicense;

    /**
     * 法人身份证正面
     */
    private String idCardFront;

    /**
     * 法人身份证反面
     */
    private String idCardBack;

    /**
     * 保险证明
     */
    private String insuranceImage;

    // ==================== 账号信息 ====================

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
