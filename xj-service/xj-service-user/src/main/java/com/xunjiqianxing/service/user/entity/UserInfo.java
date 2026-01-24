package com.xunjiqianxing.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_info")
public class UserInfo extends BaseEntity {

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 微信unionid
     */
    private String unionid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别: 0未知 1男 2女
     */
    private Integer gender;

    /**
     * 是否会员
     */
    private Integer isMember;

    /**
     * 会员到期时间
     */
    private LocalDateTime memberExpireAt;

    /**
     * 是否领队
     */
    private Integer isLeader;

    /**
     * 是否推广员
     */
    private Integer isPromoter;

    /**
     * 积分余额
     */
    private Integer points;

    /**
     * 绑定的推广员ID
     */
    private Long bindpromoterId;

    /**
     * 绑定推广员时间
     */
    private LocalDateTime bindpromoterAt;

    /**
     * 状态: 0禁用 1正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}
