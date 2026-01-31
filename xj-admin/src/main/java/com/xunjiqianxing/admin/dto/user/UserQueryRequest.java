package com.xunjiqianxing.admin.dto.user;

import com.xunjiqianxing.common.base.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 用户查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询请求")
public class UserQueryRequest extends PageQuery {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "是否会员: 0否 1是")
    private Integer isMember;

    @Schema(description = "是否领队: 0否 1是")
    private Integer isLeader;

    @Schema(description = "是否推广员: 0否 1是")
    private Integer isPromoter;

    @Schema(description = "状态: 0禁用 1正常")
    private Integer status;

    @Schema(description = "注册时间开始")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateBegin;

    @Schema(description = "注册时间结束")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDateEnd;
}
