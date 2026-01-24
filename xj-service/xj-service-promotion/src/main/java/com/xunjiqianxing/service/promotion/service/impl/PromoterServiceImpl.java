package com.xunjiqianxing.service.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.entity.PromoterWithdraw;
import com.xunjiqianxing.service.promotion.mapper.PromoterCommissionMapper;
import com.xunjiqianxing.service.promotion.mapper.PromoterInfoMapper;
import com.xunjiqianxing.service.promotion.mapper.PromoterWithdrawMapper;
import com.xunjiqianxing.service.promotion.service.PromoterService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 推广员服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromoterServiceImpl implements PromoterService {

    private final PromoterInfoMapper promoterInfoMapper;
    private final PromoterCommissionMapper commissionMapper;
    private final PromoterWithdrawMapper withdrawMapper;
    private final UserService userService;

    // 默认佣金比例
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.05");
    // 最低提现金额
    private static final BigDecimal MIN_WITHDRAW_AMOUNT = new BigDecimal("100");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromoterInfo apply(Long userId, String realName, String phone) {
        // 检查是否已经申请过
        PromoterInfo existing = getByUserId(userId);
        if (existing != null) {
            if (existing.getStatus() == 0) {
                throw new BizException("您已提交申请，请等待审核");
            } else if (existing.getStatus() == 1) {
                throw new BizException("您已是推广员");
            }
        }

        // 生成推广码
        String promoCode = generatePromoCode();

        PromoterInfo promoter = new PromoterInfo();
        promoter.setUserId(userId);
        promoter.setPromoCode(promoCode);
        promoter.setRealName(realName);
        promoter.setPhone(phone);
        promoter.setLevel(1);
        promoter.setCommissionRate(DEFAULT_COMMISSION_RATE);
        promoter.setTotalCommission(BigDecimal.ZERO);
        promoter.setAvailableCommission(BigDecimal.ZERO);
        promoter.setWithdrawnAmount(BigDecimal.ZERO);
        promoter.setPromotedCount(0);
        promoter.setOrderCount(0);
        promoter.setOrderAmount(BigDecimal.ZERO);
        promoter.setStatus(0); // 待审核

        promoterInfoMapper.insert(promoter);
        log.info("推广员申请提交: userId={}", userId);
        return promoter;
    }

    @Override
    public PromoterInfo getByUserId(Long userId) {
        return promoterInfoMapper.selectOne(
                new LambdaQueryWrapper<PromoterInfo>()
                        .eq(PromoterInfo::getUserId, userId)
                        .eq(PromoterInfo::getIsDeleted, 0)
        );
    }

    @Override
    public PromoterInfo getByPromoCode(String promoCode) {
        return promoterInfoMapper.selectOne(
                new LambdaQueryWrapper<PromoterInfo>()
                        .eq(PromoterInfo::getPromoCode, promoCode)
                        .eq(PromoterInfo::getStatus, 1)
                        .eq(PromoterInfo::getIsDeleted, 0)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bind(Long userId, String promoCode) {
        UserInfo user = userService.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 检查是否已绑定
        if (user.getBindpromoterId() != null) {
            throw new BizException("您已绑定推广员");
        }

        PromoterInfo promoter = getByPromoCode(promoCode);
        if (promoter == null) {
            throw new BizException("推广码无效");
        }

        // 不能绑定自己
        if (promoter.getUserId().equals(userId)) {
            throw new BizException("不能绑定自己");
        }

        // 绑定
        boolean success = userService.bindPromoter(userId, promoter.getUserId());
        if (success) {
            // 更新推广人数
            promoterInfoMapper.update(null,
                    new LambdaUpdateWrapper<PromoterInfo>()
                            .eq(PromoterInfo::getId, promoter.getId())
                            .setSql("promoted_count = promoted_count + 1")
            );
            log.info("用户绑定推广员: userId={}, promoterId={}", userId, promoter.getUserId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordCommission(Long fromUserId, String orderNo, BigDecimal orderAmount) {
        UserInfo user = userService.getById(fromUserId);
        if (user == null || user.getBindpromoterId() == null) {
            return false;
        }

        PromoterInfo promoter = getByUserId(user.getBindpromoterId());
        if (promoter == null || promoter.getStatus() != 1) {
            return false;
        }

        // 计算佣金
        BigDecimal commissionAmount = orderAmount.multiply(promoter.getCommissionRate())
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        PromoterCommission commission = new PromoterCommission();
        commission.setPromoterId(promoter.getId());
        commission.setPromoterUserId(promoter.getUserId());
        commission.setFromUserId(fromUserId);
        commission.setOrderNo(orderNo);
        commission.setOrderAmount(orderAmount);
        commission.setCommissionRate(promoter.getCommissionRate());
        commission.setCommissionAmount(commissionAmount);
        commission.setStatus(0); // 待结算

        commissionMapper.insert(commission);
        log.info("记录佣金: orderNo={}, amount={}", orderNo, commissionAmount);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean settleCommission(String orderNo) {
        PromoterCommission commission = commissionMapper.selectOne(
                new LambdaQueryWrapper<PromoterCommission>()
                        .eq(PromoterCommission::getOrderNo, orderNo)
                        .eq(PromoterCommission::getStatus, 0)
        );
        if (commission == null) {
            return false;
        }

        // 更新佣金状态
        int rows = commissionMapper.update(null,
                new LambdaUpdateWrapper<PromoterCommission>()
                        .eq(PromoterCommission::getId, commission.getId())
                        .eq(PromoterCommission::getStatus, 0)
                        .set(PromoterCommission::getStatus, 1)
                        .set(PromoterCommission::getSettleAt, LocalDateTime.now())
        );

        if (rows > 0) {
            // 更新推广员统计
            promoterInfoMapper.update(null,
                    new LambdaUpdateWrapper<PromoterInfo>()
                            .eq(PromoterInfo::getId, commission.getPromoterId())
                            .setSql("total_commission = total_commission + " + commission.getCommissionAmount())
                            .setSql("available_commission = available_commission + " + commission.getCommissionAmount())
                            .setSql("order_count = order_count + 1")
                            .setSql("order_amount = order_amount + " + commission.getOrderAmount())
            );
            log.info("结算佣金: orderNo={}", orderNo);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCommission(String orderNo) {
        PromoterCommission commission = commissionMapper.selectOne(
                new LambdaQueryWrapper<PromoterCommission>()
                        .eq(PromoterCommission::getOrderNo, orderNo)
                        .in(PromoterCommission::getStatus, 0, 1)
        );
        if (commission == null) {
            return false;
        }

        int oldStatus = commission.getStatus();

        // 更新佣金状态
        int rows = commissionMapper.update(null,
                new LambdaUpdateWrapper<PromoterCommission>()
                        .eq(PromoterCommission::getId, commission.getId())
                        .set(PromoterCommission::getStatus, 2)
        );

        if (rows > 0 && oldStatus == 1) {
            // 如果已结算，需要扣回
            promoterInfoMapper.update(null,
                    new LambdaUpdateWrapper<PromoterInfo>()
                            .eq(PromoterInfo::getId, commission.getPromoterId())
                            .setSql("total_commission = total_commission - " + commission.getCommissionAmount())
                            .setSql("available_commission = available_commission - " + commission.getCommissionAmount())
                            .setSql("order_count = order_count - 1")
                            .setSql("order_amount = order_amount - " + commission.getOrderAmount())
            );
            log.info("取消佣金: orderNo={}", orderNo);
        }
        return rows > 0;
    }

    @Override
    public List<PromoterCommission> listCommissions(Long promoterUserId, Integer status) {
        LambdaQueryWrapper<PromoterCommission> wrapper = new LambdaQueryWrapper<PromoterCommission>()
                .eq(PromoterCommission::getPromoterUserId, promoterUserId)
                .orderByDesc(PromoterCommission::getCreatedAt);
        if (status != null) {
            wrapper.eq(PromoterCommission::getStatus, status);
        }
        return commissionMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromoterWithdraw applyWithdraw(Long userId, BigDecimal amount, Integer withdrawType,
                                          String account, String accountName) {
        PromoterInfo promoter = getByUserId(userId);
        if (promoter == null || promoter.getStatus() != 1) {
            throw new BizException("您不是推广员");
        }

        if (amount.compareTo(MIN_WITHDRAW_AMOUNT) < 0) {
            throw new BizException("最低提现金额" + MIN_WITHDRAW_AMOUNT + "元");
        }

        if (amount.compareTo(promoter.getAvailableCommission()) > 0) {
            throw new BizException("可提现余额不足");
        }

        // 检查是否有待审核的提现
        Long pendingCount = withdrawMapper.selectCount(
                new LambdaQueryWrapper<PromoterWithdraw>()
                        .eq(PromoterWithdraw::getPromoterUserId, userId)
                        .eq(PromoterWithdraw::getStatus, 0)
        );
        if (pendingCount > 0) {
            throw new BizException("您有待审核的提现申请");
        }

        // 扣减可用余额
        int rows = promoterInfoMapper.update(null,
                new LambdaUpdateWrapper<PromoterInfo>()
                        .eq(PromoterInfo::getId, promoter.getId())
                        .ge(PromoterInfo::getAvailableCommission, amount)
                        .setSql("available_commission = available_commission - " + amount)
        );
        if (rows == 0) {
            throw new BizException("可提现余额不足");
        }

        PromoterWithdraw withdraw = new PromoterWithdraw();
        withdraw.setPromoterId(promoter.getId());
        withdraw.setPromoterUserId(userId);
        withdraw.setAmount(amount);
        withdraw.setWithdrawType(withdrawType);
        withdraw.setAccount(account);
        withdraw.setAccountName(accountName);
        withdraw.setStatus(0);

        withdrawMapper.insert(withdraw);
        log.info("提现申请: userId={}, amount={}", userId, amount);
        return withdraw;
    }

    @Override
    public List<PromoterWithdraw> listWithdraws(Long userId) {
        return withdrawMapper.selectList(
                new LambdaQueryWrapper<PromoterWithdraw>()
                        .eq(PromoterWithdraw::getPromoterUserId, userId)
                        .orderByDesc(PromoterWithdraw::getCreatedAt)
        );
    }

    @Override
    public PromoterInfo getStatistics(Long userId) {
        return getByUserId(userId);
    }

    private String generatePromoCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
