package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 推广员信息VO
 */
@Data
@Schema(description = "推广员信息")
public class PromoterVO {

    @Schema(description = "推广码")
    private String promoCode;

    @Schema(description = "等级: 1普通 2银牌 3金牌")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "佣金比例")
    private BigDecimal commissionRate;

    @Schema(description = "佣金比例描述")
    private String commissionRateDesc;

    @Schema(description = "累计佣金")
    private BigDecimal totalCommission;

    @Schema(description = "可提现佣金")
    private BigDecimal availableCommission;

    @Schema(description = "已提现金额")
    private BigDecimal withdrawnAmount;

    @Schema(description = "推广人数")
    private Integer promotedCount;

    @Schema(description = "成交订单数")
    private Integer orderCount;

    @Schema(description = "成交金额")
    private BigDecimal orderAmount;

    @Schema(description = "状态: 0待审核 1正常 2禁用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    public static String getLevelName(Integer level) {
        if (level == null) return "";
        return switch (level) {
            case 1 -> "普通推广员";
            case 2 -> "银牌推广员";
            case 3 -> "金牌推广员";
            default -> "";
        };
    }

    public static String getStatusDesc(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "正常";
            case 2 -> "已禁用";
            default -> "";
        };
    }
}
