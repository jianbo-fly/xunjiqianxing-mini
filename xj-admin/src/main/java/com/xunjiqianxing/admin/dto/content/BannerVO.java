package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Banner响应
 */
@Data
@Schema(description = "Banner响应")
public class BannerVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "跳转类型: 0无跳转 1线路详情 2外部链接 3小程序页面")
    private Integer linkType;

    @Schema(description = "跳转类型描述")
    private String linkTypeDesc;

    @Schema(description = "跳转值")
    private String linkValue;

    @Schema(description = "状态: 0禁用 1启用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "开始展示时间")
    private LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
