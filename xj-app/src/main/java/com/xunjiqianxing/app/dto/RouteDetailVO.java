package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 线路详情VO
 */
@Data
@Schema(description = "线路详情")
public class RouteDetailVO {

    @Schema(description = "线路ID")
    private Long id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "轮播图")
    private List<String> images;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "出发城市")
    private String cityName;

    @Schema(description = "最低价")
    private BigDecimal minPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "评分")
    private BigDecimal score;

    // 扩展信息
    @Schema(description = "分类")
    private String category;

    @Schema(description = "出发城市")
    private String departureCity;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "费用不含")
    private String costExclude;

    @Schema(description = "预订须知")
    private String bookingNotice;

    @Schema(description = "温馨提示")
    private String tips;

    @Schema(description = "费用包含")
    private String costInclude;

    @Schema(description = "行程安排")
    private List<ItineraryDay> itinerary;

    /**
     * 行程天
     */
    @Data
    @Schema(description = "行程天")
    public static class ItineraryDay {
        @Schema(description = "第几天")
        private Integer day;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "活动列表")
        private List<Activity> activities;

        @Schema(description = "餐食说明")
        private String meals;

        @Schema(description = "住宿说明")
        private String hotel;
    }

    /**
     * 活动项
     */
    @Data
    @Schema(description = "活动项")
    public static class Activity {
        @Schema(description = "图标类型")
        private String icon;

        @Schema(description = "时间")
        private String time;

        @Schema(description = "内容")
        private String content;
    }
}
