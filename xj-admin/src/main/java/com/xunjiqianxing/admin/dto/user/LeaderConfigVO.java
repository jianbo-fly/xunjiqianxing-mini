package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领队配置VO
 */
@Data
@Schema(description = "领队配置VO")
public class LeaderConfigVO {

    @Schema(description = "领队价格（申请费用）")
    private BigDecimal price;

    @Schema(description = "佣金比例（百分比）")
    private BigDecimal commissionRate;
}
