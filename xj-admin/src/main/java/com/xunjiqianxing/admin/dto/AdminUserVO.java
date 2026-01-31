package com.xunjiqianxing.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前用户信息")
public class AdminUserVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称/商家名称")
    private String nickname;

    @Schema(description = "头像/Logo")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "角色类型: admin/supplier")
    private String roleType;

    @Schema(description = "角色: super_admin/admin (仅管理员)")
    private String role;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
}
