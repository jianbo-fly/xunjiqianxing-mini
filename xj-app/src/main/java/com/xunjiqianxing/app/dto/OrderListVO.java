package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单列表VO
 */
@Data
@Schema(description = "订单列表项")
public class OrderListVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "套餐名称")
    private String skuName;

    @Schema(description = "出行日期")
    private LocalDate startDate;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
