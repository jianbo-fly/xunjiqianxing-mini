package com.xunjiqianxing.admin.dto.system;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "供应商查询请求")
public class SupplierQueryRequest extends PageQuery {

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "状态: 0禁用 1正常")
    private Integer status;
}
