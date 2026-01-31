package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户详情响应
 */
@Data
@Schema(description = "用户详情响应")
public class UserDetailVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "微信openid")
    private String openid;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别: 0未知 1男 2女")
    private Integer gender;

    @Schema(description = "是否会员")
    private Integer isMember;

    @Schema(description = "会员到期时间")
    private LocalDateTime memberExpireAt;

    @Schema(description = "是否领队")
    private Integer isLeader;

    @Schema(description = "是否推广员")
    private Integer isPromoter;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "绑定的推广员ID")
    private Long bindpromoterId;

    @Schema(description = "绑定推广员时间")
    private LocalDateTime bindpromoterAt;

    @Schema(description = "状态: 0禁用 1正常")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    // ========== 统计信息 ==========

    @Schema(description = "订单数")
    private Integer orderCount;

    @Schema(description = "消费总额")
    private BigDecimal totalAmount;

    @Schema(description = "出行人数量")
    private Integer travelerCount;
}
