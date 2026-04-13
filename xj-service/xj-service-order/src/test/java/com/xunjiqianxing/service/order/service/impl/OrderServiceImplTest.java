package com.xunjiqianxing.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderLog;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderLogMapper;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderRefundMapper;
import com.xunjiqianxing.service.order.mapper.OrderTravelerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试（#83 ~ #116）
 */
class OrderServiceImplTest extends ServiceTestBase {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderMainMapper orderMainMapper;
    @Mock
    private OrderTravelerMapper orderTravelerMapper;
    @Mock
    private OrderRefundMapper orderRefundMapper;
    @Mock
    private OrderLogMapper orderLogMapper;

    // ==================== 辅助方法 ====================

    private OrderMain buildOrder(Long id, Long userId, Integer status) {
        OrderMain o = new OrderMain();
        o.setId(id);
        o.setUserId(userId);
        o.setStatus(status);
        o.setOrderNo("XJ20260412000001");
        o.setProductName("测试路线");
        o.setPayAmount(BigDecimal.valueOf(999));
        return o;
    }

    private OrderTraveler buildTraveler(String name) {
        OrderTraveler t = new OrderTraveler();
        t.setName(name);
        return t;
    }

    // ==================== createOrder #83-85 ====================

    @Nested
    @DisplayName("createOrder - 创建订单")
    class CreateOrder {

