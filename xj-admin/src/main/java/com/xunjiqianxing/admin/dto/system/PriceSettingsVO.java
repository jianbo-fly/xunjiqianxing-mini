package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 价格设置VO
 */
@Data
@Schema(description = "价格设置VO")
public class PriceSettingsVO {

    @Schema(description = "会员价格（元/年）")
    private BigDecimal memberPrice;

    @Schema(description = "领队价格（元）")
    private BigDecimal leaderPrice;
}
