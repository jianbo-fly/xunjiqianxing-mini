package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 定制需求请求
 */
@Data
@Schema(description = "定制需求请求")
public class CustomDemandRequest {

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", required = true)
    private String phone;

    @NotBlank(message = "目的地不能为空")
    @Schema(description = "目的地", required = true)
    private String destination;

    @Schema(description = "出行开始日期")
    private LocalDate travelDateStart;

    @Schema(description = "出行结束日期")
    private LocalDate travelDateEnd;

    @Schema(description = "出行时间类型: exact具体日期 flexible灵活")
    private String travelDateType;

    @Schema(description = "出行天数")
    private String travelDays;

    @NotNull(message = "成人数不能为空")
    @Schema(description = "成人数", required = true)
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "预算范围")
    private String budget;

    @Schema(description = "需求标签")
    private List<String> requirements;

    @Schema(description = "补充需求")
    private String requirementsText;
}
