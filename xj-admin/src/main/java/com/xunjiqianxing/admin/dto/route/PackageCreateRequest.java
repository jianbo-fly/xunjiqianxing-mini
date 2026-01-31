package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建套餐请求
 */
@Data
@Schema(description = "创建套餐请求")
public class PackageCreateRequest {

    @Schema(description = "所属线路ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "线路ID不能为空")
    private Long productId;

    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "套餐名称不能为空")
    private String name;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "基准价(成人价)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基准价不能为空")
    private BigDecimal basePrice;

    @Schema(description = "儿童基准价")
    private BigDecimal childPrice;

    @Schema(description = "排序(越小越前)")
    private Integer sortOrder;

    @Schema(description = "行程天数")
    private Integer days;

    @Schema(description = "行程晚数")
    private Integer nights;

    @Schema(description = "业务属性(行程天数、晚数等)")
    private Map<String, Object> attrs;
}
