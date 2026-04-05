package com.xunjiqianxing.service.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

        // ── Step 1：从 product_route 按出发城市拿到候选 product_id ──────────────
        // product_route.city_name = 出发城市标准名（精确匹配）
        List<Long> departureCityIds = null;
        if (StringUtils.hasText(departureCity)) {
            departureCityIds = productRouteMapper.selectList(
                    new LambdaQueryWrapper<ProductRoute>()
                            .eq(ProductRoute::getCityName, departureCity)
                            .select(ProductRoute::getProductId)
            ).stream().map(ProductRoute::getProductId).collect(Collectors.toList());

            // 出发城市无匹配，直接返回空
            if (departureCityIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L,
                        pageQuery.getPage(), pageQuery.getPageSize());
            }
        }

        // ── Step 2：从 product_route 按目的地关键词拿到候选 product_id ──────────
        // product_route.destination = 目的地，模糊匹配
        List<Long> destinationKeywordIds = null;
        if (StringUtils.hasText(keyword)) {
            destinationKeywordIds = productRouteMapper.selectList(
                    new LambdaQueryWrapper<ProductRoute>()
                            .like(ProductRoute::getDestination, keyword)
                            .select(ProductRoute::getProductId)
            ).stream().map(ProductRoute::getProductId).collect(Collectors.toList());
        }

        // ── Step 3：构建 product_main 查询 ──────────────────────────────────────
        LambdaQueryWrapper<ProductMain> wrapper = new LambdaQueryWrapper<ProductMain>()
                .eq(ProductMain::getBizType, "route")
                .eq(ProductMain::getStatus, 1)
                .eq(ProductMain::getIsDeleted, 0)
                .eq(ProductMain::getAuditStatus, 1);

        // 关键词：name/subtitle LIKE  OR  id IN (destination 命中的 IDs)
        if (StringUtils.hasText(keyword)) {
            final List<Long> destIds = destinationKeywordIds;
            wrapper.and(w -> {
                w.like(ProductMain::getName, keyword)
                 .or()
                 .like(ProductMain::getSubtitle, keyword);
                if (destIds != null && !destIds.isEmpty()) {
                    w.or().in(ProductMain::getId, destIds);
                }
            });
        }

        // 出发城市：限制在第一步拿到的 product_id 范围内
        if (departureCityIds != null) {
            wrapper.in(ProductMain::getId, departureCityIds);
        }

        wrapper.orderByDesc(ProductMain::getSortOrder)
               .orderByDesc(ProductMain::getCreatedAt);

        Page<ProductMain> result = productMainMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(),
                pageQuery.getPage(), pageQuery.getPageSize());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(Long skuId, LocalDate date, int quantity) {
        // 原子操作：locked += quantity，WHERE 确保剩余可用库存 >= quantity，防止超售
        int rows = productPriceStockMapper.update(null,
                new LambdaUpdateWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, skuId)
                        .eq(ProductPriceStock::getDate, date)
                        .eq(ProductPriceStock::getStatus, 1)
                        .apply("(stock - sold - locked) >= {0}", quantity)
                        .setSql("locked = locked + " + quantity)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseStock(Long skuId, LocalDate date, int quantity) {
        // 原子操作：locked -= quantity，WHERE 防止 locked 变负
        int rows = productPriceStockMapper.update(null,
                new LambdaUpdateWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, skuId)
                        .eq(ProductPriceStock::getDate, date)
                        .ge(ProductPriceStock::getLocked, quantity)
                        .setSql("locked = locked - " + quantity)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmStock(Long skuId, LocalDate date, int quantity) {
        // 原子操作：sold += quantity, locked -= quantity
        int rows = productPriceStockMapper.update(null,
                new LambdaUpdateWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, skuId)
                        .eq(ProductPriceStock::getDate, date)
                        .ge(ProductPriceStock::getLocked, quantity)
                        .setSql("sold = sold + " + quantity + ", locked = locked - " + quantity)
        );
        return rows > 0;
    }
}
