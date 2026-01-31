package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 推广员审核请求
 */
@Data
@Schema(description = "推广员审核请求")
public class PromoterAuditRequest {

    @Schema(description = "推广员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "推广员ID不能为空")
    private Long id;

    @Schema(description = "审核结果: 1通过 2驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核结果不能为空")
    private Integer status;

    @Schema(description = "佣金比例(审核通过时设置)")
    private BigDecimal commissionRate;

    @Schema(description = "等级: 1普通 2银牌 3金牌")
    private Integer level;

    @Schema(description = "审核备注")
    private String remark;
}
