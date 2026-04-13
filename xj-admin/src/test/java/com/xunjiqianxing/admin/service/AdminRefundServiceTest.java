package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.order.RefundAuditRequest;
import com.xunjiqianxing.admin.dto.order.RefundQueryRequest;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.admin.dto.order.RefundListVO;
import com.xunjiqianxing.service.message.service.MessageService;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderLogMapper;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderRefundMapper;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.payment.service.PaymentService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * AdminRefundService 单元测试（#117 ~ #129）
 */
@ExtendWith(MockitoExtension.class)
class AdminRefundServiceTest {

    @InjectMocks
    private AdminRefundService adminRefundService;

    @Mock
    private OrderRefundMapper orderRefundMapper;
    @Mock
    private OrderMainMapper orderMainMapper;
    @Mock
    private UserInfoMapper userInfoMapper;
    @Mock
    private MessageService messageService;
    @Mock
    private OrderService orderService;
    @Mock
    private PaymentService paymentService;

    private MockedStatic<StpUtil> stpMock;

    @BeforeEach
    void setupAuth() {
        stpMock = Mockito.mockStatic(StpUtil.class);
        SaSession session = new SaSession("test-session");
        session.set("userId", 100L);
        stpMock.when(StpUtil::getSession).thenReturn(session);
        // paymentService 是 @Autowired(required=false)，Mockito @InjectMocks 不会自动注入
        ReflectionTestUtils.setField(adminRefundService, "paymentService", paymentService);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    // ==================== 辅助方法 ====================

    private OrderRefund buildRefund(Long id, Long orderId, int status) {
        OrderRefund r = new OrderRefund();
        r.setId(id);
        r.setOrderId(orderId);
        r.setOrderNo("XJ001");
        r.setUserId(1L);
        r.setRefundAmount(BigDecimal.valueOf(999));
        r.setRefundRatio(100);
        r.setStatus(status);
        r.setReason("不想去了");
        return r;
    }

    private OrderMain buildOrder(Long id, int status) {
        OrderMain o = new OrderMain();
        o.setId(id);
        o.setUserId(1L);
        o.setStatus(status);
        o.setOrderNo("XJ001");
        o.setProductName("测试路线");
        o.setPayAmount(BigDecimal.valueOf(999));
        return o;
    }

    // ==================== pageList #117-120 ====================

    @Test
    @DisplayName("#117 按退款编号精确查询")
    void shouldQueryByRefundNo() {
        Page<OrderRefund> page = new Page<>(1, 10);
        page.setRecords(List.of(buildRefund(1L, 1L, 0)));
        page.setTotal(1);
        when(orderRefundMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(userInfoMapper.selectById(anyLong())).thenReturn(null);
        when(orderMainMapper.selectById(anyLong())).thenReturn(null);

        RefundQueryRequest req = new RefundQueryRequest();
        req.setRefundNo("TK001");
        PageResult<RefundListVO> result = adminRefundService.pageList(req);

        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("#118 按订单编号查询")
    void shouldQueryByOrderNo() {
        Page<OrderRefund> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(orderRefundMapper.selectPage(any(Page.class), any())).thenReturn(page);

        RefundQueryRequest req = new RefundQueryRequest();
        req.setOrderNo("XJ001");
        PageResult<RefundListVO> result = adminRefundService.pageList(req);

        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("#120 无退款记录返回空列表")
    void shouldReturnEmptyList() {
        Page<OrderRefund> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(orderRefundMapper.selectPage(any(Page.class), any())).thenReturn(page);

        RefundQueryRequest req = new RefundQueryRequest();
        PageResult<RefundListVO> result = adminRefundService.pageList(req);

        assertTrue(result.getList().isEmpty());
    }

    // ==================== getDetail #121-122 ====================

    @Test
    @DisplayName("#121 返回完整退款 VO")
    void shouldReturnRefundDetail() {
        OrderRefund refund = buildRefund(1L, 1L, 0);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);
        UserInfo user = new UserInfo();
        user.setNickname("张三");
        when(userInfoMapper.selectById(1L)).thenReturn(user);
        OrderMain order = buildOrder(1L, OrderStatus.REFUND_APPLY.getCode());
        when(orderMainMapper.selectById(1L)).thenReturn(order);

        RefundListVO vo = adminRefundService.getDetail(1L);

        assertNotNull(vo);
        assertEquals("张三", vo.getUserNickname());
        assertEquals("测试路线", vo.getProductName());
        assertEquals("待审核", vo.getStatusDesc());
    }

    @Test
    @DisplayName("#122 退款记录不存在")
    void shouldThrowWhenRefundNotFound() {
        when(orderRefundMapper.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> adminRefundService.getDetail(999L));
        assertTrue(ex.getMessage().contains("退款记录不存在"));
    }

    // ==================== audit #123-129 ====================

    @Test
    @DisplayName("#123 审核通过：订单状态变为已退款")
    void shouldApproveAndUpdateOrderStatus() {
        OrderRefund refund = buildRefund(1L, 1L, 0);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);
        OrderMain order = buildOrder(1L, OrderStatus.REFUND_APPLY.getCode());
        when(orderMainMapper.selectById(1L)).thenReturn(order);
        when(orderMainMapper.updateById(any())).thenReturn(1);
        when(orderRefundMapper.updateById(any())).thenReturn(1);

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(1L);
        req.setStatus(1);

        adminRefundService.audit(req);

        assertEquals(OrderStatus.REFUNDED.getCode(), order.getStatus());
        assertEquals(1, refund.getStatus());
        verify(orderMainMapper).updateById(order);
    }

    @Test
    @DisplayName("#124 审核通过：调用支付服务退款")
    void shouldCallPaymentServiceOnApproval() {
        OrderRefund refund = buildRefund(1L, 1L, 0);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);
        OrderMain order = buildOrder(1L, OrderStatus.REFUND_APPLY.getCode());
        when(orderMainMapper.selectById(1L)).thenReturn(order);
        when(orderMainMapper.updateById(any())).thenReturn(1);
        when(orderRefundMapper.updateById(any())).thenReturn(1);

        com.xunjiqianxing.service.payment.entity.PaymentRecord payRecord =
                new com.xunjiqianxing.service.payment.entity.PaymentRecord();
        payRecord.setPaymentNo("PAY001");
        when(paymentService.getByBiz("order", 1L)).thenReturn(payRecord);
        when(paymentService.refund(eq("PAY001"), any(), any())).thenReturn("REFUND001");

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(1L);
        req.setStatus(1);

        adminRefundService.audit(req);

        assertEquals("REFUND001", refund.getRefundTradeNo());
        verify(paymentService).refund(eq("PAY001"), any(), any());
    }

    @Test
    @DisplayName("#125 审核通过：发送退款成功消息")
    void shouldSendMessageOnApproval() {
        OrderRefund refund = buildRefund(1L, 1L, 0);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);
        OrderMain order = buildOrder(1L, OrderStatus.REFUND_APPLY.getCode());
        when(orderMainMapper.selectById(1L)).thenReturn(order);
        when(orderMainMapper.updateById(any())).thenReturn(1);
        when(orderRefundMapper.updateById(any())).thenReturn(1);

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(1L);
        req.setStatus(1);

        adminRefundService.audit(req);

        verify(messageService).sendMessage(
                eq(1L), eq("refund_success"), eq("order"),
                any(), any(), eq("order"), eq(1L), any(), any());
    }

