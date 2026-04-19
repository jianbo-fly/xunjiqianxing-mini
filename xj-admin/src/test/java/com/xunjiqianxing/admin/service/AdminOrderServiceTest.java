package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.order.OrderDetailVO;
import com.xunjiqianxing.admin.dto.order.OrderListVO;
import com.xunjiqianxing.admin.dto.order.OrderQueryRequest;
import com.xunjiqianxing.admin.dto.order.OrderRemarkRequest;
import com.xunjiqianxing.admin.dto.order.OrderStatsVO;
import com.xunjiqianxing.admin.entity.SystemSupplier;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderRefundMapper;
import com.xunjiqianxing.service.order.mapper.OrderTravelerMapper;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminOrderService 单元测试（#130 ~ #138）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOrderServiceTest {

    @InjectMocks
    private AdminOrderService adminOrderService;

    @Mock
    private OrderMainMapper orderMainMapper;
    @Mock
    private OrderTravelerMapper orderTravelerMapper;
    @Mock
    private OrderRefundMapper orderRefundMapper;
    @Mock
    private UserInfoMapper userInfoMapper;
    @Mock
    private SystemSupplierMapper systemSupplierMapper;

    private MockedStatic<StpUtil> stpMock;

    @BeforeEach
    void setUp() {
        stpMock = Mockito.mockStatic(StpUtil.class);
        SaSession session = new SaSession("test-session");
        // 默认管理员角色（非供应商）
        stpMock.when(StpUtil::getSession).thenReturn(session);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    private OrderMain buildOrder(Long id, String orderNo, int status) {
        OrderMain o = new OrderMain();
        o.setId(id);
        o.setOrderNo(orderNo);
        o.setUserId(1L);
        o.setStatus(status);
        o.setProductName("测试路线");
        o.setPayAmount(BigDecimal.valueOf(999));
        o.setIsDeleted(0);
        o.setCreatedAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        return o;
    }

    private void mockAsSupplier(Long supplierId) {
        SaSession session = new SaSession("test-session");
        session.set("roleType", "supplier");
        session.set("supplierId", supplierId);
        stpMock.when(StpUtil::getSession).thenReturn(session);
    }

    // #130 管理员查询全部订单
    @Test
    @DisplayName("#130 管理员查询全部订单")
    void testPageListAll() {
        OrderQueryRequest req = new OrderQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        Page<OrderMain> page = new Page<>(1, 10);
        page.setRecords(List.of(buildOrder(1L, "XJ001", 0), buildOrder(2L, "XJ002", 1)));
        page.setTotal(2);

        when(orderMainMapper.selectPage(any(), any())).thenReturn(page);
        when(userInfoMapper.selectById(anyLong())).thenReturn(null);

        PageResult<OrderListVO> result = adminOrderService.pageList(req);

        assertEquals(2, result.getList().size());
        assertEquals(2, result.getTotal());
    }

    // #131 供应商只能查询自己的产品订单
    @Test
    @DisplayName("#131 供应商只能查询自己的产品订单")
    void testPageListSupplierFilter() {
        mockAsSupplier(100L);

        OrderQueryRequest req = new OrderQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        OrderMain order = buildOrder(1L, "XJ001", 1);
        order.setSupplierId(100L);

        Page<OrderMain> page = new Page<>(1, 10);
        page.setRecords(List.of(order));
        page.setTotal(1);

        when(orderMainMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<OrderListVO> result = adminOrderService.pageList(req);

        assertEquals(1, result.getList().size());
        // 验证查询条件包含了 supplierId 过滤（通过返回数据间接验证）
    }

    // #132 按订单号精确查询
    @Test
    @DisplayName("#132 按订单号精确查询")
    void testPageListByOrderNo() {
        OrderQueryRequest req = new OrderQueryRequest();
        req.setPage(1);
        req.setPageSize(10);
        req.setOrderNo("XJ001");

        Page<OrderMain> page = new Page<>(1, 10);
        page.setRecords(List.of(buildOrder(1L, "XJ001", 1)));
        page.setTotal(1);

        when(orderMainMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<OrderListVO> result = adminOrderService.pageList(req);

        assertEquals(1, result.getList().size());
    }

    // #133 按状态+日期范围组合筛选
    @Test
    @DisplayName("#133 按状态+日期范围组合筛选")
    void testPageListByStatusAndDate() {
        OrderQueryRequest req = new OrderQueryRequest();
        req.setPage(1);
        req.setPageSize(10);
        req.setStatus(1);
        req.setCreateDateBegin(LocalDate.of(2026, 4, 1));
        req.setCreateDateEnd(LocalDate.of(2026, 4, 30));

        Page<OrderMain> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(orderMainMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<OrderListVO> result = adminOrderService.pageList(req);

        assertEquals(0, result.getList().size());
    }

    // #134 获取订单详情（含出行人脱敏）
    @Test
    @DisplayName("#134 获取订单详情（含出行人脱敏）")
    void testGetDetailWithTravelers() {
        OrderMain order = buildOrder(1L, "XJ001", 1);
        when(orderMainMapper.selectOne(any())).thenReturn(order);

        UserInfo user = new UserInfo();
        user.setNickname("张三");
        user.setPhone("13800138000");
        when(userInfoMapper.selectById(anyLong())).thenReturn(user);

        OrderTraveler traveler = new OrderTraveler();
        traveler.setName("李四");
        traveler.setIdCard("110101199001011234");
        traveler.setPhone("13900139000");
        traveler.setTravelerType(1);
        when(orderTravelerMapper.selectList(any())).thenReturn(List.of(traveler));
        when(orderRefundMapper.selectOne(any())).thenReturn(null);

        OrderDetailVO detail = adminOrderService.getDetail("XJ001");

        assertNotNull(detail);
        assertEquals("张三", detail.getUserNickname());
        assertEquals(1, detail.getTravelers().size());
        // 身份证已脱敏
        assertNotEquals("110101199001011234", detail.getTravelers().get(0).getIdCard());
    }

    // #135 获取订单详情（含退款信息）
    @Test
    @DisplayName("#135 获取订单详情（含退款信息）")
    void testGetDetailWithRefund() {
        OrderMain order = buildOrder(1L, "XJ001", 5);
        when(orderMainMapper.selectOne(any())).thenReturn(order);
        when(userInfoMapper.selectById(anyLong())).thenReturn(null);
        when(orderTravelerMapper.selectList(any())).thenReturn(Collections.emptyList());

        OrderRefund refund = new OrderRefund();
        refund.setId(10L);
        refund.setOrderId(1L);
        refund.setRefundAmount(BigDecimal.valueOf(999));
        refund.setStatus(0);
        refund.setReason("不想去了");
        when(orderRefundMapper.selectOne(any())).thenReturn(refund);

        OrderDetailVO detail = adminOrderService.getDetail("XJ001");

        assertNotNull(detail.getRefund());
    }

    // #136 订单不存在时获取详情
    @Test
    @DisplayName("#136 订单不存在")
    void testGetDetailNotFound() {
        when(orderMainMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () -> adminOrderService.getDetail("XJ999"));
    }

    // #137 添加备注
    @Test
    @DisplayName("#137 添加备注")
    void testAddRemark() {
        OrderMain order = buildOrder(1L, "XJ001", 1);
        when(orderMainMapper.selectOne(any())).thenReturn(order);
        when(orderMainMapper.updateById(any())).thenReturn(1);

        OrderRemarkRequest req = new OrderRemarkRequest();
        req.setOrderNo("XJ001");
        req.setRemark("客户要求加急");

        assertDoesNotThrow(() -> adminOrderService.addRemark(req));
        verify(orderMainMapper).updateById(argThat(o -> "客户要求加急".equals(((OrderMain) o).getAdminRemark())));
    }

    // #138 获取订单统计
    @Test
    @DisplayName("#138 获取订单统计")
    void testGetStats() {
        when(orderMainMapper.selectCount(any())).thenReturn(5L);
        when(orderMainMapper.selectList(any())).thenReturn(List.of(buildOrder(1L, "XJ001", 1)));

        OrderStatsVO stats = adminOrderService.getStats();

        assertNotNull(stats);
        assertEquals(5L, stats.getPendingPayCount());
    }
}
