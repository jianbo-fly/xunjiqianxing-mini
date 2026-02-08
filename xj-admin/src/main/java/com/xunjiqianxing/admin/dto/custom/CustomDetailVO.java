package com.xunjiqianxing.admin.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定制需求详情VO
 */
@Data
@Schema(description = "定制需求详情VO")
public class CustomDetailVO {

    @Schema(description = "需求ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "出行开始日期")
    private LocalDate startDate;

    @Schema(description = "出行结束日期")
    private LocalDate endDate;

    @Schema(description = "出行时间类型")
    private String dateType;

    @Schema(description = "出行天数")
    private String days;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "预算")
    private String budget;

    @Schema(description = "需求标签")
    private List<String> requirements;

    @Schema(description = "补充需求")
    private String requirementsText;

    @Schema(description = "状态: 0待处理 1跟进中 2已完成 3已关闭")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "跟进记录")
    private List<FollowRecordVO> followRecords;

    @Schema(description = "提交时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
