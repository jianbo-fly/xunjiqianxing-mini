package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券VO
 */
@Data
@Schema(description = "用户优惠券")
public class UserCouponVO {

    @Schema(description = "用户优惠券ID")
    private Long id;

    @Schema(description = "券名称")
    private String name;

    @Schema(description = "券类型: 1满减券 2折扣券 3无门槛券")
    private Integer type;

    @Schema(description = "券类型描述")
    private String typeDesc;

    @Schema(description = "满减门槛金额")
    private BigDecimal threshold;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "折扣率")
    private BigDecimal discount;

    @Schema(description = "最高减免金额")
    private BigDecimal maxAmount;

    @Schema(description = "有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "状态: 0未使用 1已使用 2已过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "优惠描述")
    private String discountDesc;

    @Schema(description = "门槛描述")
    private String thresholdDesc;

    @Schema(description = "预计可减金额（用于订单选择优惠券时）")
    private BigDecimal estimatedDiscount;

    public static String getStatusDesc(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "未使用";
            case 1 -> "已使用";
            case 2 -> "已过期";
            default -> "";
        };
    }
}
