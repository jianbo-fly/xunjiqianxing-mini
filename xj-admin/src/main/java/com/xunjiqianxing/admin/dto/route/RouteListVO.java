package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 线路列表响应
 */
@Data
@Schema(description = "线路列表响应")
public class RouteListVO {

    @Schema(description = "线路ID")
    private Long id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "出发城市")
    private String cityName;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "最低价")
    private BigDecimal minPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "评分")
    private BigDecimal score;

    @Schema(description = "状态: 0下架 1上架")
    private Integer status;

    @Schema(description = "审核状态: 0待审核 1通过 2驳回")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "套餐数量")
    private Integer packageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
