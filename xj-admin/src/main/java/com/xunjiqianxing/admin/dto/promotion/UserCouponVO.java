package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券响应
 */
@Data
@Schema(description = "用户优惠券响应")
public class UserCouponVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "券名称")
    private String couponName;

    @Schema(description = "券类型")
    private Integer couponType;

    @Schema(description = "券类型描述")
    private String couponTypeDesc;

    @Schema(description = "满减门槛")
    private BigDecimal threshold;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "状态: 0未使用 1已使用 2已过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "使用时间")
    private LocalDateTime usedAt;

    @Schema(description = "使用订单号")
    private String usedOrderNo;

    @Schema(description = "领取时间")
    private LocalDateTime createdAt;
}
