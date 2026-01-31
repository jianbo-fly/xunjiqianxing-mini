package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.promotion.*;
import com.xunjiqianxing.admin.service.AdminCouponService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券管理接口
 */
@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final AdminCouponService adminCouponService;

    // ==================== 模板管理 ====================

    @Operation(summary = "优惠券模板列表")
    @GetMapping("/templates")
    public Result<PageResult<CouponListVO>> templates(CouponQueryRequest request) {
        return Result.success(adminCouponService.pageList(request));
    }

    @Operation(summary = "优惠券详情")
    @GetMapping("/template/{id}")
    public Result<CouponListVO> templateDetail(@PathVariable Long id) {
        return Result.success(adminCouponService.getDetail(id));
    }

    @Operation(summary = "创建优惠券模板")
    @PostMapping("/template")
    public Result<Long> createTemplate(@Valid @RequestBody CouponCreateRequest request) {
        return Result.success(adminCouponService.create(request));
    }

    @Operation(summary = "更新优惠券模板")
    @PutMapping("/template")
    public Result<Void> updateTemplate(@Valid @RequestBody CouponUpdateRequest request) {
        adminCouponService.update(request);
        return Result.success();
    }

    @Operation(summary = "启用/停用优惠券")
    @PutMapping("/template/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0停用 1启用") @RequestParam Integer status) {
        adminCouponService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除优惠券模板")
    @DeleteMapping("/template/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        adminCouponService.delete(id);
        return Result.success();
    }

    // ==================== 发放管理 ====================

    @Operation(summary = "发放优惠券")
    @PostMapping("/issue")
    public Result<Integer> issue(@Valid @RequestBody CouponIssueRequest request) {
        return Result.success(adminCouponService.issue(request));
    }

    @Operation(summary = "发放记录")
    @GetMapping("/records")
    public Result<PageResult<UserCouponVO>> records(UserCouponQueryRequest request) {
        return Result.success(adminCouponService.pageRecords(request));
    }

    // ==================== 统计 ====================

    @Operation(summary = "优惠券统计")
    @GetMapping("/stats")
    public Result<AdminCouponService.CouponStatsVO> stats() {
        return Result.success(adminCouponService.getStats());
    }
}
