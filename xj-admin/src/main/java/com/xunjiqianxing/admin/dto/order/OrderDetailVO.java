package com.xunjiqianxing.admin.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单详情响应
 */
@Data
@Schema(description = "订单详情响应")
public class OrderDetailVO {

    // ========== 基本信息 ==========

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "业务类型")
    private String bizType;

    // ========== 用户信息 ==========

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户手机号")
    private String userPhone;

    // ========== 商品信息 ==========

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "SKU名称")
    private String skuName;

    // ========== 行程信息 ==========

    @Schema(description = "出发日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "天数")
    private Integer days;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "数量")
    private Integer quantity;

    // ========== 价格信息 ==========

    @Schema(description = "成人单价")
    private BigDecimal adultPrice;

    @Schema(description = "儿童单价")
    private BigDecimal childPrice;

    @Schema(description = "订单总额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "优惠券金额")
    private BigDecimal couponAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    // ========== 联系人信息 ==========

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    // ========== 出行人信息 ==========

    @Schema(description = "出行人列表")
    private List<TravelerVO> travelers;

    // ========== 状态信息 ==========

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "订单状态描述")
    private String statusDesc;

    @Schema(description = "支付状态")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "支付流水号")
    private String payTradeNo;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    // ========== 供应商信息 ==========

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    // ========== 其他信息 ==========

    @Schema(description = "推广员ID")
    private Long promoterId;

    @Schema(description = "用户备注")
    private String remark;

    @Schema(description = "管理员备注")
    private String adminRemark;

    @Schema(description = "扩展数据")
    private Map<String, Object> extData;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    // ========== 退款信息 ==========

    @Schema(description = "退款记录")
    private RefundVO refund;

    /**
     * 出行人信息
     */
    @Data
    @Schema(description = "出行人信息")
    public static class TravelerVO {

        @Schema(description = "姓名")
        private String name;

        @Schema(description = "身份证号(脱敏)")
        private String idCard;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "类型: 1成人 2儿童")
        private Integer travelerType;

        @Schema(description = "类型描述")
        private String travelerTypeDesc;
    }

    /**
     * 退款信息
     */
    @Data
    @Schema(description = "退款信息")
    public static class RefundVO {

        @Schema(description = "退款编号")
        private String refundNo;

        @Schema(description = "申请退款金额")
        private BigDecimal refundAmount;

        @Schema(description = "实际退款金额")
        private BigDecimal actualAmount;

        @Schema(description = "退款比例(%)")
        private Integer refundRatio;

        @Schema(description = "退款原因")
        private String reason;

        @Schema(description = "状态: 0待审核 1已通过 2已驳回")
        private Integer status;

        @Schema(description = "审核时间")
        private LocalDateTime auditTime;

        @Schema(description = "审核备注")
        private String auditRemark;

        @Schema(description = "申请时间")
        private LocalDateTime createdAt;
    }
}
