package com.xunjiqianxing.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理后台登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class AdminLoginResponse {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称/商家名称")
    private String nickname;

    @Schema(description = "头像/Logo")
    private String avatar;

    @Schema(description = "角色类型", example = "admin/supplier")
    private String roleType;

    @Schema(description = "角色名称", example = "管理员/供应商名称")
    private String roleName;
}
