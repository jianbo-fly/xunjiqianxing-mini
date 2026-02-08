package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.system.*;
import com.xunjiqianxing.admin.service.AdminConfigService;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统设置接口
 */
@Tag(name = "系统设置")
@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final AdminConfigService adminConfigService;

    // ==================== 基础配置 ====================

    @Operation(summary = "获取基础设置")
    @GetMapping("/basic")
    public Result<BasicSettingsVO> getBasicSettings() {
        return Result.success(adminConfigService.getBasicSettings());
    }

    @Operation(summary = "更新基础设置")
    @PostMapping("/basic")
    public Result<Void> updateBasicSettings(@Valid @RequestBody BasicSettingsRequest request) {
        adminConfigService.updateBasicSettings(request);
        return Result.success();
    }

    // ==================== 积分规则 ====================

    @Operation(summary = "获取积分规则设置")
    @GetMapping("/points")
    public Result<PointsSettingsVO> getPointsSettings() {
        return Result.success(adminConfigService.getPointsSettings());
    }

    @Operation(summary = "更新积分规则设置")
    @PostMapping("/points")
    public Result<Void> updatePointsSettings(@Valid @RequestBody PointsSettingsRequest request) {
        adminConfigService.updatePointsSettings(request);
        return Result.success();
    }

    // ==================== 退款规则 ====================

    @Operation(summary = "获取退款规则设置")
    @GetMapping("/refund")
    public Result<RefundSettingsVO> getRefundSettings() {
        return Result.success(adminConfigService.getRefundSettings());
    }

    @Operation(summary = "更新退款规则设置")
    @PostMapping("/refund")
    public Result<Void> updateRefundSettings(@Valid @RequestBody RefundSettingsRequest request) {
        adminConfigService.updateRefundSettings(request);
        return Result.success();
    }

    // ==================== 价格配置 ====================

    @Operation(summary = "获取价格设置")
    @GetMapping("/price")
    public Result<PriceSettingsVO> getPriceSettings() {
        return Result.success(adminConfigService.getPriceSettings());
    }

    @Operation(summary = "更新价格设置")
    @PostMapping("/price")
    public Result<Void> updatePriceSettings(@Valid @RequestBody PriceSettingsRequest request) {
        adminConfigService.updatePriceSettings(request);
        return Result.success();
    }
}
