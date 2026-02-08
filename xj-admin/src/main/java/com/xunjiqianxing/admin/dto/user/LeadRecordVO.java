package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 带队记录VO
 */
@Data
@Schema(description = "带队记录VO")
public class LeadRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "线路名称")
    private String routeName;

    @Schema(description = "出发日期")
    private LocalDate startDate;

    @Schema(description = "团队人数")
    private Integer peopleCount;

    @Schema(description = "佣金")
    private BigDecimal commission;

    @Schema(description = "状态: 0待出行 1出行中 2已完成")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
