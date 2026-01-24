package com.xunjiqianxing.service.member.service;

import com.xunjiqianxing.service.member.entity.MemberOrder;

import java.math.BigDecimal;

/**
 * 会员服务
 */
public interface MemberService {

    /**
     * 获取会员价格
     */
    BigDecimal getMemberPrice();

    /**
     * 创建会员订单
     */
    MemberOrder createOrder(Long userId, Integer months);

    /**
     * 会员订单支付成功
     */
    boolean paySuccess(String orderNo);

    /**
     * 检查用户是否为有效会员
     */
    boolean isValidMember(Long userId);

    /**
     * 获取会员到期时间描述
     */
    String getMemberExpireDesc(Long userId);
}
