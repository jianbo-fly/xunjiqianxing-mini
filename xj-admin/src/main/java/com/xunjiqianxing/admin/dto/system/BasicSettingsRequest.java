package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 基础设置更新请求
 */
@Data
@Schema(description = "基础设置更新请求")
public class BasicSettingsRequest {

    @NotBlank(message = "客服电话不能为空")
    @Schema(description = "客服电话")
    private String customerServicePhone;

    @Schema(description = "企微客服链接")
    private String wecomServiceUrl;

    @NotNull(message = "支付超时时间不能为空")
    @Min(value = 1, message = "支付超时时间不能小于1分钟")
    @Schema(description = "支付超时时间（分钟）")
    private Integer paymentTimeout;

    @NotNull(message = "儿童年龄上限不能为空")
    @Min(value = 1, message = "儿童年龄上限不能小于1岁")
    @Schema(description = "儿童年龄上限（周岁）")
    private Integer childAgeLimit;
}
