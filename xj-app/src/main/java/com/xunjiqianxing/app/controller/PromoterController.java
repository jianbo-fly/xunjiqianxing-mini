package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.*;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.entity.PromoterWithdraw;
import com.xunjiqianxing.service.promotion.service.PromoterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推广员接口
 */
@RestController
@RequestMapping("/api/promoter")
@RequiredArgsConstructor
@Tag(name = "推广员接口", description = "推广员相关接口")
public class PromoterController {

    private final PromoterService promoterService;

    /**
     * 获取推广员信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取推广员信息", description = "获取当前用户的推广员信息")
    public Result<PromoterVO> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        PromoterInfo promoter = promoterService.getByUserId(userId);
        if (promoter == null) {
            return Result.success(null);
        }
        return Result.success(toPromoterVO(promoter));
    }

    /**
     * 申请成为推广员
     */
    @PostMapping("/apply")
    @Operation(summary = "申请成为推广员", description = "提交推广员申请")
    public Result<PromoterVO> apply(@Valid @RequestBody PromoterApplyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        PromoterInfo promoter = promoterService.apply(userId, request.getRealName(), request.getPhone());
        return Result.success(toPromoterVO(promoter));
    }

    /**
     * 绑定推广员
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定推广员", description = "通过推广码绑定推广员")
    public Result<Void> bind(
            @Parameter(description = "推广码") @RequestParam String promoCode) {

        Long userId = StpUtil.getLoginIdAsLong();
        promoterService.bind(userId, promoCode);
        return Result.success();
    }

    /**
     * 获取佣金记录
     */
    @GetMapping("/commissions")
    @Operation(summary = "佣金记录", description = "获取佣金收入记录")
    public Result<List<CommissionVO>> commissions(
            @Parameter(description = "状态: 0待结算 1已结算 2已取消")
            @RequestParam(required = false) Integer status) {

        Long userId = StpUtil.getLoginIdAsLong();
        List<PromoterCommission> list = promoterService.listCommissions(userId, status);
        List<CommissionVO> voList = list.stream()
                .map(this::toCommissionVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdraw")
    @Operation(summary = "申请提现", description = "申请佣金提现")
    public Result<WithdrawVO> withdraw(@Valid @RequestBody WithdrawRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        PromoterWithdraw withdraw = promoterService.applyWithdraw(
                userId,
                request.getAmount(),
                request.getWithdrawType(),
                request.getAccount(),
                request.getAccountName()
        );
        return Result.success(toWithdrawVO(withdraw));
    }

    /**
     * 获取提现记录
     */
    @GetMapping("/withdraws")
    @Operation(summary = "提现记录", description = "获取提现申请记录")
    public Result<List<WithdrawVO>> withdraws() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<PromoterWithdraw> list = promoterService.listWithdraws(userId);
        List<WithdrawVO> voList = list.stream()
                .map(this::toWithdrawVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取推广统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "推广统计", description = "获取推广数据统计")
    public Result<PromoterVO> statistics() {
        Long userId = StpUtil.getLoginIdAsLong();
        PromoterInfo promoter = promoterService.getStatistics(userId);
        if (promoter == null) {
            return Result.fail("您还不是推广员");
        }
        return Result.success(toPromoterVO(promoter));
    }

    private PromoterVO toPromoterVO(PromoterInfo promoter) {
        PromoterVO vo = new PromoterVO();
        vo.setPromoCode(promoter.getPromoCode());
        vo.setLevel(promoter.getLevel());
        vo.setLevelName(PromoterVO.getLevelName(promoter.getLevel()));
        vo.setCommissionRate(promoter.getCommissionRate());
        vo.setCommissionRateDesc(promoter.getCommissionRate()
                .multiply(BigDecimal.valueOf(100))
                .stripTrailingZeros()
                .toPlainString() + "%");
        vo.setTotalCommission(promoter.getTotalCommission());
        vo.setAvailableCommission(promoter.getAvailableCommission());
        vo.setWithdrawnAmount(promoter.getWithdrawnAmount());
        vo.setPromotedCount(promoter.getPromotedCount());
        vo.setOrderCount(promoter.getOrderCount());
        vo.setOrderAmount(promoter.getOrderAmount());
        vo.setStatus(promoter.getStatus());
        vo.setStatusDesc(PromoterVO.getStatusDesc(promoter.getStatus()));
        return vo;
    }

    private CommissionVO toCommissionVO(PromoterCommission commission) {
        CommissionVO vo = new CommissionVO();
        vo.setId(commission.getId());
        vo.setOrderNo(commission.getOrderNo());
        vo.setOrderAmount(commission.getOrderAmount());
        vo.setCommissionRate(commission.getCommissionRate());
        vo.setCommissionAmount(commission.getCommissionAmount());
        vo.setStatus(commission.getStatus());
        vo.setStatusDesc(CommissionVO.getStatusDesc(commission.getStatus()));
        vo.setCreateTime(commission.getCreatedAt());
        vo.setSettleAt(commission.getSettleAt());
        return vo;
    }

    private WithdrawVO toWithdrawVO(PromoterWithdraw withdraw) {
        WithdrawVO vo = new WithdrawVO();
        vo.setId(withdraw.getId());
        vo.setAmount(withdraw.getAmount());
        vo.setWithdrawType(withdraw.getWithdrawType());
        vo.setWithdrawTypeDesc(WithdrawVO.getWithdrawTypeDesc(withdraw.getWithdrawType()));
        vo.setAccount(WithdrawVO.maskAccount(withdraw.getAccount()));
        vo.setAccountName(withdraw.getAccountName());
        vo.setStatus(withdraw.getStatus());
        vo.setStatusDesc(WithdrawVO.getStatusDesc(withdraw.getStatus()));
        vo.setCreateTime(withdraw.getCreatedAt());
        vo.setRejectReason(withdraw.getRejectReason());
        return vo;
    }
}
