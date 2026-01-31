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
