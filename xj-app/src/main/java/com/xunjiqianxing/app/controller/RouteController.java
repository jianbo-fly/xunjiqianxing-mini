package com.xunjiqianxing.app.controller;

import com.xunjiqianxing.app.dto.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductRoute;
import com.xunjiqianxing.service.product.entity.ProductSku;
import com.xunjiqianxing.service.product.service.ProductService;
import com.xunjiqianxing.service.product.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 线路接口
 */
@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
@Tag(name = "线路接口", description = "跟团游线路相关接口")
public class RouteController {

    private final RouteService routeService;
    private final ProductService productService;

    /**
     * 线路列表
     */
    @GetMapping("/list")
    @Operation(summary = "线路列表", description = "分页查询线路列表")
    public Result<PageResult<RouteListVO>> list(RouteListQuery query) {
        PageResult<ProductMain> pageResult = routeService.pageRoutes(
                query, query.getCategory(), query.getDepartureCity(), query.getKeyword()
        );

        List<RouteListVO> voList = pageResult.getList().stream()
                .map(this::toRouteListVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(voList, pageResult.getTotal(),
                pageResult.getPage(), pageResult.getPageSize()));
    }

    /**
     * 线路详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "线路详情", description = "获取线路详细信息")
    public Result<RouteDetailVO> detail(
            @Parameter(description = "线路ID") @PathVariable Long id) {

        ProductMain product = routeService.getRouteById(id);
        if (product == null) {
            throw new BizException("线路不存在");
        }

        // 增加浏览量
        productService.increaseViewCount(id);

        // 获取扩展信息
        ProductRoute routeExt = routeService.getRouteExtById(id);

        RouteDetailVO vo = toRouteDetailVO(product, routeExt);
        return Result.success(vo);
    }

    /**
     * 线路套餐列表
     */
    @GetMapping("/{id}/packages")
    @Operation(summary = "线路套餐列表", description = "获取线路的所有套餐")
    public Result<List<RoutePackageVO>> packages(
            @Parameter(description = "线路ID") @PathVariable Long id) {

        List<ProductSku> skuList = routeService.getRoutePackages(id);
        List<RoutePackageVO> voList = skuList.stream()
                .map(this::toRoutePackageVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 套餐详情
     */
    @GetMapping("/package/{id}")
    @Operation(summary = "套餐详情", description = "获取套餐详细信息")
    public Result<RoutePackageVO> packageDetail(
            @Parameter(description = "套餐ID") @PathVariable Long id) {

        ProductSku sku = routeService.getPackageById(id);
        if (sku == null) {
            throw new BizException("套餐不存在");
        }

        return Result.success(toRoutePackageVO(sku));
    }

    /**
     * 套餐价格日历
     */
    @GetMapping("/package/{id}/calendar")
    @Operation(summary = "套餐价格日历", description = "获取套餐的价格日历")
    public Result<List<PriceCalendarVO>> calendar(
            @Parameter(description = "套餐ID") @PathVariable Long id,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        List<ProductPriceStock> priceStockList = routeService.getPackageCalendar(id, startDate, endDate);
        List<PriceCalendarVO> voList = priceStockList.stream()
                .map(this::toPriceCalendarVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    // ========== 转换方法 ==========

    private RouteListVO toRouteListVO(ProductMain product) {
        RouteListVO vo = new RouteListVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    private RouteDetailVO toRouteDetailVO(ProductMain product, ProductRoute routeExt) {
        RouteDetailVO vo = new RouteDetailVO();
        BeanUtils.copyProperties(product, vo);

        if (routeExt != null) {
            vo.setCategory(routeExt.getCategory());
            vo.setDepartureCity(routeExt.getDepartureCity());
            vo.setDestination(routeExt.getDestination());
            vo.setCostExclude(routeExt.getCostExclude());
            vo.setCostInclude(routeExt.getCostInclude());
            vo.setBookingNotice(routeExt.getBookingNotice());
            vo.setTips(routeExt.getTips());

            // 转换行程数据 (处理 LinkedHashMap 反序列化问题)
            if (routeExt.getItinerary() != null && !routeExt.getItinerary().isEmpty()) {
                List<RouteDetailVO.ItineraryDay> itinerary = routeExt.getItinerary().stream()
                        .map(dayObj -> {
                            RouteDetailVO.ItineraryDay dayVO = new RouteDetailVO.ItineraryDay();
                            if (dayObj instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> dayMap = (java.util.Map<String, Object>) dayObj;
                                dayVO.setDay(dayMap.get("day") != null ? Integer.valueOf(dayMap.get("day").toString()) : null);
                                dayVO.setTitle((String) dayMap.get("title"));
                                dayVO.setMeals((String) dayMap.get("meals"));
                                dayVO.setHotel((String) dayMap.get("hotel"));

                                Object activitiesObj = dayMap.get("activities");
                                if (activitiesObj instanceof java.util.List) {
                                    @SuppressWarnings("unchecked")
                                    java.util.List<java.util.Map<String, Object>> actList =
                                            (java.util.List<java.util.Map<String, Object>>) activitiesObj;
                                    dayVO.setActivities(actList.stream()
                                            .map(actMap -> {
                                                RouteDetailVO.Activity actVO = new RouteDetailVO.Activity();
                                                actVO.setIcon((String) actMap.get("icon"));
                                                actVO.setTime((String) actMap.get("time"));
                                                actVO.setContent((String) actMap.get("content"));
                                                return actVO;
                                            })
                                            .collect(Collectors.toList()));
                                }
                            }
                            return dayVO;
                        })
                        .collect(Collectors.toList());
                vo.setItinerary(itinerary);
            }
        }

        return vo;
    }

    private RoutePackageVO toRoutePackageVO(ProductSku sku) {
        RoutePackageVO vo = new RoutePackageVO();
        BeanUtils.copyProperties(sku, vo);
        return vo;
    }

    private PriceCalendarVO toPriceCalendarVO(ProductPriceStock priceStock) {
        PriceCalendarVO vo = new PriceCalendarVO();
        vo.setDate(priceStock.getDate());
        vo.setPrice(priceStock.getPrice());
        vo.setChildPrice(priceStock.getChildPrice());

        // 计算剩余库存
        int remaining = priceStock.getStock() - priceStock.getSold() - priceStock.getLocked();
        vo.setStock(Math.max(0, remaining));
        vo.setAvailable(remaining > 0 && priceStock.getStatus() == 1);

        return vo;
    }
}
