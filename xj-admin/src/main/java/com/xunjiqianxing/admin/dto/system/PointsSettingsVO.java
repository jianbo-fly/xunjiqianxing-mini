package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分规则设置VO
 */
@Data
@Schema(description = "积分规则设置VO")
public class PointsSettingsVO {

    @Schema(description = "下单积分比例（%）")
    private BigDecimal orderPointsRate;

    @Schema(description = "推广积分比例（%）")
    private BigDecimal promoterPointsRate;
}
