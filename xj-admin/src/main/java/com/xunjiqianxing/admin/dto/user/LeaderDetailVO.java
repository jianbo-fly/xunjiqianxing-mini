package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 领队详情VO
 */
@Data
@Schema(description = "领队详情VO")
public class LeaderDetailVO {

    @Schema(description = "领队ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "身份证正面")
    private String idCardFront;

    @Schema(description = "身份证背面")
    private String idCardBack;

    @Schema(description = "资质证书")
    private List<String> certificate;

    @Schema(description = "个人简介")
    private String intro;

    @Schema(description = "擅长领域")
    private List<String> expertise;

    @Schema(description = "从业年限")
    private Integer experience;

    @Schema(description = "带队次数")
    private Integer leadCount;

    @Schema(description = "累计佣金")
    private BigDecimal totalCommission;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回 3已禁用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
