package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推广员详情响应
 */
@Data
@Schema(description = "推广员详情响应")
public class PromoterDetailVO {

    @Schema(description = "推广员ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "推广码")
    private String promoCode;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "等级: 1普通 2银牌 3金牌")
    private Integer level;

    @Schema(description = "等级描述")
    private String levelDesc;

    @Schema(description = "佣金比例")
    private BigDecimal commissionRate;

    @Schema(description = "累计佣金")
    private BigDecimal totalCommission;

    @Schema(description = "可提现佣金")
    private BigDecimal availableCommission;

    @Schema(description = "已提现金额")
    private BigDecimal withdrawnAmount;

    @Schema(description = "推广人数")
    private Integer promotedCount;

    @Schema(description = "成交订单数")
    private Integer orderCount;

    @Schema(description = "成交金额")
    private BigDecimal orderAmount;

    @Schema(description = "状态: 0待审核 1正常 2禁用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "审核时间")
    private LocalDateTime auditAt;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
