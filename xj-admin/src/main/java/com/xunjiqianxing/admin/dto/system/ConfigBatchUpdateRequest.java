package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新配置请求
 */
@Data
@Schema(description = "批量更新配置请求")
public class ConfigBatchUpdateRequest {

    @Schema(description = "配置列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "配置列表不能为空")
    @Valid
    private List<ConfigUpdateRequest> configs;
}
