package com.xunjiqianxing.admin.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 添加跟进记录请求
 */
@Data
@Schema(description = "添加跟进记录请求")
public class CustomFollowRequest {

    @NotBlank(message = "跟进内容不能为空")
    @Schema(description = "跟进内容")
    private String content;
}
