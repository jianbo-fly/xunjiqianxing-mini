package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.system.*;
import com.xunjiqianxing.admin.service.AdminSupplierService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商管理接口
 */
@Tag(name = "供应商管理")
@RestController
@RequestMapping("/admin/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final AdminSupplierService adminSupplierService;

    @Operation(summary = "供应商列表")
    @GetMapping("/list")
    public Result<PageResult<SupplierVO>> list(SupplierQueryRequest request) {
        return Result.success(adminSupplierService.pageList(request));
    }

    @Operation(summary = "供应商详情")
    @GetMapping("/{id}")
    public Result<SupplierVO> detail(@PathVariable Long id) {
        return Result.success(adminSupplierService.getDetail(id));
    }

    @Operation(summary = "创建供应商")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody SupplierCreateRequest request) {
        return Result.success(adminSupplierService.create(request));
    }

    @Operation(summary = "更新供应商")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SupplierUpdateRequest request) {
        adminSupplierService.update(request);
        return Result.success();
    }

    @Operation(summary = "启用/禁用供应商")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0禁用 1正常") @RequestParam Integer status) {
        adminSupplierService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/resetPassword")
    public Result<String> resetPassword(@PathVariable Long id) {
        String newPassword = adminSupplierService.resetPassword(id);
        return Result.success(newPassword);
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminSupplierService.delete(id);
        return Result.success();
    }
}
