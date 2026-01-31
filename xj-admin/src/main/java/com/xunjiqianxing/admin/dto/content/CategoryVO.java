package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类响应
 */
@Data
@Schema(description = "分类响应")
public class CategoryVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "图标URL")
    private String icon;

    @Schema(description = "关联业务类型")
    private String bizType;

    @Schema(description = "状态: 0禁用 1启用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
