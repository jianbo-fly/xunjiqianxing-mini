package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新供应商请求
 */
@Data
@Schema(description = "更新供应商请求")
public class SupplierUpdateRequest {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "资质证书图片")
    private List<String> licenseImages;
}