        @Test
        @DisplayName("#83 正常创建，生成唯一订单号")
        void shouldGenerateUniqueOrderNo() {
            OrderMain order = new OrderMain();
            order.setUserId(1L);
            OrderTraveler t = buildTraveler("张三");

            when(orderMainMapper.insert(any())).thenReturn(1);
            when(orderTravelerMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            OrderMain result = orderService.createOrder(order, List.of(t));

            assertNotNull(result.getOrderNo());
            assertTrue(result.getOrderNo().startsWith("XJ"));
            assertEquals(OrderStatus.PENDING_PAY.getCode(), result.getStatus());
            assertEquals(0, result.getIsDeleted());
            assertNotNull(result.getExpireAt());
        }

        @Test
        @DisplayName("#83-2 订单号不重复")
        void shouldGenerateDifferentOrderNos() {
            when(orderMainMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            OrderMain o1 = new OrderMain();
            o1.setUserId(1L);
            OrderMain o2 = new OrderMain();
            o2.setUserId(1L);

            orderService.createOrder(o1, Collections.emptyList());
            orderService.createOrder(o2, Collections.emptyList());

            assertNotEquals(o1.getOrderNo(), o2.getOrderNo());
        }

        @Test
        @DisplayName("#85 创建后自动写入订单日志")
        void shouldWriteOrderLog() {
            OrderMain order = new OrderMain();
            order.setUserId(1L);

            when(orderMainMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.createOrder(order, Collections.emptyList());

            ArgumentCaptor<OrderLog> cap = ArgumentCaptor.forClass(OrderLog.class);
            verify(orderLogMapper).insert(cap.capture());

            OrderLog log = cap.getValue();
            assertNull(log.getFromStatus());
            assertEquals(OrderStatus.PENDING_PAY.getCode(), log.getToStatus());
            assertEquals("user", log.getOperatorType());
        }

        @Test
        @DisplayName("#83-3 出行人正确关联订单ID")
        void shouldAssociateTravelersWithOrderId() {
            OrderMain order = new OrderMain();
            order.setUserId(1L);
            OrderTraveler t1 = buildTraveler("张三");
            OrderTraveler t2 = buildTraveler("李四");

            when(orderMainMapper.insert(any(OrderMain.class))).thenAnswer(inv -> {
                OrderMain o = inv.getArgument(0);
                o.setId(100L);
                return 1;
            });
            when(orderTravelerMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.createOrder(order, List.of(t1, t2));

            assertEquals(100L, t1.getOrderId());
            assertEquals(100L, t2.getOrderId());
            verify(orderTravelerMapper, times(2)).insert(any());
        }

        @Test
        @DisplayName("#83-4 出行人列表为空时不插入出行人")
        void shouldNotInsertTravelersWhenEmpty() {
            OrderMain order = new OrderMain();
            order.setUserId(1L);

            when(orderMainMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.createOrder(order, Collections.emptyList());

            verify(orderTravelerMapper, never()).insert(any());
        }

        @Test
        @DisplayName("#83-5 出行人列表为null时不插入出行人")
        void shouldNotInsertTravelersWhenNull() {
            OrderMain order = new OrderMain();
            order.setUserId(1L);

            when(orderMainMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.createOrder(order, null);

            verify(orderTravelerMapper, never()).insert(any());
        }
    }

    // ==================== cancelOrder #86-91 ====================

    @Nested
    @DisplayName("cancelOrder - 取消订单")
    class CancelOrder {

        @Test
        @DisplayName("#86 成功取消待支付订单")
        void shouldCancelPendingOrder() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            boolean result = orderService.cancelOrder(1L, 1L, "不想要了");

            assertTrue(result);
            verify(orderMainMapper).update(any(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("#88 订单不存在")
        void shouldThrowWhenOrderNotFound() {
            when(orderMainMapper.selectOne(any())).thenReturn(null);

            BizException ex = assertThrows(BizException.class,
                    () -> orderService.cancelOrder(999L, 1L, "取消"));
            assertTrue(ex.getMessage().contains("订单不存在"));
        }

        @Test
        @DisplayName("#89 非订单创建者取消")
        void shouldThrowWhenNotOwner() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);

            BizException ex = assertThrows(BizException.class,
                    () -> orderService.cancelOrder(1L, 999L, "取消"));
            assertTrue(ex.getMessage().contains("无权操作"));
        }

        @Test
        @DisplayName("#90 已支付订单不允许取消")
        void shouldThrowWhenAlreadyPaid() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.BOOKED.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);

            BizException ex = assertThrows(BizException.class,
                    () -> orderService.cancelOrder(1L, 1L, "取消"));
            assertTrue(ex.getMessage().contains("不允许取消"));
        }

        @Test
        @DisplayName("#91 并发取消（乐观锁失败）返回 false")
        void shouldReturnFalseWhenConcurrentCancel() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            boolean result = orderService.cancelOrder(1L, 1L, "取消");

            assertFalse(result);
        }

        @Test
        @DisplayName("#86-2 取消成功写入日志")
        void shouldWriteLogOnCancel() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.cancelOrder(1L, 1L, "不想要了");

            ArgumentCaptor<OrderLog> cap = ArgumentCaptor.forClass(OrderLog.class);
            verify(orderLogMapper).insert(cap.capture());
            assertEquals(OrderStatus.PENDING_PAY.getCode(), cap.getValue().getFromStatus());
            assertEquals(OrderStatus.CANCELLED.getCode(), cap.getValue().getToStatus());
        }
    }

    // ==================== paySuccess #92-94 ====================

    @Nested
    @DisplayName("paySuccess - 支付成功")
    class PaySuccess {

        @Test
        @DisplayName("#92 支付成功更新状态为已预订")
        void shouldUpdateStatusToBooked() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            OrderMain paidOrder = buildOrder(1L, 1L, OrderStatus.BOOKED.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(paidOrder);
            when(orderLogMapper.insert(any())).thenReturn(1);

            boolean result = orderService.paySuccess("XJ20260412000001", "wx_trade_001");

            assertTrue(result);
        }

        @Test
        @DisplayName("#93 写入支付流水号")
        void shouldSetPayTradeNo() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            OrderMain paidOrder = buildOrder(1L, 1L, OrderStatus.BOOKED.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(paidOrder);
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.paySuccess("XJ20260412000001", "wx_trade_001");

            verify(orderMainMapper).update(any(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("#94 重复回调（订单已支付）幂等处理")
        void shouldHandleIdempotentCallback() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            boolean result = orderService.paySuccess("XJ20260412000001", "wx_trade_001");

            assertFalse(result);
        }
    }

    // ==================== applyRefund #95-98 ====================

    @Nested
    @DisplayName("applyRefund - 申请退款")
    class ApplyRefund {

        @Test
        @DisplayName("#95 已预订状态申请退款")
        void shouldApplyRefundForBookedOrder() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.BOOKED.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderRefundMapper.insert(any())).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            boolean result = orderService.applyRefund(1L, 1L, "行程变更");

            assertTrue(result);
            ArgumentCaptor<OrderRefund> cap = ArgumentCaptor.forClass(OrderRefund.class);
            verify(orderRefundMapper).insert(cap.capture());
            OrderRefund refund = cap.getValue();
            assertNotNull(refund.getRefundNo());
            assertTrue(refund.getRefundNo().startsWith("TK"));
            assertEquals(0, refund.getStatus());
            assertEquals(order.getPayAmount(), refund.getRefundAmount());
        }

        @Test
        @DisplayName("#96 出行中状态不允许退款")
        void shouldThrowWhenTraveling() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.TRAVELING.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);

            BizException ex = assertThrows(BizException.class,
                    () -> orderService.applyRefund(1L, 1L, "退款"));
            assertTrue(ex.getMessage().contains("不允许"));
        }

