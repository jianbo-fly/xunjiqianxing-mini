package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建订单请求
 */
@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @NotNull(message = "套餐ID不能为空")
    @Schema(description = "套餐ID", required = true)
    private Long skuId;

    @NotNull(message = "出行日期不能为空")
    @Schema(description = "出行日期", required = true)
    private LocalDate startDate;

    @NotNull(message = "成人数不能为空")
    @Schema(description = "成人数", required = true)
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount = 0;

    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名", required = true)
    private String contactName;

    @NotBlank(message = "联系人电话不能为空")
    @Schema(description = "联系人电话", required = true)
    private String contactPhone;

    @Valid
    @NotNull(message = "出行人信息不能为空")
    @Size(min = 1, message = "至少需要一个出行人")
    @Schema(description = "出行人列表", required = true)
    private List<TravelerDTO> travelers;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "备注")
    private String remark;

    /**
     * 出行人DTO
     */
    @Data
    @Schema(description = "出行人信息")
    public static class TravelerDTO {

        @NotBlank(message = "姓名不能为空")
        @Schema(description = "姓名", required = true)
        private String name;

        @NotBlank(message = "身份证号不能为空")
        @Schema(description = "身份证号", required = true)
        private String idCard;

        @NotBlank(message = "手机号不能为空")
        @Schema(description = "手机号", required = true)
        private String phone;

        @NotNull(message = "类型不能为空")
        @Schema(description = "类型: 1成人 2儿童", required = true)
        private Integer travelerType;
    }
}
