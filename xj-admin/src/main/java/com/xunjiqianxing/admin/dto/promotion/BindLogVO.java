package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 绑定记录响应
 */
@Data
@Schema(description = "绑定记录响应")
public class BindLogVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "推广员ID")
    private Long promoterId;

    @Schema(description = "被绑定用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "绑定时间")
    private LocalDateTime createdAt;

    @Schema(description = "解绑时间")
    private LocalDateTime unbindTime;

    @Schema(description = "解绑原因")
    private String unbindReason;

    @Schema(description = "是否有效")
    private Boolean valid;
}
