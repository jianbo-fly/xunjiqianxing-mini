package com.xunjiqianxing.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录VO
 */
@Data
@Schema(description = "提现记录")
public class WithdrawVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "提现方式: 1微信 2支付宝 3银行卡")
    private Integer withdrawType;

    @Schema(description = "提现方式描述")
    private String withdrawTypeDesc;

    @Schema(description = "收款账号(脱敏)")
    private String account;

    @Schema(description = "收款人姓名")
    private String accountName;

    @Schema(description = "状态: 0待审核 1审核通过 2已打款 3已拒绝")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    public static String getWithdrawTypeDesc(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "微信";
            case 2 -> "支付宝";
            case 3 -> "银行卡";
            default -> "";
        };
    }

    public static String getStatusDesc(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "已打款";
            case 3 -> "已拒绝";
            default -> "";
        };
    }

    public static String maskAccount(String account) {
        if (account == null || account.length() < 6) return account;
        return account.substring(0, 3) + "****" + account.substring(account.length() - 4);
    }
}
