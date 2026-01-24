package com.xunjiqianxing.api.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

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
     * 是否会员
     */
    private Boolean isMember;

    /**
     * 会员到期时间
     */
    private LocalDateTime memberExpireAt;

    /**
     * 是否领队
     */
    private Boolean isLeader;

    /**
     * 是否推广员
     */
    private Boolean isPromoter;

    /**
     * 积分余额
     */
    private Integer points;

    /**
     * 绑定的推广员ID
     */
    private Long bindPromoterId;

    /**
     * 状态
     */
    private Integer status;
}
