package com.xunjiqianxing.service.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "order_main", autoResultMap = true)
public class OrderMain extends BaseEntity {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 成人数
     */
    private Integer adultCount;

    /**
     * 儿童数
     */
    private Integer childCount;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 成人单价
     */
    private BigDecimal adultPrice;

    /**
     * 儿童单价
     */
    private BigDecimal childPrice;

    /**
     * 订单总额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 已退金额
     */
    private BigDecimal refundAmount;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券金额
     */
    private BigDecimal couponAmount;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 订单状态: 0待支付 1已支付 2已确认 3出行中 4已完成 5已取消 6退款中 7已退款 8已关闭
     */
    private Integer status;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 支付流水号
     */
    private String payTradeNo;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消类型
     */
    private Integer cancelType;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 扩展数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extData;

    /**
     * 推广员ID
     */
    private Long promoterId;

    /**
     * 用户备注
     */
    private String remark;

    /**
     * 管理员备注
     */
    private String adminRemark;

    /**
     * 支付超时时间
     */
    private LocalDateTime expireAt;

    /**
     * 是否删除
     */
    private Integer isDeleted;
}
