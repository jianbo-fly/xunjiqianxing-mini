package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定制需求VO
 */
@Data
@Schema(description = "定制需求")
public class CustomDemandVO {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "出行开始日期")
    private LocalDate travelDateStart;

    @Schema(description = "出行结束日期")
    private LocalDate travelDateEnd;

    @Schema(description = "出行天数")
    private String travelDays;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "预算范围")
    private String budget;

    @Schema(description = "需求标签")
    private List<String> requirements;

    @Schema(description = "补充需求")
    private String requirementsText;

    @Schema(description = "状态: 0待处理 1跟进中 2已完成 3已关闭")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
