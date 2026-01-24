package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券VO
 */
@Data
@Schema(description = "优惠券信息")
public class CouponVO {

    @Schema(description = "优惠券ID")
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

    @Schema(description = "剩余数量")
    private Integer remainCount;

    @Schema(description = "是否仅会员可用")
    private Boolean memberOnly;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "优惠描述")
    private String discountDesc;

    @Schema(description = "门槛描述")
    private String thresholdDesc;

    public static String getTypeDesc(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "满减券";
            case 2 -> "折扣券";
            case 3 -> "无门槛券";
            default -> "";
        };
    }

    public static String getDiscountDesc(Integer type, BigDecimal amount, BigDecimal discount) {
        if (type == null) return "";
        return switch (type) {
            case 1, 3 -> "减" + amount + "元";
            case 2 -> (discount.multiply(BigDecimal.TEN)).stripTrailingZeros().toPlainString() + "折";
            default -> "";
        };
    }

    public static String getThresholdDesc(Integer type, BigDecimal threshold) {
        if (type == 3) return "无门槛";
        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) == 0) return "无门槛";
        return "满" + threshold.stripTrailingZeros().toPlainString() + "元可用";
    }
}
