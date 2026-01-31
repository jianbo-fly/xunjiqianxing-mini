package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.dto.system.*;
import com.xunjiqianxing.admin.service.AdminConfigService;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置接口
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class ConfigController {

    private final AdminConfigService adminConfigService;

    @Operation(summary = "配置列表")
    @GetMapping("/list")
    public Result<List<ConfigVO>> list() {
        return Result.success(adminConfigService.listAll());
    }

    @Operation(summary = "配置Map")
    @GetMapping("/map")
    public Result<Map<String, String>> map() {
        return Result.success(adminConfigService.getConfigMap());
    }

    @Operation(summary = "获取配置值")
    @GetMapping("/value")
    public Result<String> getValue(@RequestParam String key) {
        return Result.success(adminConfigService.getValue(key));
    }

    @Operation(summary = "更新配置")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ConfigUpdateRequest request) {
        adminConfigService.update(request);
        return Result.success();
    }

    @Operation(summary = "批量更新配置")
    @PutMapping("/batch")
    public Result<Void> batchUpdate(@Valid @RequestBody ConfigBatchUpdateRequest request) {
        adminConfigService.batchUpdate(request);
        return Result.success();
    }

    @Operation(summary = "删除配置")
    @DeleteMapping
    public Result<Void> delete(@RequestParam String key) {
        adminConfigService.delete(key);
        return Result.success();
    }
}
