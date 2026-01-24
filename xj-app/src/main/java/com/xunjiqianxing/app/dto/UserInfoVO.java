package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别: 0未知 1男 2女")
    private Integer gender;

    @Schema(description = "是否会员")
    private Boolean isMember;

    @Schema(description = "会员到期时间")
    private LocalDateTime memberExpireAt;

    @Schema(description = "是否领队")
    private Boolean isLeader;

    @Schema(description = "是否推广员")
    private Boolean isPromoter;

    @Schema(description = "积分余额")
    private Integer points;
}
