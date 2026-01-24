package com.xunjiqianxing.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户出行人信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_traveler")
public class UserTraveler extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 证件类型: 1身份证 2护照 3港澳通行证 4台湾通行证
     */
    private Integer idType;

    /**
     * 证件号码
     */
    private String idNo;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别: 1男 2女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 是否默认: 0否 1是
     */
    private Integer isDefault;

    /**
     * 紧急联系人姓名
     */
    private String emergencyName;

    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除: 0否 1是
     */
    private Integer isDeleted;
}