        @Test
        @DisplayName("#97 非订单创建者申请")
        void shouldThrowWhenNotOwnerAppliesRefund() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.BOOKED.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);

            BizException ex = assertThrows(BizException.class,
                    () -> orderService.applyRefund(1L, 999L, "退款"));
            assertTrue(ex.getMessage().contains("无权操作"));
        }

        @Test
        @DisplayName("#98 订单不存在")
        void shouldThrowWhenOrderNotFoundForRefund() {
            when(orderMainMapper.selectOne(any())).thenReturn(null);

            BizException ex = assertThrows(BizException.class,
                    () -> orderService.applyRefund(999L, 1L, "退款"));
            assertTrue(ex.getMessage().contains("订单不存在"));
        }

        @Test
        @DisplayName("#95-2 待支付状态不允许退款")
        void shouldThrowWhenPendingPay() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);

            assertThrows(BizException.class,
                    () -> orderService.applyRefund(1L, 1L, "退款"));
        }

        @Test
        @DisplayName("#95-3 已取消状态不允许退款")
        void shouldThrowWhenCancelled() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.CANCELLED.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);

            assertThrows(BizException.class,
                    () -> orderService.applyRefund(1L, 1L, "退款"));
        }
    }

    // ==================== updateStatus #99-100 ====================

    @Nested
    @DisplayName("updateStatus - 更新状态")
    class UpdateStatus {

        @Test
        @DisplayName("#99 正常更新状态")
        void shouldUpdateStatusSuccessfully() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            boolean result = orderService.updateStatus(1L,
                    OrderStatus.PENDING_PAY.getCode(), OrderStatus.BOOKED.getCode());

            assertTrue(result);
        }

        @Test
        @DisplayName("#100 fromStatus 不匹配（并发冲突）")
        void shouldReturnFalseWhenStatusMismatch() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            boolean result = orderService.updateStatus(1L,
                    OrderStatus.PENDING_PAY.getCode(), OrderStatus.BOOKED.getCode());

            assertFalse(result);
        }
    }

    // ==================== pageUserOrders #101-103 ====================

    @Nested
    @DisplayName("pageUserOrders - 分页查询")
    class PageUserOrders {

        @Test
        @DisplayName("#101 查全部订单")
        void shouldPageAllOrders() {
            Page<OrderMain> mockPage = new Page<>(1, 10);
            mockPage.setRecords(List.of(buildOrder(1L, 1L, 0)));
            mockPage.setTotal(1);
            when(orderMainMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

            PageQuery pq = new PageQuery();
            pq.setPage(1);
            pq.setPageSize(10);
            PageResult<OrderMain> result = orderService.pageUserOrders(1L, null, pq);

            assertEquals(1, result.getTotal());
            assertEquals(1, result.getList().size());
        }

        @Test
        @DisplayName("#102 按状态筛选")
        void shouldFilterByStatus() {
            Page<OrderMain> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0);
            when(orderMainMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

            PageQuery pq = new PageQuery();
            PageResult<OrderMain> result = orderService.pageUserOrders(1L, 0, pq);

            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("#103 用户无订单")
        void shouldReturnEmptyForNoOrders() {
            Page<OrderMain> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0);
            when(orderMainMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

            PageQuery pq = new PageQuery();
            PageResult<OrderMain> result = orderService.pageUserOrders(1L, null, pq);

            assertTrue(result.getList().isEmpty());
            assertEquals(0, result.getTotal());
        }
    }

    // ==================== 定时任务 #104-109 ====================

    @Nested
    @DisplayName("定时任务相关")
    class ScheduledTasks {

        @Test
        @DisplayName("#104 getExpiredPendingOrders 返回超时未支付订单")
        void shouldReturnExpiredOrders() {
            OrderMain expired = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(expired));

            List<OrderMain> result = orderService.getExpiredPendingOrders(LocalDateTime.now());

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("#105 closeExpiredOrder 关闭过期订单")
        void shouldCloseExpiredOrder() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderLogMapper.insert(any())).thenReturn(1);

            boolean result = orderService.closeExpiredOrder(1L);

            assertTrue(result);
            ArgumentCaptor<OrderLog> cap = ArgumentCaptor.forClass(OrderLog.class);
            verify(orderLogMapper).insert(cap.capture());
            assertEquals(OrderStatus.CLOSED.getCode(), cap.getValue().getToStatus());
        }

        @Test
        @DisplayName("#106 closeExpiredOrder 订单已被支付则不关闭")
        void shouldNotCloseAlreadyPaidOrder() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.PENDING_PAY.getCode());
            when(orderMainMapper.selectOne(any())).thenReturn(order);
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            boolean result = orderService.closeExpiredOrder(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("#106-2 closeExpiredOrder 订单不存在返回 false")
        void shouldReturnFalseWhenOrderNotFoundForClose() {
            when(orderMainMapper.selectOne(any())).thenReturn(null);

            boolean result = orderService.closeExpiredOrder(999L);

            assertFalse(result);
        }

        @Test
        @DisplayName("#107 batchMarkTraveling 批量更新为出行中")
        void shouldBatchMarkTraveling() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(3);
            when(orderLogMapper.insert(any())).thenReturn(1);

            int result = orderService.batchMarkTraveling(List.of(1L, 2L, 3L));

            assertEquals(3, result);
            verify(orderLogMapper, times(3)).insert(any());
        }

        @Test
        @DisplayName("#108 batchMarkTraveling 传入空列表")
        void shouldReturnZeroForEmptyList() {
            assertEquals(0, orderService.batchMarkTraveling(Collections.emptyList()));
            assertEquals(0, orderService.batchMarkTraveling(null));
            verify(orderMainMapper, never()).update(any(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("#109 batchMarkCompleted 批量更新为已完成")
        void shouldBatchMarkCompleted() {
            when(orderMainMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(2);
            when(orderLogMapper.insert(any())).thenReturn(1);

            int result = orderService.batchMarkCompleted(List.of(1L, 2L));

            assertEquals(2, result);
            verify(orderLogMapper, times(2)).insert(any());
        }

        @Test
        @DisplayName("#109-2 batchMarkCompleted 传入空列表返回 0")
        void shouldReturnZeroForEmptyCompletedList() {
            assertEquals(0, orderService.batchMarkCompleted(Collections.emptyList()));
            assertEquals(0, orderService.batchMarkCompleted(null));
        }

        @Test
        @DisplayName("#116 getBookedOrdersToTravel 出行日期≤今天的已预订订单")
        void shouldReturnBookedOrdersToTravel() {
            OrderMain order = buildOrder(1L, 1L, OrderStatus.BOOKED.getCode());
            order.setStartDate(LocalDate.now());
            when(orderMainMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(order));

            List<OrderMain> result = orderService.getBookedOrdersToTravel(LocalDate.now());

            assertEquals(1, result.size());
        }
    }

    // ==================== addLog #110-111 ====================

    @Nested
    @DisplayName("addLog - 订单日志")
    class AddLog {

        @Test
        @DisplayName("#110 正常写入日志")
        void shouldInsertLog() {
            when(orderLogMapper.insert(any())).thenReturn(1);

            orderService.addLog(1L, "XJ001", 0, 1, "user", 1L, "备注");

            ArgumentCaptor<OrderLog> cap = ArgumentCaptor.forClass(OrderLog.class);
            verify(orderLogMapper).insert(cap.capture());
            OrderLog log = cap.getValue();
            assertEquals(1L, log.getOrderId());
            assertEquals("XJ001", log.getOrderNo());
            assertEquals(0, log.getFromStatus());
            assertEquals(1, log.getToStatus());
            assertEquals("user", log.getOperatorType());
        }

        @Test
        @DisplayName("#111 日志写入失败不影响主流程（不抛异常）")
        void shouldNotThrowWhenLogFails() {
            when(orderLogMapper.insert(any())).thenThrow(new RuntimeException("DB error"));

            assertDoesNotThrow(() ->
                    orderService.addLog(1L, "XJ001", 0, 1, "user", 1L, "备注"));
        }
    }

    // ==================== countByStatus ====================

    @Nested
    @DisplayName("countByStatus - 按状态统计")
    class CountByStatus {

        @Test
        @DisplayName("按状态正确统计订单数量")
        void shouldCountCorrectly() {
            OrderMain o1 = new OrderMain();
            o1.setStatus(0);
            OrderMain o2 = new OrderMain();
            o2.setStatus(0);
            OrderMain o3 = new OrderMain();
            o3.setStatus(1);

            when(orderMainMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(o1, o2, o3));

            Map<Integer, Long> counts = orderService.countByStatus(1L);

            assertEquals(2L, counts.get(0));
            assertEquals(1L, counts.get(1));
        }
    }
}
