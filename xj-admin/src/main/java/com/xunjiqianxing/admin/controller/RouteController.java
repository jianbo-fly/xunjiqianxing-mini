package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.route.*;
import com.xunjiqianxing.admin.service.AdminRouteService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 线路管理接口
 */
@Tag(name = "线路管理")
@RestController
@RequestMapping("/admin/route")
@RequiredArgsConstructor
public class RouteController {

    private final AdminRouteService adminRouteService;

    // ==================== 线路管理 ====================

    @Operation(summary = "线路列表")
    @GetMapping("/list")
    public Result<PageResult<RouteListVO>> list(RouteQueryRequest request) {
        return Result.success(adminRouteService.pageList(request));
    }

    @Operation(summary = "线路详情")
    @GetMapping("/{id}")
    public Result<RouteDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminRouteService.getDetail(id));
    }

    @Operation(summary = "创建线路")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RouteCreateRequest request) {
        return Result.success(adminRouteService.create(request));
    }

    @Operation(summary = "更新线路")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody RouteUpdateRequest request) {
        adminRouteService.update(request);
        return Result.success();
    }

    @Operation(summary = "删除线路")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminRouteService.delete(id);
        return Result.success();
    }

    @Operation(summary = "上下架线路")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0下架 1上架") @RequestParam Integer status) {
        adminRouteService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "审核线路")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody RouteAuditRequest request) {
        adminRouteService.audit(request);
        return Result.success();
    }

    // ==================== 套餐管理 ====================

    @Operation(summary = "获取线路套餐列表")
    @GetMapping("/{productId}/packages")
    public Result<List<RouteDetailVO.PackageVO>> getPackages(@PathVariable Long productId) {
        return Result.success(adminRouteService.getPackages(productId));
    }

    @Operation(summary = "创建套餐")
    @PostMapping("/package")
    public Result<Long> createPackage(@Valid @RequestBody PackageCreateRequest request) {
        return Result.success(adminRouteService.createPackage(request));
    }

    @Operation(summary = "更新套餐")
    @PutMapping("/package")
    public Result<Void> updatePackage(@Valid @RequestBody PackageUpdateRequest request) {
        adminRouteService.updatePackage(request);
        return Result.success();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/package/{id}")
    public Result<Void> deletePackage(@PathVariable Long id) {
        adminRouteService.deletePackage(id);
        return Result.success();
    }

    // ==================== 价格日历 ====================

    @Operation(summary = "获取价格日历")
    @GetMapping("/package/{skuId}/calendar")
    public Result<List<ProductPriceStock>> getPriceCalendar(
            @PathVariable Long skuId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(adminRouteService.getPriceCalendar(skuId, startDate, endDate));
    }

    @Operation(summary = "批量设置价格日历")
    @PostMapping("/package/prices")
    public Result<Void> setPriceCalendar(@Valid @RequestBody PriceCalendarRequest request) {
        adminRouteService.setPriceCalendar(request);
        return Result.success();
    }
}
