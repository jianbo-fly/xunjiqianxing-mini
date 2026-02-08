package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领队佣金记录VO
 */
@Data
@Schema(description = "领队佣金记录VO")
public class LeaderCommissionVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "类型: 1带队佣金 2提现 3调整")
    private Integer type;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
