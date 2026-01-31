package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券模板请求
 */
@Data
@Schema(description = "创建优惠券模板请求")
public class CouponCreateRequest {

    @Schema(description = "券名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "券名称不能为空")
    private String name;

    @Schema(description = "券类型: 1满减券 2折扣券 3无门槛券", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "券类型不能为空")
    private Integer type;

    @Schema(description = "满减门槛金额")
    private BigDecimal threshold;

    @Schema(description = "优惠金额（满减券/无门槛券）")
    private BigDecimal amount;

    @Schema(description = "折扣率（折扣券，如0.9表示9折）")
    private BigDecimal discount;

    @Schema(description = "最高减免金额（折扣券用）")
    private BigDecimal maxAmount;

    @Schema(description = "适用范围: 0全场 1指定线路 2指定分类")
    private Integer scope = 0;

    @Schema(description = "适用线路/分类ID列表，逗号分隔")
    private String scopeIds;

    @Schema(description = "有效期类型: 1固定时间 2领取后N天", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "有效期类型不能为空")
    private Integer validType;

    @Schema(description = "有效期开始时间(固定时间类型)")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束时间(固定时间类型)")
    private LocalDateTime validEnd;

    @Schema(description = "领取后有效天数(动态类型)")
    private Integer validDays;

    @Schema(description = "发行总量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发行总量不能为空")
    private Integer totalCount;

    @Schema(description = "每人限领数量")
    private Integer perLimit = 1;

    @Schema(description = "是否仅会员可用")
    private Integer memberOnly = 0;

    @Schema(description = "描述")
    private String description;
}
