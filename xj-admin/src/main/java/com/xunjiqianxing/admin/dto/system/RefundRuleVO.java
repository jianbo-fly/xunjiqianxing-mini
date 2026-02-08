package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款规则VO
 */
@Data
@Schema(description = "退款规则VO")
public class RefundRuleVO {

    @Schema(description = "出发前天数")
    private Integer daysBeforeStart;

    @Schema(description = "退款比例（%）")
    private BigDecimal refundRate;
}
