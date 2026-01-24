package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 线路列表VO
 */
@Data
@Schema(description = "线路列表项")
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

    @Schema(description = "出发城市")
    private String cityName;

    @Schema(description = "最低价")
    private BigDecimal minPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "评分")
    private BigDecimal score;
}
