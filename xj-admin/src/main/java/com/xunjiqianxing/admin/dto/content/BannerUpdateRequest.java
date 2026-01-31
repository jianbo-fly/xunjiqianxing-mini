package com.xunjiqianxing.admin.dto.content;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新Banner请求
 */
@Data
@Schema(description = "更新Banner请求")
public class BannerUpdateRequest {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "跳转类型: 0无跳转 1线路详情 2外部链接 3小程序页面")
    private Integer linkType;

    @Schema(description = "跳转值")
    private String linkValue;

    @Schema(description = "排序(越大越前)")
    private Integer sortOrder;

    @Schema(description = "开始展示时间")
    private LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    private LocalDateTime endTime;
}
