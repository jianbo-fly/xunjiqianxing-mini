package com.xunjiqianxing.service.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 价格库存表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_price_stock")
public class ProductPriceStock extends BaseEntity {

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 成人价格
     */
    private BigDecimal price;

    /**
     * 儿童价格
     */
    private BigDecimal childPrice;

    /**
     * 总库存
     */
    private Integer stock;

    /**
     * 已售数量
     */
    private Integer sold;

    /**
     * 锁定数量
     */
    private Integer locked;

    /**
     * 状态: 0不可售 1可售
     */
    private Integer status;
}
