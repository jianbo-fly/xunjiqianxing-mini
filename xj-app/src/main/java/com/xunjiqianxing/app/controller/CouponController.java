package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.CouponVO;
import com.xunjiqianxing.app.dto.UserCouponVO;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.promotion.entity.Coupon;
import com.xunjiqianxing.service.promotion.entity.UserCoupon;
import com.xunjiqianxing.service.promotion.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券接口
 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "优惠券接口", description = "优惠券相关接口")
public class CouponController {

    private final CouponService couponService;

    /**
     * 获取可领取的优惠券列表
     */
    @GetMapping("/available")
    @Operation(summary = "可领取优惠券", description = "获取可领取的优惠券列表")
    public Result<List<CouponVO>> available() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Coupon> coupons = couponService.listAvailable(userId);
        List<CouponVO> voList = coupons.stream()
                .map(this::toCouponVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/receive/{id}")
    @Operation(summary = "领取优惠券", description = "领取指定的优惠券")
    public Result<UserCouponVO> receive(
            @Parameter(description = "优惠券ID") @PathVariable Long id) {

        Long userId = StpUtil.getLoginIdAsLong();
        UserCoupon userCoupon = couponService.receive(userId, id);
        return Result.success(toUserCouponVO(userCoupon, null));
    }

    /**
     * 获取我的优惠券列表
     */
    @GetMapping("/my")
    @Operation(summary = "我的优惠券", description = "获取用户的优惠券列表")
    public Result<List<UserCouponVO>> my(
            @Parameter(description = "状态: 0未使用 1已使用 2已过期，不传查全部")
            @RequestParam(required = false) Integer status) {

        Long userId = StpUtil.getLoginIdAsLong();
        List<UserCoupon> coupons = couponService.listUserCoupons(userId, status);
        List<UserCouponVO> voList = coupons.stream()
                .map(c -> toUserCouponVO(c, null))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取订单可用优惠券
     */
    @GetMapping("/usable")
    @Operation(summary = "订单可用优惠券", description = "获取指定订单金额可用的优惠券")
    public Result<List<UserCouponVO>> usable(
            @Parameter(description = "线路ID") @RequestParam(required = false) Long routeId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal amount) {

        Long userId = StpUtil.getLoginIdAsLong();
        List<UserCoupon> coupons = couponService.listUsableForOrder(userId, routeId, amount);
        List<UserCouponVO> voList = coupons.stream()
                .map(c -> toUserCouponVO(c, amount))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 转换为CouponVO
     */
    private CouponVO toCouponVO(Coupon coupon) {
        CouponVO vo = new CouponVO();
        vo.setId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setType(coupon.getType());
        vo.setTypeDesc(CouponVO.getTypeDesc(coupon.getType()));
        vo.setThreshold(coupon.getThreshold());
        vo.setAmount(coupon.getAmount());
        vo.setDiscount(coupon.getDiscount());
        vo.setMaxAmount(coupon.getMaxAmount());
        vo.setValidStart(coupon.getValidStart());
        vo.setValidEnd(coupon.getValidEnd());
        vo.setRemainCount(coupon.getTotalCount() - coupon.getReceivedCount());
        vo.setMemberOnly(coupon.getMemberOnly() != null && coupon.getMemberOnly() == 1);
        vo.setDescription(coupon.getDescription());
        vo.setDiscountDesc(CouponVO.getDiscountDesc(coupon.getType(), coupon.getAmount(), coupon.getDiscount()));
        vo.setThresholdDesc(CouponVO.getThresholdDesc(coupon.getType(), coupon.getThreshold()));
        return vo;
    }

    /**
     * 转换为UserCouponVO
     */
    private UserCouponVO toUserCouponVO(UserCoupon coupon, BigDecimal orderAmount) {
        UserCouponVO vo = new UserCouponVO();
        vo.setId(coupon.getId());
        vo.setName(coupon.getCouponName());
        vo.setType(coupon.getCouponType());
        vo.setTypeDesc(CouponVO.getTypeDesc(coupon.getCouponType()));
        vo.setThreshold(coupon.getThreshold());
        vo.setAmount(coupon.getAmount());
        vo.setDiscount(coupon.getDiscount());
        vo.setMaxAmount(coupon.getMaxAmount());
        vo.setValidStart(coupon.getValidStart());
        vo.setValidEnd(coupon.getValidEnd());
        vo.setStatus(coupon.getStatus());
        vo.setStatusDesc(UserCouponVO.getStatusDesc(coupon.getStatus()));
        vo.setDiscountDesc(CouponVO.getDiscountDesc(coupon.getCouponType(), coupon.getAmount(), coupon.getDiscount()));
        vo.setThresholdDesc(CouponVO.getThresholdDesc(coupon.getCouponType(), coupon.getThreshold()));
        if (orderAmount != null) {
            vo.setEstimatedDiscount(couponService.calculateDiscount(coupon, orderAmount));
        }
        return vo;
    }
}
