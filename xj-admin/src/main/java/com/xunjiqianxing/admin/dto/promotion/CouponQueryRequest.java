package com.xunjiqianxing.admin.dto.promotion;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 优惠券模板查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "优惠券模板查询请求")
public class CouponQueryRequest extends PageQuery {

    @Schema(description = "券名称")
    private String name;

    @Schema(description = "券类型: 1满减券 2折扣券 3无门槛券")
    private Integer type;

    @Schema(description = "状态: 0停用 1启用")
    private Integer status;
}
