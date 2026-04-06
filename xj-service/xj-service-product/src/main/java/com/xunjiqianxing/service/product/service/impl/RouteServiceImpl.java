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

import java.math.BigDecimal;
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
    public PageResult<ProductMain> pageRoutes(PageQuery pageQuery, String category, String departureCity, String keyword,
                                              Integer minDays, Integer maxDays,
                                              BigDecimal filterMinPrice, BigDecimal filterMaxPrice) {
        Page<ProductMain> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        // ── Step 1：从 product_route 合并查询 category / departureCity / days ────
        // 这三个条件都是 AND 语义，合并为一次查询
        boolean hasDaysFilter = minDays != null && minDays > 0;
        boolean hasRouteAndFilter = StringUtils.hasText(category)
                || StringUtils.hasText(departureCity)
                || hasDaysFilter;

        List<Long> routeFilterIds = null;
        if (hasRouteAndFilter) {
            LambdaQueryWrapper<ProductRoute> routeWrapper = new LambdaQueryWrapper<ProductRoute>()
                    .select(ProductRoute::getProductId);
            if (StringUtils.hasText(category)) {
                routeWrapper.eq(ProductRoute::getCategory, category);
            }
            if (StringUtils.hasText(departureCity)) {
                routeWrapper.eq(ProductRoute::getCityName, departureCity);
            }
            if (hasDaysFilter) {
                routeWrapper.ge(ProductRoute::getDays, minDays);
                if (maxDays != null && maxDays > 0) {
                    routeWrapper.le(ProductRoute::getDays, maxDays);
                }
            }
            routeFilterIds = productRouteMapper.selectList(routeWrapper)
                    .stream().map(ProductRoute::getProductId).collect(Collectors.toList());

            if (routeFilterIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0L,
                        pageQuery.getPage(), pageQuery.getPageSize());
            }
        }

        // ── Step 2：从 product_route 按目的地关键词拿候选 ID（OR 语义，单独查询）──
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

        // 分类 + 出发城市 + 天数（合并后的 AND 过滤）
        if (routeFilterIds != null) {
            wrapper.in(ProductMain::getId, routeFilterIds);
        }

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

        // 价格：product_main.min_price 即最低售价
        if (filterMinPrice != null && filterMinPrice.compareTo(BigDecimal.ZERO) > 0) {
            wrapper.ge(ProductMain::getMinPrice, filterMinPrice);
        }
        if (filterMaxPrice != null && filterMaxPrice.compareTo(BigDecimal.ZERO) > 0) {
            wrapper.le(ProductMain::getMinPrice, filterMaxPrice);
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
