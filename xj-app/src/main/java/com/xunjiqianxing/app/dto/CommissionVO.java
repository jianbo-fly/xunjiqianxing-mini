package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录VO
 */
@Data
@Schema(description = "佣金记录")
public class CommissionVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "佣金比例")
    private BigDecimal commissionRate;

    @Schema(description = "佣金金额")
    private BigDecimal commissionAmount;

    @Schema(description = "状态: 0待结算 1已结算 2已取消")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "结算时间")
    private LocalDateTime settleAt;

    public static String getStatusDesc(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待结算";
            case 1 -> "已结算";
            case 2 -> "已取消";
            default -> "";
        };
    }
}
