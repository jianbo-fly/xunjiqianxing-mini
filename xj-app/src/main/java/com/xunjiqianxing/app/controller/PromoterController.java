package com.xunjiqianxing.app.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.*;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.entity.PromoterScanRecord;
import com.xunjiqianxing.service.promotion.entity.PromoterWithdraw;
import com.xunjiqianxing.service.promotion.service.PromoterService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推广员接口
 */
@Slf4j
@RestController
@RequestMapping("/api/promoter")
@RequiredArgsConstructor
@Tag(name = "推广员接口", description = "推广员相关接口")
public class PromoterController {

    private final PromoterService promoterService;
    private final UserService userService;
    private final WxMaService wxMaService;

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
     * 扫码上报（无需登录）
     */
    @PostMapping("/scan")
    @Operation(summary = "扫码上报", description = "扫描推广码时上报，无需登录")
    public Result<Void> scan(@RequestBody java.util.Map<String, String> body) {
        String promoCode = body.get("promoCode");
        if (promoCode != null && !promoCode.isBlank()) {
            promoterService.recordScan(promoCode);
        }
        return Result.success();
    }

    /**
     * 申请成为推广员
     */
    @PostMapping("/apply")
    @Operation(summary = "申请成为推广员", description = "提交推广员申请")
    public Result<PromoterVO> apply(@Valid @RequestBody PromoterApplyRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);
        String phone = user != null ? user.getPhone() : null;
        PromoterInfo promoter = promoterService.apply(userId, request.getNickname(), phone);
        return Result.success(toPromoterVO(promoter));
    }

    /**
     * 获取推广员专属小程序码（PNG图片）
     */
    @GetMapping("/qrcode")
    @Operation(summary = "获取推广码图片", description = "生成推广员专属小程序码，返回PNG图片字节流")
    public ResponseEntity<byte[]> qrcode() {
        Long userId = StpUtil.getLoginIdAsLong();
        PromoterInfo promoter = promoterService.getByUserId(userId);
        if (promoter == null) {
            return ResponseEntity.notFound().build();
        }
        if (promoter.getStatus() != 1) {
            return ResponseEntity.status(403).build();
        }
        try {
            // scene 即推广码，扫码后小程序 onLoad options.scene 可拿到
            // envVersion: develop=开发版 trial=体验版 release=正式版
            // checkPath=false：跳过页面路径校验（开发版/体验版均需）
            //todo： 修改为release
            File qrFile = wxMaService.getQrcodeService()
                    .createWxaCodeUnlimit(promoter.getPromoCode(), "pages/index/index",
                            false, "trial", 280, false, null, false);
            byte[] bytes = Files.readAllBytes(qrFile.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(bytes);
        } catch (Exception e) {
            log.error("生成推广码失败: userId={}", userId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 绑定推广员
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定推广员", description = "通过推广码绑定推广员")
    public Result<Void> bind(@RequestBody java.util.Map<String, String> body) {
        String promoCode = body.get("promoCode");
        if (promoCode == null || promoCode.isBlank()) {
            return Result.fail("推广码不能为空");
        }
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
     * 扫码记录列表
     */
    @GetMapping("/scan/list")
    @Operation(summary = "扫码记录", description = "获取推广码扫码记录列表")
    public Result<Map<String, Object>> scanList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<PromoterScanRecord> records = promoterService.listScanRecords(userId, page, pageSize);
        List<Map<String, Object>> list = records.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("createdAt", r.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return Result.success(result);
    }

    /**
     * 推广下单记录列表（基于佣金记录）
     */
    @GetMapping("/order/list")
    @Operation(summary = "推广下单记录", description = "获取通过推广带来的下单记录")
    public Result<Map<String, Object>> orderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<PromoterCommission> all = promoterService.listCommissions(userId, null);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<Map<String, Object>> list = new ArrayList<>();
        if (start < all.size()) {
            for (PromoterCommission c : all.subList(start, end)) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderNo", c.getOrderNo());
                item.put("orderAmount", c.getOrderAmount());
                item.put("commissionAmount", c.getCommissionAmount());
                item.put("status", c.getStatus());
                item.put("createdAt", c.getCreatedAt());
                list.add(item);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", all.size());
        return Result.success(result);
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
            return Result.success(null);
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
        vo.setScanCount(promoter.getScanCount());
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
