package com.xunjiqianxing.service.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.payment.entity.PaymentRecord;
import com.xunjiqianxing.service.payment.mapper.PaymentRecordMapper;
import com.xunjiqianxing.service.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final WxPayService wxPayService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> createPayment(String bizType, Long bizId, String bizNo,
                                              Long userId, BigDecimal amount,
                                              String description, String openid) {
        // 检查是否已有未支付的记录
        PaymentRecord existing = getByBiz(bizType, bizId);
        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new BizException("该订单已支付");
            }
            // 已有待支付记录，关闭后重新创建
            if (existing.getStatus() == 0) {
                closePayment(existing.getPaymentNo());
            }
        }

        // 生成支付流水号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        PaymentRecord record = new PaymentRecord();
        record.setPaymentNo(paymentNo);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setBizNo(bizNo);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setPayChannel("wechat");
        record.setPayType("jsapi");
        record.setStatus(0);
        record.setExpireAt(LocalDateTime.now().plusMinutes(30));

        // 调用微信统一下单
        try {
            WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
            request.setOutTradeNo(paymentNo);
            request.setDescription(description);
            request.setNotifyUrl(wxPayService.getConfig().getNotifyUrl());

            WxPayUnifiedOrderV3Request.Amount amountInfo = new WxPayUnifiedOrderV3Request.Amount();
            amountInfo.setTotal(amount.multiply(BigDecimal.valueOf(100)).intValue());
            amountInfo.setCurrency("CNY");
            request.setAmount(amountInfo);

            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(openid);
            request.setPayer(payer);

            // 设置过期时间
            request.setTimeExpire(record.getExpireAt()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")));

            Object result = wxPayService.createOrderV3(TradeTypeEnum.JSAPI, request);

            // 保存prepay_id
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> resultMap = (Map<String, String>) result;
                record.setPrepayId(resultMap.get("prepay_id"));
            }

            paymentRecordMapper.insert(record);
            log.info("创建支付成功: paymentNo={}, bizNo={}", paymentNo, bizNo);

            // 返回小程序调起支付所需参数
            @SuppressWarnings("unchecked")
            Map<String, String> payParams = (Map<String, String>) result;
            payParams.put("paymentNo", paymentNo);
            return payParams;

        } catch (WxPayException e) {
            log.error("创建微信支付失败: {}", e.getMessage(), e);
            throw new BizException("创建支付失败: " + e.getErrCodeDes());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(String notifyData) {
        try {

            WxPayNotifyV3Result result = wxPayService.parseOrderNotifyV3Result(notifyData, null);
            WxPayNotifyV3Result.DecryptNotifyResult decryptResult = result.getResult();

            String paymentNo = decryptResult.getOutTradeNo();
            String transactionId = decryptResult.getTransactionId();
            String tradeState = decryptResult.getTradeState();

            log.info("收到支付回调: paymentNo={}, transactionId={}, state={}",
                    paymentNo, transactionId, tradeState);

            if (!"SUCCESS".equals(tradeState)) {
                log.warn("支付未成功: paymentNo={}, state={}", paymentNo, tradeState);
                return true;
            }

            // 更新支付记录
            int rows = paymentRecordMapper.update(null,
                    new LambdaUpdateWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getPaymentNo, paymentNo)
                            .eq(PaymentRecord::getStatus, 0)
                            .set(PaymentRecord::getStatus, 1)
                            .set(PaymentRecord::getTransactionId, transactionId)
                            .set(PaymentRecord::getPayTime, LocalDateTime.now())
                            .set(PaymentRecord::getNotifyData, notifyData)
            );

            if (rows > 0) {
                // 发布支付成功事件
                PaymentRecord record = paymentRecordMapper.selectOne(
                        new LambdaQueryWrapper<PaymentRecord>()
                                .eq(PaymentRecord::getPaymentNo, paymentNo)
                );
                eventPublisher.publishEvent(new PaymentSuccessEvent(this, record));
                log.info("支付成功处理完成: paymentNo={}", paymentNo);
            }

            return true;

        } catch (WxPayException e) {
            log.error("解析支付回调失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PaymentRecord queryPayment(String paymentNo) {
        return paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getPaymentNo, paymentNo)
        );
    }

    @Override
    public PaymentRecord getByBiz(String bizType, Long bizId) {
        return paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getBizType, bizType)
                        .eq(PaymentRecord::getBizId, bizId)
                        .orderByDesc(PaymentRecord::getCreatedAt)
                        .last("LIMIT 1")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePayment(String paymentNo) {
        PaymentRecord record = queryPayment(paymentNo);
        if (record == null || record.getStatus() != 0) {
            return false;
        }

        try {
            wxPayService.closeOrderV3(paymentNo);
        } catch (WxPayException e) {
            log.warn("关闭微信订单失败(可忽略): {}", e.getMessage());
        }

        int rows = paymentRecordMapper.update(null,
                new LambdaUpdateWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getPaymentNo, paymentNo)
                        .eq(PaymentRecord::getStatus, 0)
                        .set(PaymentRecord::getStatus, 2)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refund(String paymentNo, BigDecimal refundAmount, String refundReason) {
        PaymentRecord record = queryPayment(paymentNo);
        if (record == null) {
            throw new BizException("支付记录不存在");
        }
        if (record.getStatus() != 1) {
            throw new BizException("该订单未支付，无法退款");
        }

        String refundNo = generateRefundNo();

        try {
            WxPayRefundV3Request request = new WxPayRefundV3Request();
            request.setOutTradeNo(paymentNo);
            request.setOutRefundNo(refundNo);
            request.setReason(refundReason);

            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            amount.setRefund(refundAmount.multiply(BigDecimal.valueOf(100)).intValue());
            amount.setTotal(record.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            amount.setCurrency("CNY");
            request.setAmount(amount);

            request.setNotifyUrl(wxPayService.getConfig().getNotifyUrl().replace("/pay/", "/refund/"));

            WxPayRefundV3Result result = wxPayService.refundV3(request);
            log.info("退款申请成功: paymentNo={}, refundNo={}, status={}",
                    paymentNo, refundNo, result.getStatus());

            return refundNo;

        } catch (WxPayException e) {
            log.error("退款失败: {}", e.getMessage(), e);
            throw new BizException("退款失败: " + e.getErrCodeDes());
        }
    }

    @Override
    public boolean handleRefundNotify(String notifyData) {
        // TODO: 处理退款回调
        log.info("收到退款回调: {}", notifyData);
        return true;
    }

    private String generatePaymentNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = IdGenerator.nextId() % 100000;
        return "PAY" + date + String.format("%05d", seq);
    }

    private String generateRefundNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = IdGenerator.nextId() % 100000;
        return "REF" + date + String.format("%05d", seq);
    }

    /**
     * 支付成功事件
     */
    public static class PaymentSuccessEvent extends org.springframework.context.ApplicationEvent {
        private final PaymentRecord paymentRecord;

        public PaymentSuccessEvent(Object source, PaymentRecord paymentRecord) {
            super(source);
            this.paymentRecord = paymentRecord;
        }

        public PaymentRecord getPaymentRecord() {
            return paymentRecord;
        }
    }
}
