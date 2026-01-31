package com.xunjiqianxing.admin.dto.user;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 领队申请查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "领队申请查询请求")
public class LeaderApplyQueryRequest extends PageQuery {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回")
    private Integer status;

    @Schema(description = "申请时间开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateBegin;

    @Schema(description = "申请时间结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateEnd;
}
