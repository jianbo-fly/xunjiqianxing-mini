package com.xunjiqianxing.app.dto;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 线路列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "线路列表查询参数")
public class RouteListQuery extends PageQuery {

    @Schema(description = "分类: domestic国内游 overseas出境游")
    private String category;

    @Schema(description = "出发城市编码")
    private String departureCity;

    @Schema(description = "关键词搜索")
    private String keyword;
}
