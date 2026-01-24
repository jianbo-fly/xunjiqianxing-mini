package com.xunjiqianxing.service.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.service.promotion.entity.Coupon;
import com.xunjiqianxing.service.promotion.entity.UserCoupon;
import com.xunjiqianxing.service.promotion.mapper.CouponMapper;
import com.xunjiqianxing.service.promotion.mapper.UserCouponMapper;
import com.xunjiqianxing.service.promotion.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<Coupon> listAvailable(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .eq(Coupon::getIsDeleted, 0)
                        .and(w -> w
                                .eq(Coupon::getValidType, 2)
                                .or(o -> o
                                        .eq(Coupon::getValidType, 1)
                                        .le(Coupon::getValidStart, now)
                                        .ge(Coupon::getValidEnd, now)
                                )
                        )
                        .apply("received_count < total_count")
                        .orderByDesc(Coupon::getCreatedAt)
        );

        // 过滤用户已领满的
        return coupons.stream().filter(coupon -> {
            Long count = userCouponMapper.selectCount(
                    new LambdaQueryWrapper<UserCoupon>()
                            .eq(UserCoupon::getUserId, userId)
                            .eq(UserCoupon::getCouponId, coupon.getId())
            );
            return count < coupon.getPerLimit();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon receive(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BizException("优惠券不存在或已停用");
        }

        // 检查库存
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BizException("优惠券已被领完");
        }

        // 检查领取次数
        Long count = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
        );
        if (count >= coupon.getPerLimit()) {
            throw new BizException("已达到领取上限");
        }

        // 计算有效期
        LocalDateTime validStart, validEnd;
        if (coupon.getValidType() == 1) {
            validStart = coupon.getValidStart();
            validEnd = coupon.getValidEnd();
        } else {
            validStart = LocalDateTime.now();
            validEnd = validStart.plusDays(coupon.getValidDays());
        }

        // 创建用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponName(coupon.getName());
        userCoupon.setCouponType(coupon.getType());
        userCoupon.setThreshold(coupon.getThreshold());
        userCoupon.setAmount(coupon.getAmount());
        userCoupon.setDiscount(coupon.getDiscount());
        userCoupon.setMaxAmount(coupon.getMaxAmount());
        userCoupon.setValidStart(validStart);
        userCoupon.setValidEnd(validEnd);
        userCoupon.setStatus(0);

        userCouponMapper.insert(userCoupon);

        // 更新已领取数量
        couponMapper.update(null,
                new LambdaUpdateWrapper<Coupon>()
                        .eq(Coupon::getId, couponId)
                        .setSql("received_count = received_count + 1")
        );

        log.info("用户领取优惠券: userId={}, couponId={}", userId, couponId);
        return userCoupon;
    }

    @Override
    public List<UserCoupon> listUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getIsDeleted, 0)
                .orderByDesc(UserCoupon::getCreatedAt);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        return userCouponMapper.selectList(wrapper);
    }

    @Override
    public List<UserCoupon> listUsableForOrder(Long userId, Long routeId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        return userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0)
                        .le(UserCoupon::getValidStart, now)
                        .ge(UserCoupon::getValidEnd, now)
                        .and(w -> w
                                .isNull(UserCoupon::getThreshold)
                                .or()
                                .le(UserCoupon::getThreshold, orderAmount)
                        )
                        .orderByDesc(UserCoupon::getAmount)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean use(Long userCouponId, String orderNo) {
        int rows = userCouponMapper.update(null,
                new LambdaUpdateWrapper<UserCoupon>()
                        .eq(UserCoupon::getId, userCouponId)
                        .eq(UserCoupon::getStatus, 0)
                        .set(UserCoupon::getStatus, 1)
                        .set(UserCoupon::getUsedAt, LocalDateTime.now())
                        .set(UserCoupon::getUsedOrderNo, orderNo)
        );
        if (rows > 0) {
            log.info("优惠券使用成功: userCouponId={}, orderNo={}", userCouponId, orderNo);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refund(String orderNo) {
        int rows = userCouponMapper.update(null,
                new LambdaUpdateWrapper<UserCoupon>()
                        .eq(UserCoupon::getUsedOrderNo, orderNo)
                        .eq(UserCoupon::getStatus, 1)
                        .set(UserCoupon::getStatus, 0)
                        .set(UserCoupon::getUsedAt, null)
                        .set(UserCoupon::getUsedOrderNo, null)
        );
        if (rows > 0) {
            log.info("优惠券退还成功: orderNo={}", orderNo);
        }
        return rows > 0;
    }

    @Override
    public BigDecimal calculateDiscount(UserCoupon coupon, BigDecimal orderAmount) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }

        // 检查门槛
        if (coupon.getThreshold() != null && orderAmount.compareTo(coupon.getThreshold()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        switch (coupon.getCouponType()) {
            case 1: // 满减券
            case 3: // 无门槛券
                discount = coupon.getAmount();
                break;
            case 2: // 折扣券
                discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscount()));
                if (coupon.getMaxAmount() != null && discount.compareTo(coupon.getMaxAmount()) > 0) {
                    discount = coupon.getMaxAmount();
                }
                break;
            default:
                discount = BigDecimal.ZERO;
        }

        // 优惠不能超过订单金额
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expireProcess() {
        LocalDateTime now = LocalDateTime.now();
        int rows = userCouponMapper.update(null,
                new LambdaUpdateWrapper<UserCoupon>()
                        .eq(UserCoupon::getStatus, 0)
                        .lt(UserCoupon::getValidEnd, now)
                        .set(UserCoupon::getStatus, 2)
        );
        if (rows > 0) {
            log.info("优惠券过期处理完成，处理数量: {}", rows);
        }
    }
}
