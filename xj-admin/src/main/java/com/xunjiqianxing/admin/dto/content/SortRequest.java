package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 排序请求
 */
@Data
@Schema(description = "排序请求")
public class SortRequest {

    @Schema(description = "排序ID列表(按顺序)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排序列表不能为空")
    private List<Long> ids;
}
