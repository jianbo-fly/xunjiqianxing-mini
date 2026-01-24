package com.xunjiqianxing.service.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductRoute;
import com.xunjiqianxing.service.product.entity.ProductSku;
import com.xunjiqianxing.service.product.mapper.ProductMainMapper;
import com.xunjiqianxing.service.product.mapper.ProductPriceStockMapper;
import com.xunjiqianxing.service.product.mapper.ProductRouteMapper;
import com.xunjiqianxing.service.product.mapper.ProductSkuMapper;
import com.xunjiqianxing.service.product.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 线路服务实现
 */
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final ProductMainMapper productMainMapper;
    private final ProductRouteMapper productRouteMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductPriceStockMapper productPriceStockMapper;

    @Override
    public PageResult<ProductMain> pageRoutes(PageQuery pageQuery, String category, String departureCity, String keyword) {
        Page<ProductMain> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        LambdaQueryWrapper<ProductMain> wrapper = new LambdaQueryWrapper<ProductMain>()
                .eq(ProductMain::getBizType, "route")
                .eq(ProductMain::getStatus, 1)
                .eq(ProductMain::getIsDeleted, 0)
                .eq(ProductMain::getAuditStatus, 1);

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(ProductMain::getName, keyword)
                    .or()
                    .like(ProductMain::getSubtitle, keyword)
                    .or()
                    .like(ProductMain::getCityName, keyword)
            );
        }

        // 城市筛选
        if (StringUtils.hasText(departureCity)) {
            wrapper.eq(ProductMain::getCityCode, departureCity);
        }

        wrapper.orderByDesc(ProductMain::getSortOrder)
                .orderByDesc(ProductMain::getCreatedAt);

        Page<ProductMain> result = productMainMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), pageQuery.getPage(), pageQuery.getPageSize());
    }

    @Override
    public ProductMain getRouteById(Long id) {
        ProductMain product = productMainMapper.selectOne(
                new LambdaQueryWrapper<ProductMain>()
                        .eq(ProductMain::getId, id)
                        .eq(ProductMain::getBizType, "route")
                        .eq(ProductMain::getIsDeleted, 0)
        );
        return product;
    }

    @Override
    public ProductRoute getRouteExtById(Long productId) {
        return productRouteMapper.selectById(productId);
    }

    @Override
    public List<ProductSku> getRoutePackages(Long productId) {
        return productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getStatus, 1)
                        .orderByAsc(ProductSku::getSortOrder)
        );
    }

    @Override
    public ProductSku getPackageById(Long skuId) {
        return productSkuMapper.selectById(skuId);
    }

    @Override
    public List<ProductPriceStock> getPackageCalendar(Long skuId, LocalDate startDate, LocalDate endDate) {
        return productPriceStockMapper.selectList(
                new LambdaQueryWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, skuId)
                        .ge(ProductPriceStock::getDate, startDate)
                        .le(ProductPriceStock::getDate, endDate)
                        .eq(ProductPriceStock::getStatus, 1)
                        .orderByAsc(ProductPriceStock::getDate)
        );
    }

    @Override
    public ProductPriceStock getPriceStock(Long skuId, LocalDate date) {
        return productPriceStockMapper.selectOne(
                new LambdaQueryWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, skuId)
                        .eq(ProductPriceStock::getDate, date)
        );
    }
}
