package com.xunjiqianxing.admin.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商响应
 */
@Data
@Schema(description = "供应商响应")
public class SupplierVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "商家名称")
    private String name;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "资质证书图片")
    private List<String> licenseImages;

    // 公司信息
    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司地址")
    private String companyAddress;

    // 联系人1
    @Schema(description = "联系人1姓名")
    private String contact1Name;

    @Schema(description = "联系人1职务")
    private String contact1Title;

    @Schema(description = "联系人1电话")
    private String contact1Phone;

    // 联系人2（选填）
    @Schema(description = "联系人2姓名")
    private String contact2Name;

    @Schema(description = "联系人2职务")
    private String contact2Title;

    @Schema(description = "联系人2电话")
    private String contact2Phone;

    // 资质证件
    @Schema(description = "营业执照")
    private String businessLicense;

    @Schema(description = "旅游业务许可证")
    private String tourismLicense;

    @Schema(description = "法人身份证正面")
    private String idCardFront;

    @Schema(description = "法人身份证反面")
    private String idCardBack;

    @Schema(description = "保险证明")
    private String insuranceImage;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "状态: 0禁用 1正常")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "线路数量")
    private Integer routeCount;

    @Schema(description = "订单数量")
    private Integer orderCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
