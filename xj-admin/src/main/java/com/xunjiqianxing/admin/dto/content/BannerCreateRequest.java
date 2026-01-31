package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建Banner请求
 */
@Data
@Schema(description = "创建Banner请求")
public class BannerCreateRequest {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图片URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    @Schema(description = "跳转类型: 0无跳转 1线路详情 2外部链接 3小程序页面")
    private Integer linkType = 0;

    @Schema(description = "跳转值")
    private String linkValue;

    @Schema(description = "排序(越大越前)")
    private Integer sortOrder = 0;

    @Schema(description = "开始展示时间")
    private LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    private LocalDateTime endTime;
}
