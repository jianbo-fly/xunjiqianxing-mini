package com.xunjiqianxing.admin.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单确认/驳回请求
 */
@Data
@Schema(description = "订单确认/驳回请求")
public class OrderConfirmRequest {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @Schema(description = "操作类型: confirm确认 reject驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "操作类型不能为空")
    private String action;

    @Schema(description = "驳回原因(驳回时必填)")
    private String rejectReason;

    @Schema(description = "备注")
    private String remark;
}
