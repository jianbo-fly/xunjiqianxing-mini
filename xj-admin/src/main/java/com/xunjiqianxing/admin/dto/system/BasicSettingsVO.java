package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 基础设置VO
 */
@Data
@Schema(description = "基础设置VO")
public class BasicSettingsVO {

    @Schema(description = "客服电话")
    private String customerServicePhone;

    @Schema(description = "企微客服链接")
    private String wecomServiceUrl;

    @Schema(description = "支付超时时间（分钟）")
    private Integer paymentTimeout;

    @Schema(description = "儿童年龄上限（周岁）")
    private Integer childAgeLimit;
}
