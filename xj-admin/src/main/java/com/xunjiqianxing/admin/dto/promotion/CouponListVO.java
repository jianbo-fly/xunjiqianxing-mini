package com.xunjiqianxing.admin.dto.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板列表响应
 */
@Data
@Schema(description = "优惠券模板列表响应")
public class CouponListVO {

    @Schema(description = "优惠券ID")
    private Long id;

    @Schema(description = "券名称")
    private String name;

    @Schema(description = "券类型: 1满减券 2折扣券 3无门槛券")
    private Integer type;

    @Schema(description = "券类型描述")
    private String typeDesc;

    @Schema(description = "满减门槛金额")
    private BigDecimal threshold;

    @Schema(description = "优惠金额")
    private BigDecimal amount;

    @Schema(description = "折扣率")
    private BigDecimal discount;

    @Schema(description = "最高减免金额")
    private BigDecimal maxAmount;

    @Schema(description = "适用范围: 0全场 1指定线路 2指定分类")
    private Integer scope;

    @Schema(description = "适用范围描述")
    private String scopeDesc;

    @Schema(description = "有效期类型: 1固定时间 2领取后N天")
    private Integer validType;

    @Schema(description = "有效期开始")
    private LocalDateTime validStart;

    @Schema(description = "有效期结束")
    private LocalDateTime validEnd;

    @Schema(description = "领取后有效天数")
    private Integer validDays;

    @Schema(description = "发行总量")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    private Integer receivedCount;

    @Schema(description = "剩余数量")
    private Integer remainCount;

    @Schema(description = "每人限领")
    private Integer perLimit;

    @Schema(description = "状态: 0停用 1启用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "是否仅会员可用")
    private Integer memberOnly;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
