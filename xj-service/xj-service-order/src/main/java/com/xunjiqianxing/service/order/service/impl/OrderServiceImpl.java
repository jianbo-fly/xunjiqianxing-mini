package com.xunjiqianxing.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.order.entity.OrderLog;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderLogMapper;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderRefundMapper;
import com.xunjiqianxing.service.order.mapper.OrderTravelerMapper;
import com.xunjiqianxing.service.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMainMapper orderMainMapper;
    private final OrderTravelerMapper orderTravelerMapper;
    private final OrderRefundMapper orderRefundMapper;
    private final OrderLogMapper orderLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderMain createOrder(OrderMain order, List<OrderTraveler> travelers) {
        // 生成订单编号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
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

        addLog(order.getId(), order.getOrderNo(), null, OrderStatus.PENDING_PAY.getCode(),
                "user", order.getUserId(), "创建订单");
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
            addLog(orderId, order.getOrderNo(), OrderStatus.PENDING_PAY.getCode(),
                    OrderStatus.CANCELLED.getCode(), "user", userId,
                    reason != null ? "用户取消: " + reason : "用户取消");
            log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
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
                        .set(OrderMain::getStatus, OrderStatus.BOOKED.getCode())
                        .set(OrderMain::getPayTime, LocalDateTime.now())
                        .set(OrderMain::getPayTradeNo, payTradeNo)
        );

        if (rows > 0) {
            OrderMain paid = getByOrderNo(orderNo);
            if (paid != null) {
                addLog(paid.getId(), orderNo, OrderStatus.PENDING_PAY.getCode(),
                        OrderStatus.BOOKED.getCode(), "system", null, "支付成功");
            }
            log.info("订单支付成功: orderNo={}", orderNo);
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
            addLog(orderId, order.getOrderNo(), order.getStatus(),
                    OrderStatus.REFUND_APPLY.getCode(), "user", userId,
                    reason != null ? "申请退款: " + reason : "申请退款");
            log.info("申请退款成功: orderId={}, userId={}", orderId, userId);
            // 创建退款记录
            OrderRefund refund = new OrderRefund();
            refund.setRefundNo(generateRefundNo());
            refund.setOrderId(order.getId());
            refund.setOrderNo(order.getOrderNo());
            refund.setUserId(userId);
            refund.setRefundAmount(order.getPayAmount());
            refund.setRefundRatio(100);
            refund.setReason(reason);
            refund.setStatus(0);
            orderRefundMapper.insert(refund);
        }
        return rows > 0;
    }

    @Override
    public List<OrderMain> getExpiredPendingOrders(LocalDateTime now) {
        return orderMainMapper.selectList(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .eq(OrderMain::getIsDeleted, 0)
                        .lt(OrderMain::getExpireAt, now)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeExpiredOrder(Long orderId) {
        OrderMain order = getById(orderId);
        if (order == null) return false;

        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .eq(OrderMain::getId, orderId)
                        .eq(OrderMain::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .set(OrderMain::getStatus, OrderStatus.CLOSED.getCode())
                        .set(OrderMain::getCancelTime, LocalDateTime.now())
                        .set(OrderMain::getCancelReason, "支付超时自动关闭")
                        .set(OrderMain::getCancelType, 2)
        );
        if (rows > 0) {
            addLog(orderId, order.getOrderNo(), OrderStatus.PENDING_PAY.getCode(),
                    OrderStatus.CLOSED.getCode(), "system", null, "支付超时自动关闭");
        }
        return rows > 0;
    }

    @Override
    public void addLog(Long orderId, String orderNo, Integer fromStatus, Integer toStatus,
                       String operatorType, Long operatorId, String remark) {
        try {
            OrderLog entry = new OrderLog();
            entry.setOrderId(orderId);
            entry.setOrderNo(orderNo);
            entry.setFromStatus(fromStatus);
            entry.setToStatus(toStatus);
            entry.setOperatorType(operatorType);
            entry.setOperatorId(operatorId);
            entry.setRemark(remark);
            orderLogMapper.insert(entry);
        } catch (Exception e) {
            // 日志写入失败不影响主流程
            log.warn("订单日志写入失败: orderId={}, error={}", orderId, e.getMessage());
        }
    }

    @Override
    public List<OrderMain> getBookedOrdersToTravel(LocalDate today) {
        return orderMainMapper.selectList(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getStatus, OrderStatus.BOOKED.getCode())
                        .eq(OrderMain::getIsDeleted, 0)
                        .le(OrderMain::getStartDate, today)
        );
    }

    @Override
    public List<OrderMain> getTravelingOrdersToComplete(LocalDate today) {
        return orderMainMapper.selectList(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getStatus, OrderStatus.TRAVELING.getCode())
                        .eq(OrderMain::getIsDeleted, 0)
                        .lt(OrderMain::getEndDate, today)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkTraveling(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return 0;
        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .in(OrderMain::getId, orderIds)
                        .eq(OrderMain::getStatus, OrderStatus.BOOKED.getCode())
                        .set(OrderMain::getStatus, OrderStatus.TRAVELING.getCode())
        );
        // 批量写日志
        orderIds.forEach(id -> addLog(id, null, OrderStatus.BOOKED.getCode(),
                OrderStatus.TRAVELING.getCode(), "system", null, "出行日已到，自动更新为出行中"));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkCompleted(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return 0;
        int rows = orderMainMapper.update(null,
                new LambdaUpdateWrapper<OrderMain>()
                        .in(OrderMain::getId, orderIds)
                        .eq(OrderMain::getStatus, OrderStatus.TRAVELING.getCode())
                        .set(OrderMain::getStatus, OrderStatus.COMPLETED.getCode())
                        .set(OrderMain::getCompleteTime, LocalDateTime.now())
        );
        orderIds.forEach(id -> addLog(id, null, OrderStatus.TRAVELING.getCode(),
                OrderStatus.COMPLETED.getCode(), "system", null, "行程结束，自动更新为已完成"));
        return rows;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = IdGenerator.nextId() % 100000;
        return "XJ" + date + String.format("%05d", seq);
    }

    /**
     * 生成退款编号
     */
    private String generateRefundNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long seq = IdGenerator.nextId() % 100000;
        return "TK" + date + String.format("%05d", seq);
    }
}
