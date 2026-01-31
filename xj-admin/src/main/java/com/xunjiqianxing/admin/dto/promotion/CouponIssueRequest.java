package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 发放优惠券请求
 */
@Data
@Schema(description = "发放优惠券请求")
public class CouponIssueRequest {

    @Schema(description = "优惠券模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优惠券模板ID不能为空")
    private Long couponId;

    @Schema(description = "发放类型: 1指定用户 2全部会员", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发放类型不能为空")
    private Integer issueType;

    @Schema(description = "指定用户ID列表(发放类型为1时必填)")
    private List<Long> userIds;

    @Schema(description = "每人发放数量")
    private Integer count = 1;

    @Schema(description = "发放原因")
    private String reason;
}
