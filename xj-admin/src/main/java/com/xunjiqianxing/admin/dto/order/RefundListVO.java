package com.xunjiqianxing.admin.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款列表响应
 */
@Data
@Schema(description = "退款列表响应")
public class RefundListVO {

    @Schema(description = "退款ID")
    private Long id;

    @Schema(description = "退款编号")
    private String refundNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "申请退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "实际退款金额")
    private BigDecimal actualAmount;

    @Schema(description = "退款比例(%)")
    private Integer refundRatio;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "申请时间")
    private LocalDateTime createdAt;
}
