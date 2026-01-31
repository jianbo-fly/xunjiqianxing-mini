package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新优惠券模板请求
 */
@Data
@Schema(description = "更新优惠券模板请求")
public class CouponUpdateRequest {

    @Schema(description = "优惠券ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优惠券ID不能为空")
    private Long id;

    @Schema(description = "券名称")
    private String name;

    @Schema(description = "满减门槛金额")
    private BigDecimal threshold;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "折扣率")
    private BigDecimal discount;

    @Schema(description = "最高减免金额")
    private BigDecimal maxAmount;

    @Schema(description = "适用范围: 0全场 1指定线路 2指定分类")
    private Integer scope;

    @Schema(description = "适用线路/分类ID列表")
    private String scopeIds;

    @Schema(description = "有效期结束时间")
    private LocalDateTime validEnd;

    @Schema(description = "发行总量")
    private Integer totalCount;

    @Schema(description = "每人限领数量")
    private Integer perLimit;

    @Schema(description = "是否仅会员可用")
    private Integer memberOnly;

    @Schema(description = "描述")
    private String description;
}
