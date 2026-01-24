package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 首页数据VO
 */
@Data
@Builder
@Schema(description = "首页数据")
public class HomeDataVO {

    @Schema(description = "轮播图列表")
    private List<BannerVO> banners;

    @Schema(description = "推荐线路列表")
    private List<ProductVO> recommendRoutes;
}
