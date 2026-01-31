package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建分类请求
 */
@Data
@Schema(description = "创建分类请求")
public class CategoryCreateRequest {

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "关联业务类型")
    private String bizType;

    @Schema(description = "排序(越大越前)")
    private Integer sortOrder = 0;
}
