package com.xunjiqianxing.service.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.member.entity.MemberOrder;
import com.xunjiqianxing.service.member.mapper.MemberOrderMapper;
import com.xunjiqianxing.service.member.service.MemberService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 会员服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberOrderMapper memberOrderMapper;
    private final UserService userService;

    // TODO: 从配置表读取
    private static final BigDecimal MEMBER_YEAR_PRICE = new BigDecimal("99");

    @Override
    public BigDecimal getMemberPrice() {
        return MEMBER_YEAR_PRICE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberOrder createOrder(Long userId, Integer months) {
        if (months == null || months < 1) {
            months = 12;
        }

        // 计算价格
        BigDecimal price = MEMBER_YEAR_PRICE.multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);

        // 计算会员有效期
        UserInfo user = userService.getById(userId);
        LocalDate startDate;
        if (user.getIsMember() == 1 && user.getMemberExpireAt() != null
                && user.getMemberExpireAt().isAfter(LocalDateTime.now())) {
            // 已是会员，从到期时间续期
            startDate = user.getMemberExpireAt().toLocalDate();
        } else {
            // 新会员，从今天开始
            startDate = LocalDate.now();
        }
        LocalDate endDate = startDate.plusMonths(months);

        // 创建订单
        MemberOrder order = new MemberOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAmount(price);
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        order.setDurationMonths(months);
        order.setStatus(0);

        memberOrderMapper.insert(order);
        log.info("会员订单创建成功: orderNo={}, userId={}", order.getOrderNo(), userId);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean paySuccess(String orderNo) {
        MemberOrder order = memberOrderMapper.selectOne(
                new LambdaUpdateWrapper<MemberOrder>()
                        .eq(MemberOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != 0) {
            return false;
        }

        // 更新订单状态
        int rows = memberOrderMapper.update(null,
                new LambdaUpdateWrapper<MemberOrder>()
                        .eq(MemberOrder::getOrderNo, orderNo)
                        .eq(MemberOrder::getStatus, 0)
                        .set(MemberOrder::getStatus, 1)
                        .set(MemberOrder::getPayTime, LocalDateTime.now())
        );

        if (rows > 0) {
            // 更新用户会员状态
            userService.updateMemberStatus(
                    order.getUserId(),
                    true,
                    order.getEndDate().atStartOfDay()
            );
            log.info("会员订单支付成功: orderNo={}", orderNo);
        }
        return rows > 0;
    }

    @Override
    public boolean isValidMember(Long userId) {
        UserInfo user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return user.getIsMember() == 1
                && user.getMemberExpireAt() != null
                && user.getMemberExpireAt().isAfter(LocalDateTime.now());
    }

    @Override
    public String getMemberExpireDesc(Long userId) {
        UserInfo user = userService.getById(userId);
        if (user == null || user.getIsMember() != 1 || user.getMemberExpireAt() == null) {
            return "未开通会员";
        }
        if (user.getMemberExpireAt().isBefore(LocalDateTime.now())) {
            return "会员已过期";
        }
        return "有效期至 " + user.getMemberExpireAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = IdGenerator.nextId() % 100000;
        return "MB" + date + String.format("%05d", seq);
    }
}
