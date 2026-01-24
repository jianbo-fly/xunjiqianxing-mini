package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员订单VO
 */
@Data
@Schema(description = "会员订单")
public class MemberOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "会员开始日期")
    private LocalDate startDate;

    @Schema(description = "会员结束日期")
    private LocalDate endDate;

    @Schema(description = "购买月数")
    private Integer durationMonths;

    @Schema(description = "状态: 0待支付 1已支付 2已取消")
    private Integer status;
}
