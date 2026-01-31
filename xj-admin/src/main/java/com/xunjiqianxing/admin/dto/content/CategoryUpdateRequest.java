package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新分类请求
 */
@Data
@Schema(description = "更新分类请求")
public class CategoryUpdateRequest {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "关联业务类型")
    private String bizType;

    @Schema(description = "排序(越大越前)")
    private Integer sortOrder;
}
