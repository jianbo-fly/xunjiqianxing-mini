package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.custom.*;
import com.xunjiqianxing.admin.service.AdminCustomService;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 定制需求管理接口
 */
@Tag(name = "定制需求管理")
@RestController
@RequestMapping("/admin/custom")
@RequiredArgsConstructor
public class CustomController {

    private final AdminCustomService adminCustomService;

    @Operation(summary = "定制需求列表")
    @GetMapping("/list")
    public Result<PageResult<CustomListVO>> list(CustomQueryRequest request) {
        return Result.success(adminCustomService.pageList(request));
    }

    @Operation(summary = "定制需求详情")
    @GetMapping("/{id}")
    public Result<CustomDetailVO> detail(@PathVariable Long id) {
        return Result.success(adminCustomService.getDetail(id));
    }

    @Operation(summary = "更新需求状态")
    @PostMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CustomStatusRequest request) {
        adminCustomService.updateStatus(id, request);
        return Result.success();
    }

    @Operation(summary = "添加跟进记录")
    @PostMapping("/{id}/follow")
    public Result<Void> addFollow(
            @PathVariable Long id,
            @Valid @RequestBody CustomFollowRequest request) {
        adminCustomService.addFollowRecord(id, request);
        return Result.success();
    }
}
