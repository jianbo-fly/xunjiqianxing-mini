package com.xunjiqianxing.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderTravelerMapper;
import com.xunjiqianxing.service.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMainMapper orderMainMapper;
    private final OrderTravelerMapper orderTravelerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMain createOrder(OrderMain order, List<OrderTraveler> travelers) {
        // 生成订单编号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setPayStatus(0);
        order.setIsDeleted(0);

        // 设置支付超时时间（30分钟）
        order.setExpireAt(LocalDateTime.now().plusMinutes(30));

        orderMainMapper.insert(order);

        // 保存出行人
        if (travelers != null && !travelers.isEmpty()) {
            for (OrderTraveler traveler : travelers) {
                traveler.setOrderId(order.getId());
                orderTravelerMapper.insert(traveler);
            }
        }

        log.info("订单创建成功: orderNo={}, userId={}", orderNo, order.getUserId());
        return order;
    }

    @Override
    public OrderMain getById(Long id) {
        return orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getId, id)
                        .eq(OrderMain::getIsDeleted, 0)
        );
    }

    @Override
    public OrderMain getByOrderNo(String orderNo) {
        return orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getOrderNo, orderNo)
                        .eq(OrderMain::getIsDeleted, 0)
        );
    }

    @Override
    public PageResult<OrderMain> pageUserOrders(Long userId, Integer status, PageQuery pageQuery) {
        Page<OrderMain> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getUserId, userId)
                .eq(OrderMain::getIsDeleted, 0);

        if (status != null) {
            wrapper.eq(OrderMain::getStatus, status);
        }

        wrapper.orderByDesc(OrderMain::getCreatedAt);

        Page<OrderMain> result = orderMainMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(),
                pageQuery.getPage(), pageQuery.getPageSize());
    }

    @Override
    public List<OrderTraveler> getOrderTravelers(Long orderId) {
        return orderTravelerMapper.selectList(
                new LambdaQueryWrapper<OrderTraveler>()
                        .eq(OrderTraveler::getOrderId, orderId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, Long userId, String reason) {
        OrderMain order = getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException("无权操作此订单");
        }
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BizException("当前订单状态不允许取消");
        }

        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .eq(OrderMain::getId, orderId)
                        .eq(OrderMain::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .set(OrderMain::getStatus, OrderStatus.CANCELLED.getCode())
                        .set(OrderMain::getCancelTime, LocalDateTime.now())
                        .set(OrderMain::getCancelReason, reason)
                        .set(OrderMain::getCancelType, 1)
        );

        if (rows > 0) {
            log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
            // TODO: 释放库存
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean paySuccess(String orderNo, String payTradeNo) {
        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .eq(OrderMain::getOrderNo, orderNo)
                        .eq(OrderMain::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .set(OrderMain::getStatus, OrderStatus.PENDING_CONFIRM.getCode())
                        .set(OrderMain::getPayStatus, 1)
                        .set(OrderMain::getPayTime, LocalDateTime.now())
                        .set(OrderMain::getPayTradeNo, payTradeNo)
        );

        if (rows > 0) {
            log.info("订单支付成功: orderNo={}", orderNo);
            // TODO: 发送通知
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long orderId, Integer fromStatus, Integer toStatus) {
        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .eq(OrderMain::getId, orderId)
                        .eq(OrderMain::getStatus, fromStatus)
                        .set(OrderMain::getStatus, toStatus)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyRefund(Long orderId, Long userId, String reason) {
        OrderMain order = getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BizException("无权操作此订单");
        }

        // 只有已支付的订单才能申请退款
        List<Integer> allowStatus = OrderStatus.refundableStatuses();
        if (!allowStatus.contains(order.getStatus())) {
            throw new BizException("当前订单状态不允许申请退款");
        }

        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .eq(OrderMain::getId, orderId)
                        .in(OrderMain::getStatus, allowStatus)
                        .set(OrderMain::getStatus, OrderStatus.REFUND_APPLY.getCode())
        );

        if (rows > 0) {
            log.info("申请退款成功: orderId={}, userId={}", orderId, userId);
            // TODO: 创建退款记录
        }
        return rows > 0;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = IdGenerator.nextId() % 100000;
        return "XJ" + date + String.format("%05d", seq);
    }
}
