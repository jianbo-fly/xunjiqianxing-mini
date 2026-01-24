package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现请求
 */
@Data
@Schema(description = "提现请求")
public class WithdrawRequest {

    @Schema(description = "提现金额")
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "100", message = "最低提现金额100元")
    private BigDecimal amount;

    @Schema(description = "提现方式: 1微信 2支付宝 3银行卡")
    @NotNull(message = "提现方式不能为空")
    private Integer withdrawType;

    @Schema(description = "收款账号")
    @NotBlank(message = "收款账号不能为空")
    private String account;

    @Schema(description = "收款人姓名")
    @NotBlank(message = "收款人姓名不能为空")
    private String accountName;
}
