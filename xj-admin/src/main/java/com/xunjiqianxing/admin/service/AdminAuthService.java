package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunjiqianxing.admin.dto.AdminLoginRequest;
import com.xunjiqianxing.admin.dto.AdminLoginResponse;
import com.xunjiqianxing.admin.dto.AdminUserVO;
import com.xunjiqianxing.admin.entity.SystemAdmin;
import com.xunjiqianxing.admin.entity.SystemSupplier;
import com.xunjiqianxing.admin.mapper.SystemAdminMapper;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 管理后台认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final SystemAdminMapper systemAdminMapper;
    private final SystemSupplierMapper systemSupplierMapper;

    /**
     * 登录
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 先查找管理员
        SystemAdmin admin = systemAdminMapper.selectOne(
                new LambdaQueryWrapper<SystemAdmin>()
                        .eq(SystemAdmin::getUsername, username)
        );

        if (admin != null) {
            // 管理员登录 - 使用BCrypt验证
            if (!BCrypt.checkpw(password, admin.getPassword())) {
                throw new BizException("密码错误");
            }
            if (admin.getStatus() != 1) {
                throw new BizException("账号已被禁用");
            }

            // 更新最后登录时间
            admin.setLastLoginAt(LocalDateTime.now());
            systemAdminMapper.updateById(admin);

            // Sa-Token登录，使用admin_前缀区分
            String loginId = "admin_" + admin.getId();
            StpUtil.login(loginId);
            StpUtil.getSession().set("roleType", "admin");
            StpUtil.getSession().set("role", admin.getRole());
            StpUtil.getSession().set("userId", admin.getId());

            return AdminLoginResponse.builder()
                    .token(StpUtil.getTokenValue())
                    .userId(admin.getId())
                    .username(admin.getUsername())
                    .nickname(admin.getNickname())
                    .avatar(admin.getAvatar())
                    .roleType("admin")
                    .roleName(getRoleName(admin.getRole()))
                    .build();
        }

        // 查找供应商
        SystemSupplier supplier = systemSupplierMapper.selectOne(
                new LambdaQueryWrapper<SystemSupplier>()
                        .eq(SystemSupplier::getUsername, username)
        );

        if (supplier != null) {
            // 供应商登录 - 使用BCrypt验证
            if (!BCrypt.checkpw(password, supplier.getPassword())) {
                throw new BizException("密码错误");
            }
            if (supplier.getStatus() != 1) {
                throw new BizException("账号已被禁用");
            }

            // 更新最后登录时间
            supplier.setLastLoginAt(LocalDateTime.now());
            systemSupplierMapper.updateById(supplier);

            // Sa-Token登录，使用supplier_前缀区分
            String loginId = "supplier_" + supplier.getId();
            StpUtil.login(loginId);
            StpUtil.getSession().set("roleType", "supplier");
            StpUtil.getSession().set("supplierId", supplier.getId());

            return AdminLoginResponse.builder()
                    .token(StpUtil.getTokenValue())
                    .userId(supplier.getId())
                    .username(supplier.getUsername())
                    .nickname(supplier.getName())
                    .avatar(supplier.getLogo())
                    .roleType("supplier")
                    .roleName(supplier.getName())
                    .build();
        }

        throw new BizException("用户名或密码错误");
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前用户信息
     */
    public AdminUserVO getCurrentUser() {
        String roleType = (String) StpUtil.getSession().get("roleType");

        if ("admin".equals(roleType)) {
            Long userId = (Long) StpUtil.getSession().get("userId");
            SystemAdmin admin = systemAdminMapper.selectById(userId);
            if (admin == null) {
                throw new BizException("用户不存在");
            }

            return AdminUserVO.builder()
                    .userId(admin.getId())
                    .username(admin.getUsername())
                    .nickname(admin.getNickname())
                    .avatar(admin.getAvatar())
                    .phone(admin.getPhone())
                    .roleType("admin")
                    .role(admin.getRole())
                    .permissions(getPermissions(admin.getRole()))
                    .lastLoginAt(admin.getLastLoginAt())
                    .build();
        } else if ("supplier".equals(roleType)) {
            Long supplierId = (Long) StpUtil.getSession().get("supplierId");
            SystemSupplier supplier = systemSupplierMapper.selectById(supplierId);
            if (supplier == null) {
                throw new BizException("用户不存在");
            }

            return AdminUserVO.builder()
                    .userId(supplier.getId())
                    .username(supplier.getUsername())
                    .nickname(supplier.getName())
                    .avatar(supplier.getLogo())
                    .phone(supplier.getPhone())
                    .roleType("supplier")
                    .role(null)
                    .permissions(getSupplierPermissions())
                    .lastLoginAt(supplier.getLastLoginAt())
                    .build();
        }

        throw new BizException("未知的角色类型");
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        String roleType = (String) StpUtil.getSession().get("roleType");

        if ("admin".equals(roleType)) {
            Long userId = (Long) StpUtil.getSession().get("userId");
            SystemAdmin admin = systemAdminMapper.selectById(userId);

            if (!BCrypt.checkpw(oldPassword, admin.getPassword())) {
                throw new BizException("原密码错误");
            }

            admin.setPassword(encryptPassword(newPassword));
            systemAdminMapper.updateById(admin);
        } else if ("supplier".equals(roleType)) {
            Long supplierId = (Long) StpUtil.getSession().get("supplierId");
            SystemSupplier supplier = systemSupplierMapper.selectById(supplierId);

            if (!BCrypt.checkpw(oldPassword, supplier.getPassword())) {
                throw new BizException("原密码错误");
            }

            supplier.setPassword(encryptPassword(newPassword));
            systemSupplierMapper.updateById(supplier);
        }

        // 修改密码后退出登录
        StpUtil.logout();
    }

    /**
     * 密码加密 (BCrypt)
     */
    public String encryptPassword(String password) {
        return BCrypt.hashpw(password);
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(String role) {
        if ("super_admin".equals(role)) {
            return "超级管理员";
        } else if ("admin".equals(role)) {
            return "普通管理员";
        }
        return "管理员";
    }

    /**
     * 获取管理员权限列表
     */
    private List<String> getPermissions(String role) {
        if ("super_admin".equals(role)) {
            // 超级管理员拥有所有权限
            return Arrays.asList(
                    "route:*",
                    "order:*",
                    "refund:*",
                    "custom:*",
                    "member:*",
                    "leader:*",
                    "promoter:*",
                    "supplier:*",
                    "user:*",
                    "banner:*",
                    "category:*",
                    "config:*",
                    "file:*"
            );
        } else if ("admin".equals(role)) {
            // 普通管理员权限
            return Arrays.asList(
                    "route:view",
                    "order:view",
                    "order:confirm",
                    "refund:view",
                    "custom:*",
                    "member:view",
                    "leader:view",
                    "promoter:view",
                    "banner:*",
                    "category:*",
                    "file:*"
            );
        }
        return Collections.emptyList();
    }

    /**
     * 获取供应商权限列表
     */
    private List<String> getSupplierPermissions() {
        return Arrays.asList(
                "route:own:*",
                "order:own:view",
                "order:own:confirm",
                "file:*"
        );
    }
}
