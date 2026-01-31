package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信绑定手机号请求
 */
@Data
@Schema(description = "微信绑定手机号请求")
public class BindPhoneByWxRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信getPhoneNumber返回的code")
    private String code;
}
