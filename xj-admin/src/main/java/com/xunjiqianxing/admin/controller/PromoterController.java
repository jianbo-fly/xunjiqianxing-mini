package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.promotion.*;
import com.xunjiqianxing.admin.service.AdminPromoterService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 推广员管理接口
 */
@Tag(name = "推广员管理")
@RestController
@RequestMapping("/admin/promoter")
@RequiredArgsConstructor
public class PromoterController {

    private final AdminPromoterService adminPromoterService;

    // ==================== 申请管理 ====================

    @Operation(summary = "推广员申请列表")
    @GetMapping("/applies")
    public Result<PageResult<PromoterListVO>> applies(PromoterQueryRequest request) {
        return Result.success(adminPromoterService.pageApplies(request));
    }

    @Operation(summary = "审核申请")
    @PostMapping("/applies/audit")
    public Result<Void> auditApply(@Valid @RequestBody PromoterAuditRequest request) {
        adminPromoterService.audit(request);
        return Result.success();
    }

    @Operation(summary = "待审核申请数量")
    @GetMapping("/applies/pendingCount")
    public Result<Long> pendingApplyCount() {
        return Result.success(adminPromoterService.getPendingApplyCount());
    }

    // ==================== 推广员管理 ====================

    @Operation(summary = "推广员列表")
    @GetMapping("/list")
    public Result<PageResult<PromoterListVO>> list(PromoterQueryRequest request) {
        return Result.success(adminPromoterService.pageList(request));
    }

    @Operation(summary = "推广员详情")
    @GetMapping("/{id}")
    public Result<PromoterDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminPromoterService.getDetail(id));
    }

    @Operation(summary = "更新推广员信息")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody PromoterUpdateRequest request) {
        adminPromoterService.update(request);
        return Result.success();
    }

    @Operation(summary = "启用/禁用推广员")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 1正常 2禁用") @RequestParam Integer status) {
        adminPromoterService.updateStatus(id, status);
        return Result.success();
    }

    // ==================== 绑定与佣金 ====================

    @Operation(summary = "绑定用户记录")
    @GetMapping("/{id}/bindLogs")
    public Result<PageResult<BindLogVO>> bindLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminPromoterService.getBindLogs(id, page, pageSize));
    }

    @Operation(summary = "佣金记录")
    @GetMapping("/{id}/commissions")
    public Result<PageResult<CommissionVO>> commissions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(adminPromoterService.getCommissions(id, page, pageSize));
    }
}
