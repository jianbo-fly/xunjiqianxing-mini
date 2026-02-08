package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分规则设置更新请求
 */
@Data
@Schema(description = "积分规则设置更新请求")
public class PointsSettingsRequest {

    @NotNull(message = "下单积分比例不能为空")
    @DecimalMin(value = "0", message = "下单积分比例不能小于0")
    @Schema(description = "下单积分比例（%）")
    private BigDecimal orderPointsRate;

    @NotNull(message = "推广积分比例不能为空")
    @DecimalMin(value = "0", message = "推广积分比例不能小于0")
    @Schema(description = "推广积分比例（%）")
    private BigDecimal promoterPointsRate;
}
