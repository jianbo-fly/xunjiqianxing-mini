package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "是否新用户")
    private Boolean isNewUser;
}
