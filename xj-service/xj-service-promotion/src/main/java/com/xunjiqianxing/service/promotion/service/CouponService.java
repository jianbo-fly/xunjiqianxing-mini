package com.xunjiqianxing.service.promotion.service;

import com.xunjiqianxing.service.promotion.entity.Coupon;
import com.xunjiqianxing.service.promotion.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务
 */
public interface CouponService {

    /**
     * 获取可领取的优惠券列表
     */
    List<Coupon> listAvailable(Long userId);

    /**
     * 领取优惠券
     */
    UserCoupon receive(Long userId, Long couponId);

    /**
     * 获取用户的优惠券列表
     */
    List<UserCoupon> listUserCoupons(Long userId, Integer status);

    /**
     * 获取可用于指定订单的优惠券
     */
    List<UserCoupon> listUsableForOrder(Long userId, Long routeId, BigDecimal orderAmount);

    /**
     * 使用优惠券
     */
    boolean use(Long userCouponId, String orderNo);

    /**
     * 退还优惠券
     */
    boolean refund(String orderNo);

    /**
     * 计算优惠金额
     */
    BigDecimal calculateDiscount(UserCoupon coupon, BigDecimal orderAmount);

    /**
     * 过期处理
     */
    void expireProcess();
}
