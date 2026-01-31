package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.user.*;
import com.xunjiqianxing.admin.service.AdminUserService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public Result<PageResult<UserListVO>> list(UserQueryRequest request) {
        return Result.success(adminUserService.pageList(request));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminUserService.getDetail(id));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0禁用 1正常") @RequestParam Integer status) {
        adminUserService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "调整积分")
    @PostMapping("/points/adjust")
    public Result<Void> adjustPoints(@Valid @RequestBody PointsAdjustRequest request) {
        adminUserService.adjustPoints(request);
        return Result.success();
    }
}
