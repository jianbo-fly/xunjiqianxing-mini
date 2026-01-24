package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
@Schema(description = "微信登录请求")
public class WxLoginRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信临时登录凭证code", required = true)
    private String code;
}
