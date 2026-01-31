package com.xunjiqianxing.admin.dto.promotion;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户优惠券查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户优惠券查询请求")
public class UserCouponQueryRequest extends PageQuery {

    @Schema(description = "优惠券模板ID")
    private Long couponId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "状态: 0未使用 1已使用 2已过期")
    private Integer status;
}
