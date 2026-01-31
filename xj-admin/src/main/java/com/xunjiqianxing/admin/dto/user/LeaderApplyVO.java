package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 领队申请响应
 */
@Data
@Schema(description = "领队申请响应")
public class LeaderApplyVO {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "擅长领域")
    private List<String> expertise;

    @Schema(description = "个人简介")
    private String intro;

    @Schema(description = "资质证书图片")
    private List<String> certificateImages;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "支付状态: 0未支付 1已支付")
    private Integer payStatus;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "申请时间")
    private LocalDateTime createdAt;
}
