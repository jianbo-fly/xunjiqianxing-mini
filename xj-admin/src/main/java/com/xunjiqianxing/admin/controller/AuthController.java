package com.xunjiqianxing.admin.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.xunjiqianxing.admin.dto.AdminLoginRequest;
import com.xunjiqianxing.admin.dto.AdminLoginResponse;
import com.xunjiqianxing.admin.dto.AdminUserVO;
import com.xunjiqianxing.admin.dto.ChangePasswordRequest;
import com.xunjiqianxing.admin.entity.SystemAdmin;
import com.xunjiqianxing.admin.mapper.SystemAdminMapper;
import com.xunjiqianxing.admin.service.AdminAuthService;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.common.utils.IdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService adminAuthService;
    private final SystemAdminMapper systemAdminMapper;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.success(adminAuthService.login(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        adminAuthService.logout();
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<AdminUserVO> getCurrentUser() {
        return Result.success(adminAuthService.getCurrentUser());
    }

    @Operation(summary = "修改密码")
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        adminAuthService.changePassword(request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    /**
     * 开发环境 - 初始化或重置超级管理员
     * 仅在 dev 环境可用
     */
    @Operation(summary = "初始化/重置超级管理员(仅开发环境)")
    @PostMapping("/initAdmin")
    public Result<String> initAdmin() {
        // 检查是否已存在
        Long count = systemAdminMapper.selectCount(null);
        if (count > 0) {
            // 已存在则重置密码
            SystemAdmin existing = systemAdminMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemAdmin>()
                    .eq(SystemAdmin::getUsername, "admin")
            );
            if (existing != null) {
                existing.setPassword(BCrypt.hashpw("admin123"));
                systemAdminMapper.updateById(existing);
                return Result.success("密码已重置！账号: admin, 密码: admin123");
            }
        }

        // 创建超级管理员
        SystemAdmin admin = new SystemAdmin();
        admin.setId(IdGenerator.nextId());
        admin.setUsername("admin");
        admin.setPassword(BCrypt.hashpw("admin123"));
        admin.setNickname("超级管理员");
        admin.setRole("super_admin");
        admin.setStatus(1);
        systemAdminMapper.insert(admin);

        return Result.success("初始化成功！账号: admin, 密码: admin123");
    }
}
