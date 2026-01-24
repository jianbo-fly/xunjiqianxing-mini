package com.xunjiqianxing.service.promotion.service;

import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.entity.PromoterWithdraw;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推广员服务
 */
public interface PromoterService {

    /**
     * 申请成为推广员
     */
    PromoterInfo apply(Long userId, String realName, String phone);

    /**
     * 获取推广员信息
     */
    PromoterInfo getByUserId(Long userId);

    /**
     * 根据推广码获取推广员
     */
    PromoterInfo getByPromoCode(String promoCode);

    /**
     * 绑定推广员
     */
    boolean bind(Long userId, String promoCode);

    /**
     * 记录佣金
     */
    boolean recordCommission(Long fromUserId, String orderNo, BigDecimal orderAmount);

    /**
     * 结算佣金
     */
    boolean settleCommission(String orderNo);

    /**
     * 取消佣金
     */
    boolean cancelCommission(String orderNo);

    /**
     * 获取佣金记录
     */
    List<PromoterCommission> listCommissions(Long promoterUserId, Integer status);

    /**
     * 申请提现
     */
    PromoterWithdraw applyWithdraw(Long userId, BigDecimal amount, Integer withdrawType, String account, String accountName);

    /**
     * 获取提现记录
     */
    List<PromoterWithdraw> listWithdraws(Long userId);

    /**
     * 获取推广统计
     */
    PromoterInfo getStatistics(Long userId);
}
