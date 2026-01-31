package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 线路审核请求
 */
@Data
@Schema(description = "线路审核请求")
public class RouteAuditRequest {

    @Schema(description = "线路ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "线路ID不能为空")
    private Long id;

    @Schema(description = "审核结果: 1通过 2驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    @Schema(description = "审核备注(驳回时必填)")
    private String auditRemark;
}
