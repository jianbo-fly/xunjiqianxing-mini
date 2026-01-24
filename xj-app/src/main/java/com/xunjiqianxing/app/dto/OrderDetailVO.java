package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
@Schema(description = "订单详情")
public class OrderDetailVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "套餐名称")
    private String skuName;

    @Schema(description = "出行日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "天数")
    private Integer days;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "成人单价")
    private BigDecimal adultPrice;

    @Schema(description = "儿童单价")
    private BigDecimal childPrice;

    @Schema(description = "订单总额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "支付超时时间")
    private LocalDateTime expireAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "出行人列表")
    private List<TravelerVO> travelers;

    /**
     * 出行人VO
     */
    @Data
    @Schema(description = "出行人信息")
    public static class TravelerVO {

        @Schema(description = "姓名")
        private String name;

        @Schema(description = "身份证号(脱敏)")
        private String idCard;

        @Schema(description = "手机号(脱敏)")
        private String phone;

        @Schema(description = "类型: 1成人 2儿童")
        private Integer travelerType;
    }
}
