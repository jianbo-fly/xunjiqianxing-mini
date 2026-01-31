package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.user.*;
import com.xunjiqianxing.admin.service.AdminLeaderService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 领队管理接口
 */
@Tag(name = "领队管理")
@RestController
@RequestMapping("/admin/leader")
@RequiredArgsConstructor
public class LeaderController {

    private final AdminLeaderService adminLeaderService;

    // ==================== 申请管理 ====================

    @Operation(summary = "领队申请列表")
    @GetMapping("/applies")
    public Result<PageResult<LeaderApplyVO>> applies(LeaderApplyQueryRequest request) {
        return Result.success(adminLeaderService.pageApplies(request));
    }

    @Operation(summary = "申请详情")
    @GetMapping("/applies/{id}")
    public Result<LeaderApplyVO> applyDetail(@PathVariable Long id) {
        return Result.success(adminLeaderService.getApplyDetail(id));
    }

    @Operation(summary = "审核申请")
    @PostMapping("/applies/audit")
    public Result<Void> auditApply(@Valid @RequestBody LeaderApplyAuditRequest request) {
        adminLeaderService.auditApply(request);
        return Result.success();
    }

    @Operation(summary = "待审核申请数量")
    @GetMapping("/applies/pendingCount")
    public Result<Long> pendingApplyCount() {
        return Result.success(adminLeaderService.getPendingApplyCount());
    }

    // ==================== 领队管理 ====================

    @Operation(summary = "领队列表")
    @GetMapping("/list")
    public Result<PageResult<LeaderListVO>> list(LeaderApplyQueryRequest request) {
        return Result.success(adminLeaderService.pageLeaders(request));
    }

    @Operation(summary = "启用/禁用领队")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0禁用 1正常") @RequestParam Integer status) {
        adminLeaderService.updateStatus(id, status);
        return Result.success();
    }
}
