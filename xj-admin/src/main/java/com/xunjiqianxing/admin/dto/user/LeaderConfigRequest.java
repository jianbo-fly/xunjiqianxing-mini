package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领队配置更新请求
 */
@Data
@Schema(description = "领队配置更新请求")
public class LeaderConfigRequest {

    @NotNull(message = "领队价格不能为空")
    @DecimalMin(value = "0", message = "领队价格不能小于0")
    @Schema(description = "领队价格（申请费用）")
    private BigDecimal price;

    @NotNull(message = "佣金比例不能为空")
    @DecimalMin(value = "0", message = "佣金比例不能小于0")
    @Schema(description = "佣金比例（百分比）")
    private BigDecimal commissionRate;
}
