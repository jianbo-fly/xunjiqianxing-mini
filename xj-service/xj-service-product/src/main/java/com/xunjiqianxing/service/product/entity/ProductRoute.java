package com.xunjiqianxing.service.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 跟团游扩展表实体
 */
@Data
@TableName("product_route")
public class ProductRoute {

    /**
     * 商品ID
     */
    @TableId
    private Long productId;

    /**
     * 分类: domestic国内游 overseas出境游
     */
    private String category;

    /**
     * 出发城市
     */
    private String departureCity;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 费用不含(富文本)
     */
    private String costExclude;

    /**
     * 预订须知(富文本)
     */
    private String bookingNotice;

    /**
     * 温馨提示(富文本)
     */
    private String tips;
}
