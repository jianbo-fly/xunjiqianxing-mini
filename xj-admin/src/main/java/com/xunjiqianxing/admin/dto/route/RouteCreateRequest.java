package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建线路请求
 */
@Data
@Schema(description = "创建线路请求")
public class RouteCreateRequest {

    // ========== 基本信息 ==========

    @Schema(description = "线路名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "线路名称不能为空")
    private String name;

    @Schema(description = "副标题")
    private String subtitle;

    @Schema(description = "封面图", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "封面图不能为空")
    private String coverImage;

    @Schema(description = "轮播图")
    private List<String> images;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "分类: domestic国内游 overseas出境游", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类不能为空")
    private String category;

    // ========== 地点信息 ==========

    @Schema(description = "出发城市编码")
    private String cityCode;

    @Schema(description = "出发城市名称")
    private String cityName;

    @Schema(description = "出发城市(显示用)")
    private String departureCity;

    @Schema(description = "目的地")
    private String destination;

    // ========== 价格信息 ==========

    @Schema(description = "原价(划线价)")
    private BigDecimal originalPrice;

    @Schema(description = "最低价")
    private BigDecimal minPrice;

    // ========== 详情信息 ==========

    @Schema(description = "费用不含(富文本)")
    private String costExclude;

    @Schema(description = "预订须知(富文本)")
    private String bookingNotice;

    @Schema(description = "温馨提示(富文本)")
    private String tips;

    @Schema(description = "费用包含(富文本)")
    private String costInclude;

    @Schema(description = "行程安排")
    private List<ItineraryDay> itinerary;

    // ========== 其他 ==========

    @Schema(description = "排序(越大越前)", defaultValue = "0")
    private Integer sortOrder;

    @Schema(description = "是否推荐: 0否 1是", defaultValue = "0")
    private Integer isRecommend;

    @Schema(description = "供应商ID(管理员创建时指定)")
    private Long supplierId;

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
        @Schema(description = "图标类型: plane/bus/scenic/hotel/food/walk/boat/default")
        private String icon;

        @Schema(description = "时间")
        private String time;

        @Schema(description = "内容")
        private String content;
    }
}
