package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 推广员申请请求
 */
@Data
@Schema(description = "推广员申请请求")
public class PromoterApplyRequest {

    @Schema(description = "推广昵称（展示在海报上）")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatarUrl;
}
