package com.xunjiqianxing.service.payment.service;

import com.xunjiqianxing.service.payment.entity.PaymentRecord;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付服务
 */
public interface PaymentService {

    /**
     * 创建支付
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param bizNo 业务编号
     * @param userId 用户ID
     * @param amount 金额
     * @param description 商品描述
     * @param openid 用户openid
     * @return 支付参数（用于小程序调起支付）
     */
    Map<String, String> createPayment(String bizType, Long bizId, String bizNo,
                                       Long userId, BigDecimal amount,
                                       String description, String openid);

    /**
     * 处理支付回调
     * @param notifyData 回调数据
     * @return 处理结果
     */
    boolean handleNotify(String notifyData);

    /**
     * 查询支付状态
     * @param paymentNo 支付流水号
     * @return 支付记录
     */
    PaymentRecord queryPayment(String paymentNo);

    /**
     * 根据业务查询支付记录
     */
    PaymentRecord getByBiz(String bizType, Long bizId);

    /**
     * 关闭支付
     */
    boolean closePayment(String paymentNo);

    /**
     * 申请退款
     * @param paymentNo 支付流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款单号
     */
    String refund(String paymentNo, BigDecimal refundAmount, String refundReason);

    /**
     * 处理退款回调
     */
    boolean handleRefundNotify(String notifyData);
}
