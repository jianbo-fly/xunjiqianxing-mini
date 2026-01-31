package com.xunjiqianxing.admin.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计响应
 */
@Data
@Schema(description = "订单统计响应")
public class OrderStatsVO {

    @Schema(description = "待支付订单数")
    private Long pendingPayCount;

    @Schema(description = "待确认订单数")
    private Long pendingConfirmCount;

    @Schema(description = "待出行订单数")
    private Long pendingTravelCount;

    @Schema(description = "出行中订单数")
    private Long travelingCount;

    @Schema(description = "已完成订单数")
    private Long completedCount;

    @Schema(description = "退款中订单数")
    private Long refundingCount;

    @Schema(description = "今日订单数")
    private Long todayOrderCount;

    @Schema(description = "今日销售额")
    private BigDecimal todaySalesAmount;

    @Schema(description = "本月订单数")
    private Long monthOrderCount;

    @Schema(description = "本月销售额")
    private BigDecimal monthSalesAmount;
}
