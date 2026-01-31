package com.xunjiqianxing.admin.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 线路详情响应
 */
@Data
@Schema(description = "线路详情响应")
public class RouteDetailVO {

    // ========== 基本信息 ==========

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

    @Schema(description = "分类")
    private String category;

    // ========== 地点信息 ==========

    @Schema(description = "出发城市编码")
    private String cityCode;

    @Schema(description = "出发城市名称")
    private String cityName;

    @Schema(description = "出发城市(显示)")
    private String departureCity;

    @Schema(description = "目的地")
    private String destination;

    // ========== 价格信息 ==========

    @Schema(description = "最低价")
    private BigDecimal minPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    // ========== 统计信息 ==========

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "评分")
    private BigDecimal score;

    // ========== 状态信息 ==========

    @Schema(description = "状态: 0下架 1上架")
    private Integer status;

    @Schema(description = "审核状态: 0待审核 1通过 2驳回")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否推荐")
    private Integer isRecommend;

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

    // ========== 供应商信息 ==========

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    // ========== 套餐信息 ==========

    @Schema(description = "套餐列表")
    private List<PackageVO> packages;

    // ========== 时间信息 ==========

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 套餐信息
     */
    @Data
    @Schema(description = "套餐信息")
    public static class PackageVO {

        @Schema(description = "套餐ID")
        private Long id;

        @Schema(description = "套餐名称")
        private String name;

        @Schema(description = "标签")
        private List<String> tags;

        @Schema(description = "基准价")
        private BigDecimal basePrice;

        @Schema(description = "儿童基准价")
        private BigDecimal childPrice;

        @Schema(description = "状态: 0禁用 1启用")
        private Integer status;

        @Schema(description = "排序")
        private Integer sortOrder;

        @Schema(description = "行程天数")
        private Integer days;

        @Schema(description = "行程晚数")
        private Integer nights;
    }

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
