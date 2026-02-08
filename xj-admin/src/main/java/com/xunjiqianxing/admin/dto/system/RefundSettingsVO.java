package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 退款规则设置VO
 */
@Data
@Schema(description = "退款规则设置VO")
public class RefundSettingsVO {

    @Schema(description = "退款规则列表")
    private List<RefundRuleVO> rules;
}
