package com.xunjiqianxing.service.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * SKU表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "product_sku", autoResultMap = true)
public class ProductSku extends BaseEntity {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * SKU名称
     */
    private String name;

    /**
     * 标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 业务属性(JSON)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attrs;

    /**
     * 基准价
     */
    private BigDecimal basePrice;

    /**
     * 儿童基准价
     */
    private BigDecimal childPrice;

    /**
     * 状态: 0禁用 1启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
