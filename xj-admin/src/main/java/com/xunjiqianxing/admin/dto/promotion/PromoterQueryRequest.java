package com.xunjiqianxing.admin.dto.promotion;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 推广员查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "推广员查询请求")
public class PromoterQueryRequest extends PageQuery {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "推广码")
    private String promoCode;

    @Schema(description = "等级: 1普通 2银牌 3金牌")
    private Integer level;

    @Schema(description = "状态: 0待审核 1正常 2禁用")
    private Integer status;

    @Schema(description = "申请时间开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateBegin;

    @Schema(description = "申请时间结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateEnd;
}
