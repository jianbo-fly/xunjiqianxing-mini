package com.xunjiqianxing.app.controller;

import com.xunjiqianxing.service.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 支付回调接口（无需登录）
 */
@Slf4j
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final PaymentService paymentService;

    /**
     * 微信支付回调
     */
    @PostMapping("/pay/wechat")
    public String payNotify(HttpServletRequest request) {
        try {
            String notifyData = readRequestBody(request);
            log.info("收到微信支付回调");

            boolean success = paymentService.handleNotify(notifyData);

            if (success) {
                return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
            } else {
                return "{\"code\":\"FAIL\",\"message\":\"处理失败\"}";
            }

        } catch (Exception e) {
            log.error("处理支付回调异常", e);
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 微信退款回调
     */
    @PostMapping("/refund/wechat")
    public String refundNotify(HttpServletRequest request) {
        try {
            String notifyData = readRequestBody(request);
            log.info("收到微信退款回调");

            boolean success = paymentService.handleRefundNotify(notifyData);

            if (success) {
                return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
            } else {
                return "{\"code\":\"FAIL\",\"message\":\"处理失败\"}";
            }

        } catch (Exception e) {
            log.error("处理退款回调异常", e);
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
