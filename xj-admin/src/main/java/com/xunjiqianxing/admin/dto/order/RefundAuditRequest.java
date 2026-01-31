package com.xunjiqianxing.admin.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款审核请求
 */
@Data
@Schema(description = "退款审核请求")
public class RefundAuditRequest {

    @Schema(description = "退款ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退款ID不能为空")
    private Long id;

    @Schema(description = "审核结果: 1通过 2驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核结果不能为空")
    private Integer status;

    @Schema(description = "实际退款金额(通过时可调整)")
    private BigDecimal actualAmount;

    @Schema(description = "审核备注")
    private String auditRemark;
}
