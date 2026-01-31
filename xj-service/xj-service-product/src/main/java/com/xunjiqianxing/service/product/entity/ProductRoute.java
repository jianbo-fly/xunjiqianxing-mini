package com.xunjiqianxing.service.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.List;

/**
 * 跟团游扩展表实体
 */
@Data
@TableName(value = "product_route", autoResultMap = true)
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

    /**
     * 行程安排(JSON数组)
     * 结构: [{day, title, activities: [{icon, time, content}], meals, hotel}]
     * 注意: 使用 List<Object> 是因为 JacksonTypeHandler 反序列化嵌套泛型时会返回 LinkedHashMap
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Object> itinerary;

    /**
     * 费用包含(富文本)
     */
    private String costInclude;

    /**
     * 行程天数据结构
     */
    @Data
    public static class ItineraryDay {
        /** 第几天 */
        private Integer day;
        /** 标题，如"北京 → 昆明" */
        private String title;
        /** 当天活动列表 */
        private List<Activity> activities;
        /** 餐食说明 */
        private String meals;
        /** 住宿说明 */
        private String hotel;
    }

    /**
     * 活动项
     */
    @Data
    public static class Activity {
        /** 图标类型: plane/bus/scenic/hotel/food/walk/boat */
        private String icon;
        /** 时间，如"08:00" */
        private String time;
        /** 活动内容 */
        private String content;
    }
}
