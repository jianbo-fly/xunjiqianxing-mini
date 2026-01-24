package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员信息VO
 */
@Data
@Builder
@Schema(description = "会员信息")
public class MemberInfoVO {

    @Schema(description = "是否会员")
    private Boolean isMember;

    @Schema(description = "会员到期时间")
    private LocalDateTime expireAt;

    @Schema(description = "到期描述")
    private String expireDesc;

    @Schema(description = "会员年费")
    private BigDecimal yearPrice;
}
