package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.route.*;
import com.xunjiqianxing.admin.entity.SystemSupplier;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductRoute;
import com.xunjiqianxing.service.product.entity.ProductSku;
import com.xunjiqianxing.service.product.mapper.ProductMainMapper;
import com.xunjiqianxing.service.product.mapper.ProductPriceStockMapper;
import com.xunjiqianxing.service.product.mapper.ProductRouteMapper;
import com.xunjiqianxing.service.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台 - 线路管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRouteService {

    private final ProductMainMapper productMainMapper;
    private final ProductRouteMapper productRouteMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductPriceStockMapper productPriceStockMapper;
    private final SystemSupplierMapper systemSupplierMapper;

    /**
     * 分页查询线路列表
     */
    public PageResult<RouteListVO> pageList(RouteQueryRequest request) {
        Page<ProductMain> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<ProductMain> wrapper = new LambdaQueryWrapper<ProductMain>()
                .eq(ProductMain::getBizType, "route")
                .eq(ProductMain::getIsDeleted, 0);

        // 供应商只能查看自己的线路
        Long supplierId = getCurrentSupplierId();
        if (supplierId != null) {
            wrapper.eq(ProductMain::getSupplierId, supplierId);
        } else if (request.getSupplierId() != null) {
            wrapper.eq(ProductMain::getSupplierId, request.getSupplierId());
        }

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(ProductMain::getName, request.getKeyword())
                    .or()
                    .like(ProductMain::getSubtitle, request.getKeyword())
            );
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(ProductMain::getStatus, request.getStatus());
        }

        // 审核状态筛选
        if (request.getAuditStatus() != null) {
            wrapper.eq(ProductMain::getAuditStatus, request.getAuditStatus());
        }

        wrapper.orderByDesc(ProductMain::getCreatedAt);

        Page<ProductMain> result = productMainMapper.selectPage(page, wrapper);

        // 转换为 VO
        List<RouteListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取线路详情
     */
    public RouteDetailVO getDetail(Long id) {
        ProductMain product = productMainMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BizException("线路不存在");
        }

        // 检查权限
        checkRoutePermission(product);

        RouteDetailVO vo = new RouteDetailVO();
        BeanUtil.copyProperties(product, vo);

        // 获取扩展信息
        ProductRoute routeExt = productRouteMapper.selectById(id);
        if (routeExt != null) {
            vo.setCategory(routeExt.getCategory());
            vo.setDepartureCity(routeExt.getDepartureCity());
            vo.setDestination(routeExt.getDestination());
            vo.setCostExclude(routeExt.getCostExclude());
            vo.setBookingNotice(routeExt.getBookingNotice());
            vo.setTips(routeExt.getTips());
            vo.setCostInclude(routeExt.getCostInclude());

            // 转换行程数据 (使用 ObjectMapper 处理 LinkedHashMap 转换)
            if (routeExt.getItinerary() != null && !routeExt.getItinerary().isEmpty()) {
                List<RouteDetailVO.ItineraryDay> itinerary = routeExt.getItinerary().stream()
                        .map(dayObj -> {
                            RouteDetailVO.ItineraryDay dayVO = new RouteDetailVO.ItineraryDay();
                            // 处理可能是 LinkedHashMap 的情况
                            if (dayObj instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> dayMap = (java.util.Map<String, Object>) dayObj;
                                dayVO.setDay(dayMap.get("day") != null ? Integer.valueOf(dayMap.get("day").toString()) : null);
                                dayVO.setTitle((String) dayMap.get("title"));
                                dayVO.setMeals((String) dayMap.get("meals"));
                                dayVO.setHotel((String) dayMap.get("hotel"));

                                Object activitiesObj = dayMap.get("activities");
                                if (activitiesObj instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<java.util.Map<String, Object>> actList = (List<java.util.Map<String, Object>>) activitiesObj;
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
                            } else if (dayObj instanceof ProductRoute.ItineraryDay) {
                                ProductRoute.ItineraryDay day = (ProductRoute.ItineraryDay) dayObj;
                                dayVO.setDay(day.getDay());
                                dayVO.setTitle(day.getTitle());
                                dayVO.setMeals(day.getMeals());
                                dayVO.setHotel(day.getHotel());
                                if (day.getActivities() != null) {
                                    dayVO.setActivities(day.getActivities().stream()
                                            .map(act -> {
                                                RouteDetailVO.Activity actVO = new RouteDetailVO.Activity();
                                                actVO.setIcon(act.getIcon());
                                                actVO.setTime(act.getTime());
                                                actVO.setContent(act.getContent());
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

        // 获取供应商名称
        if (product.getSupplierId() != null) {
            SystemSupplier supplier = systemSupplierMapper.selectById(product.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            }
        }

        // 获取套餐列表
        List<ProductSku> skuList = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
                        .orderByAsc(ProductSku::getSortOrder)
        );

        List<RouteDetailVO.PackageVO> packages = skuList.stream()
                .map(this::convertToPackageVO)
                .collect(Collectors.toList());
        vo.setPackages(packages);

        return vo;
    }

    /**
     * 创建线路
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(RouteCreateRequest request) {
        // 确定供应商ID
        Long supplierId = getCurrentSupplierId();
        if (supplierId == null) {
            // 管理员创建，使用请求中的供应商ID
            supplierId = request.getSupplierId();
            if (supplierId == null) {
                throw new BizException("请选择供应商");
            }
        }

        // 创建商品主表
        ProductMain product = new ProductMain();
        product.setId(IdGenerator.nextId());
        product.setBizType("route");
        product.setSupplierId(supplierId);
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setCoverImage(request.getCoverImage());
        product.setImages(request.getImages());
        product.setTags(request.getTags());
        product.setCityCode(request.getCityCode());
        product.setCityName(request.getCityName());
        product.setMinPrice(request.getMinPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setSalesCount(0);
        product.setViewCount(0);
        product.setScore(BigDecimal.valueOf(5.0));
        product.setStatus(0); // 默认下架
        product.setAuditStatus(0); // 待审核
        product.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        product.setIsRecommend(request.getIsRecommend() != null ? request.getIsRecommend() : 0);
        product.setIsDeleted(0);

        productMainMapper.insert(product);

        // 创建线路扩展表
        ProductRoute routeExt = new ProductRoute();
        routeExt.setProductId(product.getId());
        routeExt.setCategory(request.getCategory());
        routeExt.setDepartureCity(request.getDepartureCity());
        routeExt.setDestination(request.getDestination());
        routeExt.setCostExclude(request.getCostExclude());
        routeExt.setBookingNotice(request.getBookingNotice());
        routeExt.setTips(request.getTips());
        routeExt.setCostInclude(request.getCostInclude());

        // 转换行程数据 (转换为 Map 结构以便 JSON 序列化)
        if (request.getItinerary() != null && !request.getItinerary().isEmpty()) {
            List<Object> itinerary = request.getItinerary().stream()
                    .map(day -> {
                        Map<String, Object> dayMap = new HashMap<>();
                        dayMap.put("day", day.getDay());
                        dayMap.put("title", day.getTitle());
                        dayMap.put("meals", day.getMeals());
                        dayMap.put("hotel", day.getHotel());
                        if (day.getActivities() != null) {
                            List<Map<String, Object>> activities = day.getActivities().stream()
                                    .map(act -> {
                                        Map<String, Object> actMap = new HashMap<>();
                                        actMap.put("icon", act.getIcon());
                                        actMap.put("time", act.getTime());
                                        actMap.put("content", act.getContent());
                                        return actMap;
                                    })
                                    .collect(Collectors.toList());
                            dayMap.put("activities", activities);
                        }
                        return (Object) dayMap;
                    })
                    .collect(Collectors.toList());
            routeExt.setItinerary(itinerary);
        }

        productRouteMapper.insert(routeExt);

        log.info("创建线路成功: id={}, name={}", product.getId(), product.getName());
        return product.getId();
    }

    /**
     * 更新线路
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(RouteUpdateRequest request) {
        ProductMain product = productMainMapper.selectById(request.getId());
        if (product == null || product.getIsDeleted() == 1) {
            throw new BizException("线路不存在");
        }

        // 检查权限
        checkRoutePermission(product);

        // 更新商品主表
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (request.getSubtitle() != null) {
            product.setSubtitle(request.getSubtitle());
        }
        if (StringUtils.hasText(request.getCoverImage())) {
            product.setCoverImage(request.getCoverImage());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getTags() != null) {
            product.setTags(request.getTags());
        }
        if (request.getCityCode() != null) {
            product.setCityCode(request.getCityCode());
        }
        if (request.getCityName() != null) {
            product.setCityName(request.getCityName());
        }
        if (request.getMinPrice() != null) {
            product.setMinPrice(request.getMinPrice());
        }
        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getSortOrder() != null) {
            product.setSortOrder(request.getSortOrder());
        }
        if (request.getIsRecommend() != null) {
            product.setIsRecommend(request.getIsRecommend());
        }

        // 修改后需要重新审核
        if (product.getAuditStatus() == 1) {
            product.setAuditStatus(0);
            product.setStatus(0);
        }

        productMainMapper.updateById(product);

        // 更新扩展表
        ProductRoute routeExt = productRouteMapper.selectById(request.getId());
        if (routeExt == null) {
            routeExt = new ProductRoute();
            routeExt.setProductId(request.getId());
        }

        if (request.getCategory() != null) {
            routeExt.setCategory(request.getCategory());
        }
        if (request.getDepartureCity() != null) {
            routeExt.setDepartureCity(request.getDepartureCity());
        }
        if (request.getDestination() != null) {
            routeExt.setDestination(request.getDestination());
        }
        if (request.getCostExclude() != null) {
            routeExt.setCostExclude(request.getCostExclude());
        }
        if (request.getBookingNotice() != null) {
            routeExt.setBookingNotice(request.getBookingNotice());
        }
        if (request.getTips() != null) {
            routeExt.setTips(request.getTips());
        }
        if (request.getCostInclude() != null) {
            routeExt.setCostInclude(request.getCostInclude());
        }

        // 转换行程数据 (转换为 Map 结构以便 JSON 序列化)
        if (request.getItinerary() != null) {
            List<Object> itinerary = request.getItinerary().stream()
                    .map(day -> {
                        Map<String, Object> dayMap = new HashMap<>();
                        dayMap.put("day", day.getDay());
                        dayMap.put("title", day.getTitle());
                        dayMap.put("meals", day.getMeals());
                        dayMap.put("hotel", day.getHotel());
                        if (day.getActivities() != null) {
                            List<Map<String, Object>> activities = day.getActivities().stream()
                                    .map(act -> {
                                        Map<String, Object> actMap = new HashMap<>();
                                        actMap.put("icon", act.getIcon());
                                        actMap.put("time", act.getTime());
                                        actMap.put("content", act.getContent());
                                        return actMap;
                                    })
                                    .collect(Collectors.toList());
                            dayMap.put("activities", activities);
                        }
                        return (Object) dayMap;
                    })
                    .collect(Collectors.toList());
            routeExt.setItinerary(itinerary);
        }

        if (productRouteMapper.selectById(request.getId()) != null) {
            productRouteMapper.updateById(routeExt);
        } else {
            productRouteMapper.insert(routeExt);
        }

        log.info("更新线路成功: id={}", request.getId());
    }

    /**
     * 删除线路
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProductMain product = productMainMapper.selectById(id);
        if (product == null) {
            throw new BizException("线路不存在");
        }

        // 检查权限
        checkRoutePermission(product);

        // 逻辑删除
        product.setIsDeleted(1);
        productMainMapper.updateById(product);

        log.info("删除线路成功: id={}", id);
    }

    /**
     * 上下架线路
     */
    public void updateStatus(Long id, Integer status) {
        ProductMain product = productMainMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BizException("线路不存在");
        }

        // 检查权限
        checkRoutePermission(product);

        // 上架需要审核通过
        if (status == 1 && product.getAuditStatus() != 1) {
            throw new BizException("线路未审核通过，无法上架");
        }

        product.setStatus(status);
        productMainMapper.updateById(product);

        log.info("更新线路状态: id={}, status={}", id, status);
    }

    /**
     * 审核线路 (仅管理员)
     */
    public void audit(RouteAuditRequest request) {
        // 检查是否是管理员
        if (getCurrentSupplierId() != null) {
            throw new BizException("无权限操作");
        }

        ProductMain product = productMainMapper.selectById(request.getId());
        if (product == null || product.getIsDeleted() == 1) {
            throw new BizException("线路不存在");
        }

        if (request.getAuditStatus() == 2 && !StringUtils.hasText(request.getAuditRemark())) {
            throw new BizException("驳回时必须填写备注");
        }

        product.setAuditStatus(request.getAuditStatus());
        product.setAuditRemark(request.getAuditRemark());

        // 审核通过后自动上架
        if (request.getAuditStatus() == 1) {
            product.setStatus(1);
        }

        productMainMapper.updateById(product);

        log.info("审核线路: id={}, status={}", request.getId(), request.getAuditStatus());
    }

    // ==================== 套餐管理 ====================

    /**
     * 获取套餐列表
     */
    public List<RouteDetailVO.PackageVO> getPackages(Long productId) {
        // 检查线路权限
        ProductMain product = productMainMapper.selectById(productId);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BizException("线路不存在");
        }
        checkRoutePermission(product);

        List<ProductSku> skuList = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getSortOrder)
        );

        return skuList.stream()
                .map(this::convertToPackageVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPackage(PackageCreateRequest request) {
        // 检查线路权限
        ProductMain product = productMainMapper.selectById(request.getProductId());
        if (product == null || product.getIsDeleted() == 1) {
            throw new BizException("线路不存在");
        }
        checkRoutePermission(product);

        ProductSku sku = new ProductSku();
        sku.setId(IdGenerator.nextId());
        sku.setProductId(request.getProductId());
        sku.setBizType("route");
        sku.setName(request.getName());
        sku.setTags(request.getTags());
        sku.setBasePrice(request.getBasePrice());
        sku.setChildPrice(request.getChildPrice());
        sku.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        sku.setStatus(1);

        // 构建attrs，包含days和nights
        Map<String, Object> attrs = request.getAttrs() != null ? new HashMap<>(request.getAttrs()) : new HashMap<>();
        if (request.getDays() != null) {
            attrs.put("days", request.getDays());
        }
        if (request.getNights() != null) {
            attrs.put("nights", request.getNights());
        }
        sku.setAttrs(attrs);

        productSkuMapper.insert(sku);

        // 更新线路最低价
        updateRouteMinPrice(request.getProductId());

        log.info("创建套餐成功: id={}, productId={}", sku.getId(), request.getProductId());
        return sku.getId();
    }

    /**
     * 更新套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePackage(PackageUpdateRequest request) {
        ProductSku sku = productSkuMapper.selectById(request.getId());
        if (sku == null) {
            throw new BizException("套餐不存在");
        }

        // 检查线路权限
        ProductMain product = productMainMapper.selectById(sku.getProductId());
        checkRoutePermission(product);

        if (StringUtils.hasText(request.getName())) {
            sku.setName(request.getName());
        }
        if (request.getTags() != null) {
            sku.setTags(request.getTags());
        }
        if (request.getBasePrice() != null) {
            sku.setBasePrice(request.getBasePrice());
        }
        if (request.getChildPrice() != null) {
            sku.setChildPrice(request.getChildPrice());
        }
        if (request.getSortOrder() != null) {
            sku.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            sku.setStatus(request.getStatus());
        }

        // 处理attrs和days/nights
        Map<String, Object> attrs = sku.getAttrs() != null ? new HashMap<>(sku.getAttrs()) : new HashMap<>();
        if (request.getAttrs() != null) {
            attrs.putAll(request.getAttrs());
        }
        if (request.getDays() != null) {
            attrs.put("days", request.getDays());
        }
        if (request.getNights() != null) {
            attrs.put("nights", request.getNights());
        }
        sku.setAttrs(attrs);

        productSkuMapper.updateById(sku);

        // 更新线路最低价
        updateRouteMinPrice(sku.getProductId());

        log.info("更新套餐成功: id={}", request.getId());
    }

    /**
     * 删除套餐
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePackage(Long id) {
        ProductSku sku = productSkuMapper.selectById(id);
        if (sku == null) {
            throw new BizException("套餐不存在");
        }

        // 检查线路权限
        ProductMain product = productMainMapper.selectById(sku.getProductId());
        checkRoutePermission(product);

        Long productId = sku.getProductId();

        // 删除套餐
        productSkuMapper.deleteById(id);

        // 删除价格日历
        productPriceStockMapper.delete(
                new LambdaQueryWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, id)
        );

        // 更新线路最低价
        updateRouteMinPrice(productId);

        log.info("删除套餐成功: id={}", id);
    }

    /**
     * 批量设置价格日历
     */
    @Transactional(rollbackFor = Exception.class)
    public void setPriceCalendar(PriceCalendarRequest request) {
        ProductSku sku = productSkuMapper.selectById(request.getSkuId());
        if (sku == null) {
            throw new BizException("套餐不存在");
        }

        // 检查线路权限
        ProductMain product = productMainMapper.selectById(sku.getProductId());
        checkRoutePermission(product);

        for (PriceCalendarRequest.PriceItem item : request.getPrices()) {
            // 查找是否已存在
            ProductPriceStock existing = productPriceStockMapper.selectOne(
                    new LambdaQueryWrapper<ProductPriceStock>()
                            .eq(ProductPriceStock::getSkuId, request.getSkuId())
                            .eq(ProductPriceStock::getDate, item.getDate())
            );

            if (existing != null) {
                // 更新
                existing.setPrice(item.getPrice());
                existing.setChildPrice(item.getChildPrice());
                existing.setStock(item.getStock());
                existing.setStatus(item.getStatus() != null ? item.getStatus() : 1);
                productPriceStockMapper.updateById(existing);
            } else {
                // 新增
                ProductPriceStock priceStock = new ProductPriceStock();
                priceStock.setId(IdGenerator.nextId());
                priceStock.setSkuId(request.getSkuId());
                priceStock.setDate(item.getDate());
                priceStock.setPrice(item.getPrice());
                priceStock.setChildPrice(item.getChildPrice());
                priceStock.setStock(item.getStock());
                priceStock.setSold(0);
                priceStock.setLocked(0);
                priceStock.setStatus(item.getStatus() != null ? item.getStatus() : 1);
                productPriceStockMapper.insert(priceStock);
            }
        }

        // 更新线路最低价
        updateRouteMinPrice(sku.getProductId());

        log.info("设置价格日历成功: skuId={}, count={}", request.getSkuId(), request.getPrices().size());
    }

    /**
     * 获取价格日历
     */
    public List<ProductPriceStock> getPriceCalendar(Long skuId, LocalDate startDate, LocalDate endDate) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException("套餐不存在");
        }

        // 检查线路权限
        ProductMain product = productMainMapper.selectById(sku.getProductId());
        checkRoutePermission(product);

        return productPriceStockMapper.selectList(
                new LambdaQueryWrapper<ProductPriceStock>()
                        .eq(ProductPriceStock::getSkuId, skuId)
                        .ge(ProductPriceStock::getDate, startDate)
                        .le(ProductPriceStock::getDate, endDate)
                        .orderByAsc(ProductPriceStock::getDate)
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前供应商ID (如果是供应商登录)
     */
    private Long getCurrentSupplierId() {
        String roleType = (String) StpUtil.getSession().get("roleType");
        if ("supplier".equals(roleType)) {
            return (Long) StpUtil.getSession().get("supplierId");
        }
        return null;
    }

    /**
     * 检查线路权限
     */
    private void checkRoutePermission(ProductMain product) {
        Long supplierId = getCurrentSupplierId();
        if (supplierId != null && !supplierId.equals(product.getSupplierId())) {
            throw new BizException("无权限操作此线路");
        }
    }

    /**
     * 更新线路最低价
     */
    private void updateRouteMinPrice(Long productId) {
        // 获取所有启用的套餐
        List<ProductSku> skuList = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getStatus, 1)
        );

        if (skuList.isEmpty()) {
            return;
        }

        // 找出最低基准价
        BigDecimal minPrice = skuList.stream()
                .map(ProductSku::getBasePrice)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null);

        if (minPrice != null) {
            productMainMapper.update(null,
                    new LambdaUpdateWrapper<ProductMain>()
                            .eq(ProductMain::getId, productId)
                            .set(ProductMain::getMinPrice, minPrice)
            );
        }
    }

    /**
     * 转换为列表 VO
     */
    private RouteListVO convertToListVO(ProductMain product) {
        RouteListVO vo = new RouteListVO();
        BeanUtil.copyProperties(product, vo);

        // 获取扩展信息
        ProductRoute routeExt = productRouteMapper.selectById(product.getId());
        if (routeExt != null) {
            vo.setCategory(routeExt.getCategory());
            vo.setDestination(routeExt.getDestination());
        }

        // 获取供应商名称
        if (product.getSupplierId() != null) {
            SystemSupplier supplier = systemSupplierMapper.selectById(product.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            }
        }

        // 获取套餐数量
        Long count = productSkuMapper.selectCount(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, product.getId())
        );
        vo.setPackageCount(count.intValue());

        return vo;
    }

    /**
     * 转换为套餐 VO
     */
    private RouteDetailVO.PackageVO convertToPackageVO(ProductSku sku) {
        RouteDetailVO.PackageVO vo = new RouteDetailVO.PackageVO();
        vo.setId(sku.getId());
        vo.setName(sku.getName());
        vo.setTags(sku.getTags());
        vo.setBasePrice(sku.getBasePrice());
        vo.setChildPrice(sku.getChildPrice());
        vo.setStatus(sku.getStatus());
        vo.setSortOrder(sku.getSortOrder());

        // 从 attrs 中获取行程天数
        if (sku.getAttrs() != null) {
            Object days = sku.getAttrs().get("days");
            if (days != null) {
                vo.setDays(Integer.valueOf(days.toString()));
            }
            Object nights = sku.getAttrs().get("nights");
            if (nights != null) {
                vo.setNights(Integer.valueOf(nights.toString()));
            }
        }

        return vo;
    }
}
