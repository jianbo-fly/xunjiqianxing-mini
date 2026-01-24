package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 轮播图VO
 */
@Data
@Schema(description = "轮播图")
public class BannerVO {

    @Schema(description = "轮播图ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "跳转类型: 0无跳转 1线路详情 2外部链接 3小程序页面")
    private Integer linkType;

    @Schema(description = "跳转值")
    private String linkValue;
}
