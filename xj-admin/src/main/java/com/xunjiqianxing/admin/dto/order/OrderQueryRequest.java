package com.xunjiqianxing.admin.dto.order;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 订单查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单查询请求")
public class OrderQueryRequest extends PageQuery {

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "订单状态: 0待支付 1待确认 2已确认 3出行中 4已完成 5已取消 6退款中 7已退款 8已关闭")
    private Integer status;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "出发日期开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateBegin;

    @Schema(description = "出发日期结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateEnd;

    @Schema(description = "下单时间开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateBegin;

    @Schema(description = "下单时间结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateEnd;
}
