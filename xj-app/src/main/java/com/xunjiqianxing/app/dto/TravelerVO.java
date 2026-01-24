package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 出行人VO
 */
@Data
@Schema(description = "出行人信息")
public class TravelerVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "证件类型: 1身份证 2护照 3港澳通行证 4台湾通行证")
    private Integer idType;

    @Schema(description = "证件类型描述")
    private String idTypeDesc;

    @Schema(description = "证件号码(脱敏)")
    private String idNo;

    @Schema(description = "手机号(脱敏)")
    private String phone;

    @Schema(description = "性别: 1男 2女")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthday;

    @Schema(description = "是否默认")
    private Boolean isDefault;

    @Schema(description = "紧急联系人姓名")
    private String emergencyName;

    @Schema(description = "紧急联系人电话(脱敏)")
    private String emergencyPhone;

    public static String getIdTypeDesc(Integer idType) {
        if (idType == null) return "";
        return switch (idType) {
            case 1 -> "身份证";
            case 2 -> "护照";
            case 3 -> "港澳通行证";
            case 4 -> "台湾通行证";
            default -> "";
        };
    }

    public static String maskIdNo(String idNo) {
        if (idNo == null || idNo.length() < 6) return idNo;
        int len = idNo.length();
        return idNo.substring(0, 3) + "****" + idNo.substring(len - 4);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
