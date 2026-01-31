package com.xunjiqianxing.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统管理员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_admin")
public class SystemAdmin extends BaseEntity {

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码(加密)
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色: super_admin/admin
     */
    private String role;

    /**
     * 状态: 0禁用 1正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}
