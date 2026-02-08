package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 退款规则设置更新请求
 */
@Data
@Schema(description = "退款规则设置更新请求")
public class RefundSettingsRequest {

    @NotNull(message = "退款规则不能为空")
    @Schema(description = "退款规则列表")
    private List<RefundRuleVO> rules;
}
