package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户信息请求
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateUserRequest {

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "性别: 0未知 1男 2女")
    private Integer gender;
}
