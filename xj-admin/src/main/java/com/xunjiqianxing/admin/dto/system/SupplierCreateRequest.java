package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建供应商请求
 */
@Data
@Schema(description = "创建供应商请求")
public class SupplierCreateRequest {

    @Schema(description = "商家名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商家名称不能为空")
    private String name;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "资质证书图片")
    private List<String> licenseImages;

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    private String username;

    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录密码不能为空")
    private String password;
}
