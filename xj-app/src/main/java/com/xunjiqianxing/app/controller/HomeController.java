package com.xunjiqianxing.app.controller;

import com.xunjiqianxing.app.dto.BannerVO;
import com.xunjiqianxing.app.dto.HomeDataVO;
import com.xunjiqianxing.app.dto.ProductVO;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.content.entity.Banner;
import com.xunjiqianxing.service.content.service.BannerService;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页接口
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "首页接口", description = "首页数据相关接口")
public class HomeController {

    private final BannerService bannerService;
    private final ProductService productService;

    /**
     * 获取首页数据
     */
    @GetMapping("/data")
    @Operation(summary = "获取首页数据", description = "获取轮播图和推荐线路")
    public Result<HomeDataVO> getHomeData() {
        // 获取轮播图
        List<Banner> banners = bannerService.getActiveBanners();
        List<BannerVO> bannerVOList = banners.stream()
                .map(this::toBannerVO)
                .collect(Collectors.toList());

        // 获取推荐线路
        List<ProductMain> routes = productService.getRecommendProducts(10);
        List<ProductVO> routeVOList = routes.stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());

        return Result.success(HomeDataVO.builder()
                .banners(bannerVOList)
                .recommendRoutes(routeVOList)
                .build());
    }

    /**
     * 获取轮播图列表
     */
    @GetMapping("/banners")
    @Operation(summary = "获取轮播图列表")
    public Result<List<BannerVO>> getBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        List<BannerVO> voList = banners.stream()
                .map(this::toBannerVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取推荐线路列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "获取推荐线路列表")
    public Result<List<ProductVO>> getRecommendRoutes() {
        List<ProductMain> routes = productService.getRecommendProducts(10);
        List<ProductVO> voList = routes.stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    private BannerVO toBannerVO(Banner banner) {
        BannerVO vo = new BannerVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }

    private ProductVO toProductVO(ProductMain product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }
}
