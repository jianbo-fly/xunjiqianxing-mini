package com.xunjiqianxing.service.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "product_main", autoResultMap = true)
public class ProductMain extends BaseEntity {

    /**
     * 业务类型: route/car/hotel/ticket/transfer/food/rental/insurance
     */
    private String bizType;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 轮播图
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /**
     * 标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 最低价
     */
    private BigDecimal minPrice;

    /**
     * 原价(划线价)
     */
    private BigDecimal originalPrice;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 状态: 0下架 1上架
     */
    private Integer status;

    /**
     * 审核状态: 0待审核 1通过 2驳回
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 排序(越大越前)
     */
    private Integer sortOrder;

    /**
     * 是否推荐
     */
    private Integer isRecommend;

    /**
     * 是否删除
     */
    private Integer isDeleted;
}
