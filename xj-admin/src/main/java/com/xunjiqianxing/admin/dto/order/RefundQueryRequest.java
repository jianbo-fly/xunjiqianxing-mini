package com.xunjiqianxing.admin.dto.order;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 退款查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "退款查询请求")
public class RefundQueryRequest extends PageQuery {

    @Schema(description = "退款编号")
    private String refundNo;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回")
    private Integer status;

    @Schema(description = "申请时间开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateBegin;

    @Schema(description = "申请时间结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateEnd;
}
