package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新配置请求
 */
@Data
@Schema(description = "更新配置请求")
public class ConfigUpdateRequest {

    @Schema(description = "配置键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @Schema(description = "配置值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置值不能为空")
    private String configValue;

    @Schema(description = "类型: string/number/json/boolean")
    private String configType = "string";

    @Schema(description = "备注")
    private String remark;
}
