package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 延长会员时间请求
 */
@Data
@Schema(description = "延长会员时间请求")
public class MemberExtendRequest {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "延长天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "延长天数不能为空")
    @Min(value = 1, message = "延长天数必须大于0")
    private Integer days;

    @Schema(description = "原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原因不能为空")
    private String reason;
}
