package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 线路分类 VO
 */
@Data
@Schema(description = "线路分类")
public class RouteCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称（与 product_route.category 一致）")
    private String name;

    @Schema(description = "图标URL")
    private String icon;
}
