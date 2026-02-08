package com.xunjiqianxing.admin.dto.custom;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 定制需求查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "定制需求查询请求")
public class CustomQueryRequest extends PageQuery {

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "状态: 0待处理 1跟进中 2已完成 3已关闭")
    private Integer status;

    @Schema(description = "出行开始日期-起")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateBegin;

    @Schema(description = "出行开始日期-止")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateEnd;
}
