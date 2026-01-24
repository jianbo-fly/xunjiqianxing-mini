package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 价格日历VO
 */
@Data
@Schema(description = "价格日历")
public class PriceCalendarVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "成人价格")
    private BigDecimal price;

    @Schema(description = "儿童价格")
    private BigDecimal childPrice;

    @Schema(description = "剩余库存")
    private Integer stock;

    @Schema(description = "是否可售")
    private Boolean available;
}
