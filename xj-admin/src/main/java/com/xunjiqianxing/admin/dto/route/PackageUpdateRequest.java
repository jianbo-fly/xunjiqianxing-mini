package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 更新套餐请求
 */
@Data
@Schema(description = "更新套餐请求")
public class PackageUpdateRequest {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "套餐ID不能为空")
    private Long id;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "基准价(成人价)")
    private BigDecimal basePrice;

    @Schema(description = "儿童基准价")
    private BigDecimal childPrice;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0禁用 1启用")
    private Integer status;

    @Schema(description = "行程天数")
    private Integer days;

    @Schema(description = "行程晚数")
    private Integer nights;

    @Schema(description = "业务属性")
    private Map<String, Object> attrs;
}