    @Test
    @DisplayName("#126 审核驳回：订单状态恢复为已预订")
    void shouldRejectAndRestoreOrderStatus() {
        OrderRefund refund = buildRefund(1L, 1L, 0);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);
        OrderMain order = buildOrder(1L, OrderStatus.REFUND_APPLY.getCode());
        when(orderMainMapper.selectById(1L)).thenReturn(order);
        when(orderMainMapper.updateById(any())).thenReturn(1);
        when(orderRefundMapper.updateById(any())).thenReturn(1);

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(1L);
        req.setStatus(2);
        req.setAuditRemark("不符合退款条件");

        adminRefundService.audit(req);

        assertEquals(OrderStatus.BOOKED.getCode(), order.getStatus());
        assertEquals(2, refund.getStatus());
    }

    @Test
    @DisplayName("#127 审核驳回：未填写备注时抛异常")
    void shouldThrowWhenRejectWithoutRemark() {
        OrderRefund refund = buildRefund(1L, 1L, 0);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);
        OrderMain order = buildOrder(1L, OrderStatus.REFUND_APPLY.getCode());
        when(orderMainMapper.selectById(1L)).thenReturn(order);

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(1L);
        req.setStatus(2);
        req.setAuditRemark("");

        BizException ex = assertThrows(BizException.class,
                () -> adminRefundService.audit(req));
        assertTrue(ex.getMessage().contains("驳回时必须填写"));
    }

    @Test
    @DisplayName("#128 重复审核")
    void shouldThrowWhenAlreadyAudited() {
        OrderRefund refund = buildRefund(1L, 1L, 1);
        when(orderRefundMapper.selectById(1L)).thenReturn(refund);

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(1L);
        req.setStatus(1);

        BizException ex = assertThrows(BizException.class,
                () -> adminRefundService.audit(req));
        assertTrue(ex.getMessage().contains("已审核"));
    }

    @Test
    @DisplayName("#128-2 退款记录不存在时审核")
    void shouldThrowWhenRefundNotFoundForAudit() {
        when(orderRefundMapper.selectById(999L)).thenReturn(null);

        RefundAuditRequest req = new RefundAuditRequest();
        req.setId(999L);
        req.setStatus(1);

        BizException ex = assertThrows(BizException.class,
                () -> adminRefundService.audit(req));
        assertTrue(ex.getMessage().contains("退款记录不存在"));
    }
}
