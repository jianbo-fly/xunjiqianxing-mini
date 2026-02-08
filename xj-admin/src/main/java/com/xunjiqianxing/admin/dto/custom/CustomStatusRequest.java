package com.xunjiqianxing.admin.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 定制需求状态更新请求
 */
@Data
@Schema(description = "定制需求状态更新请求")
public class CustomStatusRequest {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 0待处理 1跟进中 2已完成 3已关闭")
    private Integer status;

    @Schema(description = "备注（关闭原因）")
    private String remark;
}
