package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * 出行人请求
 */
@Data
@Schema(description = "出行人请求")
public class TravelerRequest {

    @Schema(description = "姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "证件类型: 1身份证 2护照 3港澳通行证 4台湾通行证")
    @NotNull(message = "证件类型不能为空")
    private Integer idType;

    @Schema(description = "证件号码")
    @NotBlank(message = "证件号码不能为空")
    private String idNo;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "性别: 1男 2女")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthday;

    @Schema(description = "是否设为默认")
    private Boolean isDefault;

    @Schema(description = "紧急联系人姓名")
    private String emergencyName;

    @Schema(description = "紧急联系人电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系人电话格式不正确")
    private String emergencyPhone;
}
