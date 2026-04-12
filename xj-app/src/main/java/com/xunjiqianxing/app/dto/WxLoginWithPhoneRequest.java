package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信一键登录 + 手机号授权（同步完成登录与手机号绑定）
 */
@Data
@Schema(description = "微信一键登录 + 手机号授权请求")
public class WxLoginWithPhoneRequest {

    @NotBlank(message = "登录code不能为空")
    @Schema(description = "wx.login 返回的 code")
    private String loginCode;

    @NotBlank(message = "手机号code不能为空")
    @Schema(description = "getPhoneNumber 返回的 code")
    private String phoneCode;

    @Schema(description = "推广员邀请码（可选）")
    private String promoterCode;
}
