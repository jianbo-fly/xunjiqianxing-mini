package com.xunjiqianxing.admin.dto.route;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 线路查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "线路查询请求")
public class RouteQueryRequest extends PageQuery {

    @Schema(description = "关键词(名称/副标题)")
    private String keyword;

    @Schema(description = "分类: domestic国内游 overseas出境游")
    private String category;

    @Schema(description = "出发城市")
    private String departureCity;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "状态: 0下架 1上架")
    private Integer status;

    @Schema(description = "审核状态: 0待审核 1通过 2驳回")
    private Integer auditStatus;
}
