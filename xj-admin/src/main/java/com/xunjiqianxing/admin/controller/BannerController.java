package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.content.*;
import com.xunjiqianxing.admin.service.AdminBannerService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Banner管理接口
 */
@Tag(name = "Banner管理")
@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class BannerController {

    private final AdminBannerService adminBannerService;

    @Operation(summary = "Banner列表(分页)")
    @GetMapping("/list")
    public Result<PageResult<BannerVO>> list(BannerQueryRequest request) {
        return Result.success(adminBannerService.pageList(request));
    }

    @Operation(summary = "Banner列表(全部)")
    @GetMapping("/listAll")
    public Result<List<BannerVO>> listAll() {
        return Result.success(adminBannerService.listAll());
    }

    @Operation(summary = "Banner详情")
    @GetMapping("/{id}")
    public Result<BannerVO> detail(@PathVariable Long id) {
        return Result.success(adminBannerService.getDetail(id));
    }

    @Operation(summary = "创建Banner")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody BannerCreateRequest request) {
        return Result.success(adminBannerService.create(request));
    }

    @Operation(summary = "更新Banner")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody BannerUpdateRequest request) {
        adminBannerService.update(request);
        return Result.success();
    }

    @Operation(summary = "启用/禁用Banner")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态: 0禁用 1启用") @RequestParam Integer status) {
        adminBannerService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除Banner")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminBannerService.delete(id);
        return Result.success();
    }

    @Operation(summary = "调整排序")
    @PutMapping("/sort")
    public Result<Void> sort(@Valid @RequestBody SortRequest request) {
        adminBannerService.sort(request);
        return Result.success();
    }
}
