package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 价格日历设置请求
 */
@Data
@Schema(description = "价格日历设置请求")
public class PriceCalendarRequest {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "套餐ID不能为空")
    private Long skuId;

    @Schema(description = "价格日历列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "价格日历不能为空")
    private List<PriceItem> prices;

    /**
     * 单日价格项
     */
    @Data
    @Schema(description = "单日价格项")
    public static class PriceItem {

        @Schema(description = "日期", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "日期不能为空")
        private LocalDate date;

        @Schema(description = "成人价格", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "成人价格不能为空")
        private BigDecimal price;

        @Schema(description = "儿童价格")
        private BigDecimal childPrice;

        @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "库存不能为空")
        private Integer stock;

        @Schema(description = "状态: 0不可售 1可售", defaultValue = "1")
        private Integer status = 1;
    }
}
