package com.xunjiqianxing.admin.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单备注请求
 */
@Data
@Schema(description = "订单备注请求")
public class OrderRemarkRequest {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @Schema(description = "备注内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "备注内容不能为空")
    private String remark;
}
