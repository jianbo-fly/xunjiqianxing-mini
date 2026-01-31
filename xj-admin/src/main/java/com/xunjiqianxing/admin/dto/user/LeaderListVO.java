package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 领队列表响应
 */
@Data
@Schema(description = "领队列表响应")
public class LeaderListVO {

    @Schema(description = "领队ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "擅长领域")
    private List<String> expertise;

    @Schema(description = "个人简介")
    private String intro;

    @Schema(description = "带队次数")
    private Integer tripCount;

    @Schema(description = "累计佣金")
    private BigDecimal totalCommission;

    @Schema(description = "状态: 0禁用 1正常")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
