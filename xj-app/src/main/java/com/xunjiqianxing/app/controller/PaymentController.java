package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.payment.entity.PaymentRecord;
import com.xunjiqianxing.service.payment.service.PaymentService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.Map;

/**
 * 支付接口
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付接口", description = "支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;

    /**
     * 创建订单支付
     */
    @PostMapping("/order/{orderId}")
    @Operation(summary = "创建订单支付", description = "创建订单支付，返回小程序调起支付参数")
    public Result<Map<String, String>> payOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {

        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        OrderMain order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.fail("订单状态不正确");
        }

        Map<String, String> payParams = paymentService.createPayment(
                "order",
                order.getId(),
                order.getOrderNo(),
                userId,
                order.getPayAmount(),
                "寻迹千行-" + order.getProductName(),
                user.getOpenid()
        );

        return Result.success(payParams);
    }

    /**
     * 创建会员支付
     */
    @PostMapping("/member/{orderId}")
    @Operation(summary = "创建会员支付", description = "创建会员购买支付")
    public Result<Map<String, String>> payMember(
            @Parameter(description = "会员订单ID") @PathVariable Long orderId) {

        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        // TODO: 获取会员订单并验证
        // MemberOrder memberOrder = memberService.getOrderById(orderId);

        Map<String, String> payParams = paymentService.createPayment(
                "member",
                orderId,
                "MB" + orderId,
                userId,
                new java.math.BigDecimal("99"), // TODO: 从订单获取
                "寻迹千行会员",
                user.getOpenid()
        );

        return Result.success(payParams);
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{paymentNo}")
    @Operation(summary = "查询支付状态", description = "查询支付状态")
    public Result<PaymentRecord> queryStatus(
            @Parameter(description = "支付流水号") @PathVariable String paymentNo) {

        Long userId = StpUtil.getLoginIdAsLong();
        PaymentRecord record = paymentService.queryPayment(paymentNo);

        if (record == null || !record.getUserId().equals(userId)) {
            return Result.fail("支付记录不存在");
        }

        return Result.success(record);
    }
}
