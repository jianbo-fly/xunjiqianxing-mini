package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 线路套餐VO
 */
@Data
@Schema(description = "线路套餐")
public class RoutePackageVO {

    @Schema(description = "套餐ID")
    private Long id;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "业务属性")
    private Map<String, Object> attrs;

    @Schema(description = "成人基准价")
    private BigDecimal basePrice;

    @Schema(description = "儿童基准价")
    private BigDecimal childPrice;
}
