package com.xunjiqianxing.admin.dto.content;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Banner查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Banner查询请求")
public class BannerQueryRequest extends PageQuery {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "状态: 0禁用 1启用")
    private Integer status;
}
