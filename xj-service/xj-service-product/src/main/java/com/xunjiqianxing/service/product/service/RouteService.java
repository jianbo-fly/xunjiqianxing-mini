package com.xunjiqianxing.service.product.service;

import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductRoute;
import com.xunjiqianxing.service.product.entity.ProductSku;

import java.time.LocalDate;
import java.util.List;

/**
 * 线路服务
 */
public interface RouteService {

    /**
     * 分页查询线路列表
     */
    PageResult<ProductMain> pageRoutes(PageQuery pageQuery, String category, String departureCity, String keyword);

    /**
     * 获取线路详情
     */
    ProductMain getRouteById(Long id);

    /**
     * 获取线路扩展信息
     */
    ProductRoute getRouteExtById(Long productId);

    /**
     * 获取线路的套餐列表
     */
    List<ProductSku> getRoutePackages(Long productId);

    /**
     * 获取套餐详情
     */
    ProductSku getPackageById(Long skuId);

    /**
     * 获取套餐的价格日历
     */
    List<ProductPriceStock> getPackageCalendar(Long skuId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取指定日期的价格库存
     */
    ProductPriceStock getPriceStock(Long skuId, LocalDate date);
}
