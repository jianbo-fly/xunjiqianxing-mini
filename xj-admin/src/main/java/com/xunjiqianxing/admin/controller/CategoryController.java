package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.content.*;
import com.xunjiqianxing.admin.service.AdminCategoryService;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理接口
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final AdminCategoryService adminCategoryService;

    @Operation(summary = "分类列表")
    @GetMapping("/list")
    public Result<List<CategoryVO>> list(
            @Parameter(description = "业务类型") @RequestParam(required = false) String bizType) {
        return Result.success(adminCategoryService.list(bizType));
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> detail(@PathVariable Long id) {
        return Result.success(adminCategoryService.getDetail(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CategoryCreateRequest request) {
        return Result.success(adminCategoryService.create(request));
    }

    @Operation(summary = "更新分类")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody CategoryUpdateRequest request) {
        adminCategoryService.update(request);
        return Result.success();
    }

    @Operation(summary = "启用/禁用分类")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0禁用 1启用") @RequestParam Integer status) {
        adminCategoryService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminCategoryService.delete(id);
        return Result.success();
    }

    @Operation(summary = "调整排序")
    @PutMapping("/sort")
    public Result<Void> sort(@Valid @RequestBody SortRequest request) {
        adminCategoryService.sort(request);
        return Result.success();
    }
}
