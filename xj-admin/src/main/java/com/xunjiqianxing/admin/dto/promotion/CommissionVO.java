package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录响应
 */
@Data
@Schema(description = "佣金记录响应")
public class CommissionVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "推广员ID")
    private Long promoterId;

    @Schema(description = "被推广用户ID")
    private Long fromUserId;

    @Schema(description = "用户昵称")
    private String fromUserNickname;

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

    @Schema(description = "结算时间")
    private LocalDateTime settleAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
