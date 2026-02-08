package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格设置更新请求
 */
@Data
@Schema(description = "价格设置更新请求")
public class PriceSettingsRequest {

    @NotNull(message = "会员价格不能为空")
    @DecimalMin(value = "0", message = "会员价格不能小于0")
    @Schema(description = "会员价格（元/年）")
    private BigDecimal memberPrice;

    @NotNull(message = "领队价格不能为空")
    @DecimalMin(value = "0", message = "领队价格不能小于0")
    @Schema(description = "领队价格（元）")
    private BigDecimal leaderPrice;
}
