package com.xunjiqianxing.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员列表响应
 */
@Data
@Schema(description = "会员列表响应")
public class MemberListVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "会员到期时间")
    private LocalDateTime memberExpireAt;

    @Schema(description = "会员状态: 1有效 2已过期")
    private Integer memberStatus;

    @Schema(description = "会员状态描述")
    private String memberStatusDesc;

    @Schema(description = "累计支付金额")
    private BigDecimal totalPaidAmount;

    @Schema(description = "首次开通时间")
    private LocalDateTime firstOpenAt;

    @Schema(description = "剩余天数")
    private Integer remainDays;
}
