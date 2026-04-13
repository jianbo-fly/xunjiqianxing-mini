package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xunjiqianxing.app.dto.CreateOrderRequest;
import com.xunjiqianxing.common.exception.GlobalExceptionHandler;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdCardEncryptor;
import com.xunjiqianxing.service.message.service.MessageService;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.service.OrderService;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductRoute;
import com.xunjiqianxing.service.product.entity.ProductSku;
import com.xunjiqianxing.service.product.service.ProductService;
import com.xunjiqianxing.service.product.service.RouteService;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController 单元测试（#1 ~ #32）
 * 使用 standalone MockMvc + Mockito，不启动 Spring 容器
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private RouteService routeService;
    @Mock private ProductService productService;
    @Mock private UserService userService;
    @Mock private MessageService messageService;

    // IdCardEncryptor 是 final 签名不方便，Java 25 + Mockito inline 无法 mock 具体类 —
    // 直接用真实实例，通过反射注入 rawKey
    private IdCardEncryptor idCardEncryptor;

    private OrderController orderController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockedStatic<StpUtil> stpMock;
    private static final Long USER_ID = 10001L;

    @BeforeEach
    void setUp() {
        idCardEncryptor = new IdCardEncryptor();
        org.springframework.test.util.ReflectionTestUtils.setField(
                idCardEncryptor, "rawKey", "xjqx_test_key_16");

        orderController = new OrderController(
                orderService, routeService, productService,
                userService, messageService, idCardEncryptor);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setMessageConverters(converter)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        stpMock = Mockito.mockStatic(StpUtil.class);
        stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    // ==================== 辅助构造方法 ====================

    private CreateOrderRequest buildRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setSkuId(10L);
        req.setStartDate(LocalDate.of(2026, 5, 1));
        req.setAdultCount(2);
        req.setChildCount(1);
        req.setContactName("张三");
        req.setContactPhone("13800138000");
        CreateOrderRequest.TravelerDTO t1 = new CreateOrderRequest.TravelerDTO();
        t1.setName("张三");
        t1.setIdCard("110101199001011234");
        t1.setPhone("13800138000");
        t1.setTravelerType(1);
        req.setTravelers(List.of(t1));
        return req;
    }

    private ProductSku buildSku() {
        ProductSku sku = new ProductSku();
        sku.setId(10L);
        sku.setProductId(100L);
        sku.setName("标准套餐");
        return sku;
    }

    private ProductMain buildProduct() {
        ProductMain p = new ProductMain();
        p.setId(100L);
        p.setSupplierId(1L);
        p.setName("丽江三日游");
        p.setCoverImage("cover.jpg");
        p.setStatus(1);
        return p;
    }

    private ProductPriceStock buildPriceStock() {
        ProductPriceStock ps = new ProductPriceStock();
        ps.setPrice(BigDecimal.valueOf(299));
        ps.setChildPrice(BigDecimal.valueOf(199));
        ps.setStatus(1);
        return ps;
    }

    private ProductRoute buildRouteExt() {
        ProductRoute r = new ProductRoute();
        r.setDays(3);
        return r;
    }

    private OrderMain buildOrder() {
        OrderMain o = new OrderMain();
        o.setId(1000L);
        o.setOrderNo("XJ20260501001");
        o.setUserId(USER_ID);
        o.setProductName("丽江三日游");
        o.setStatus(0);
        o.setAdultCount(2);
        o.setChildCount(1);
        o.setSkuId(10L);
        o.setStartDate(LocalDate.of(2026, 5, 1));
        return o;
    }

    private void stubCreateOrderHappyPath() {
        when(routeService.getPackageById(10L)).thenReturn(buildSku());
        when(productService.getById(100L)).thenReturn(buildProduct());
        when(routeService.getPriceStock(10L, LocalDate.of(2026, 5, 1))).thenReturn(buildPriceStock());
        when(routeService.lockStock(10L, LocalDate.of(2026, 5, 1), 3)).thenReturn(true);
        when(routeService.getRouteExtById(100L)).thenReturn(buildRouteExt());
        when(userService.getById(USER_ID)).thenReturn(new UserInfo());
        when(orderService.createOrder(any(), any())).thenReturn(buildOrder());
    }

    // ==================== createOrder #1-15 ====================

    @Test
    @DisplayName("#1 正常创建订单")
    void shouldCreateOrderSuccessfully() throws Exception {
        stubCreateOrderHappyPath();

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderNo").value("XJ20260501001"))
                .andExpect(jsonPath("$.data.productName").value("丽江三日游"));

        verify(orderService).createOrder(any(), any());
    }

    @Test
    @DisplayName("#2 SKU不存在")
    void shouldFailWhenSkuNotFound() throws Exception {
        when(routeService.getPackageById(10L)).thenReturn(null);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("套餐不存在")));
    }

    @Test
    @DisplayName("#3 商品不存在")
    void shouldFailWhenProductNotFound() throws Exception {
        when(routeService.getPackageById(10L)).thenReturn(buildSku());
        when(productService.getById(100L)).thenReturn(null);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("商品不存在或已下架")));
    }

    @Test
    @DisplayName("#4 商品已下架")
    void shouldFailWhenProductOffline() throws Exception {
        when(routeService.getPackageById(10L)).thenReturn(buildSku());
        ProductMain p = buildProduct();
        p.setStatus(0);
        when(productService.getById(100L)).thenReturn(p);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已下架")));
    }

    @Test
    @DisplayName("#5 价格库存不存在")
    void shouldFailWhenPriceStockNotFound() throws Exception {
        when(routeService.getPackageById(10L)).thenReturn(buildSku());
        when(productService.getById(100L)).thenReturn(buildProduct());
        when(routeService.getPriceStock(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("不可预订")));
    }

    @Test
    @DisplayName("#6 所选日期不可预订（status=0）")
    void shouldFailWhenPriceStockDisabled() throws Exception {
        when(routeService.getPackageById(10L)).thenReturn(buildSku());
        when(productService.getById(100L)).thenReturn(buildProduct());
        ProductPriceStock ps = buildPriceStock();
        ps.setStatus(0);
        when(routeService.getPriceStock(any(), any())).thenReturn(ps);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("不可预订")));
    }

    @Test
    @DisplayName("#7 库存锁定失败")
    void shouldFailWhenLockStockFails() throws Exception {
        when(routeService.getPackageById(10L)).thenReturn(buildSku());
        when(productService.getById(100L)).thenReturn(buildProduct());
        when(routeService.getPriceStock(any(), any())).thenReturn(buildPriceStock());
        when(routeService.lockStock(any(), any(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("库存不足")));

        verify(orderService, never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("#8 创建订单异常时释放库存")
    void shouldReleaseStockWhenCreateOrderThrows() throws Exception {
        stubCreateOrderHappyPath();
        when(orderService.createOrder(any(), any())).thenThrow(new RuntimeException("DB错误"));

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().is5xxServerError());

        verify(routeService).releaseStock(10L, LocalDate.of(2026, 5, 1), 3);
    }

    @Test
    @DisplayName("#9 儿童数为null时正常计算")
    void shouldHandleNullChildCount() throws Exception {
        stubCreateOrderHappyPath();
        CreateOrderRequest req = buildRequest();
        req.setChildCount(null);

        // 默认 lockStock(skuId, date, 3) — 但 totalCount=2，需要重新 stub
        when(routeService.lockStock(10L, LocalDate.of(2026, 5, 1), 2)).thenReturn(true);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderService).createOrder(orderCaptor.capture(), any());
        // 总价 = 299 * 2 = 598
        org.junit.jupiter.api.Assertions.assertEquals(0,
                orderCaptor.getValue().getTotalAmount().compareTo(BigDecimal.valueOf(598)));
        org.junit.jupiter.api.Assertions.assertEquals(0, orderCaptor.getValue().getChildCount());
    }

    @Test
    @DisplayName("#10 正确计算总价（成人+儿童）")
    void shouldCalculateTotalAmountCorrectly() throws Exception {
        stubCreateOrderHappyPath();

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderService).createOrder(orderCaptor.capture(), any());
        // 299*2 + 199*1 = 797
        org.junit.jupiter.api.Assertions.assertEquals(0,
                orderCaptor.getValue().getTotalAmount().compareTo(BigDecimal.valueOf(797)));
        org.junit.jupiter.api.Assertions.assertEquals(0,
                orderCaptor.getValue().getPayAmount().compareTo(BigDecimal.valueOf(797)));
    }

    @Test
    @DisplayName("#11 绑定推广员")
    void shouldBindPromoter() throws Exception {
        stubCreateOrderHappyPath();
        UserInfo user = new UserInfo();
        user.setBindpromoterId(888L);
        when(userService.getById(USER_ID)).thenReturn(user);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderService).createOrder(orderCaptor.capture(), any());
        org.junit.jupiter.api.Assertions.assertEquals(888L, orderCaptor.getValue().getPromoterId());
    }

    @Test
    @DisplayName("#12 行程天数与结束日期")
    void shouldComputeDaysAndEndDate() throws Exception {
        stubCreateOrderHappyPath();

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderService).createOrder(orderCaptor.capture(), any());
        OrderMain o = orderCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(3, o.getDays());
        org.junit.jupiter.api.Assertions.assertEquals(LocalDate.of(2026, 5, 3), o.getEndDate());
    }

    @Test
    @DisplayName("#13 RouteExt 为空时天数默认为1")
    void shouldDefaultDaysToOneWhenRouteExtNull() throws Exception {
        stubCreateOrderHappyPath();
        when(routeService.getRouteExtById(100L)).thenReturn(null);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        ArgumentCaptor<OrderMain> orderCaptor = ArgumentCaptor.forClass(OrderMain.class);
        verify(orderService).createOrder(orderCaptor.capture(), any());
        org.junit.jupiter.api.Assertions.assertEquals(1, orderCaptor.getValue().getDays());
    }

    @Test
    @DisplayName("#14 出行人身份证加密存储")
    void shouldEncryptTravelerIdCard() throws Exception {
        stubCreateOrderHappyPath();

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderTraveler>> travelerCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderService).createOrder(any(), travelerCaptor.capture());
        String stored = travelerCaptor.getValue().get(0).getIdCard();
        // 不应明文存储
        org.junit.jupiter.api.Assertions.assertNotEquals("110101199001011234", stored);
        // 解密后应还原
        org.junit.jupiter.api.Assertions.assertEquals(
                "110101199001011234", idCardEncryptor.decrypt(stored));
    }

    @Test
    @DisplayName("#15 发送下单确认消息")
    void shouldSendOrderConfirmMessage() throws Exception {
        stubCreateOrderHappyPath();

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());

        verify(messageService).sendMessage(
                eq(USER_ID), eq("order_confirm"), eq("order"),
                any(), any(), eq("order"), eq(1000L), any(), any());
    }

    @Test
    @DisplayName("#16 参数校验：缺少 skuId")
    void shouldRejectRequestMissingSkuId() throws Exception {
        CreateOrderRequest req = buildRequest();
        req.setSkuId(null);

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("#17 参数校验：联系人姓名为空")
    void shouldRejectRequestMissingContactName() throws Exception {
        CreateOrderRequest req = buildRequest();
        req.setContactName("");

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("#18 参数校验：出行人为空")
    void shouldRejectRequestEmptyTravelers() throws Exception {
        CreateOrderRequest req = buildRequest();
        req.setTravelers(Collections.emptyList());

        mockMvc.perform(post("/api/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==================== list #19-21 ====================

    @Test
    @DisplayName("#19 订单列表：带状态查询")
    void shouldQueryOrderListWithStatus() throws Exception {
        OrderMain o = buildOrder();
        PageResult<OrderMain> pr = PageResult.of(List.of(o), 1L, 1, 10);
        when(orderService.pageUserOrders(eq(USER_ID), eq(0), any())).thenReturn(pr);

        mockMvc.perform(get("/api/order/list").param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("XJ20260501001"));
    }

    @Test
    @DisplayName("#20 订单列表：不传状态")
    void shouldQueryOrderListWithoutStatus() throws Exception {
        when(orderService.pageUserOrders(eq(USER_ID), isNull(), any()))
                .thenReturn(PageResult.of(Collections.emptyList(), 0L, 1, 10));

        mockMvc.perform(get("/api/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("#21 订单列表：分页参数")
    void shouldPassPaginationParams() throws Exception {
        when(orderService.pageUserOrders(any(), any(), any()))
                .thenReturn(PageResult.of(Collections.emptyList(), 0L, 2, 20));

        mockMvc.perform(get("/api/order/list").param("page", "2").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    // ==================== detail #22-25 ====================

    @Test
    @DisplayName("#22 订单详情：正常返回")
    void shouldGetOrderDetail() throws Exception {
        OrderMain o = buildOrder();
        when(orderService.getById(1000L)).thenReturn(o);
        OrderTraveler t = new OrderTraveler();
        t.setName("张三");
        t.setIdCard(idCardEncryptor.encrypt("110101199001011234"));
        t.setPhone("13800138000");
        t.setTravelerType(1);
        when(orderService.getOrderTravelers(1000L)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/order/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("XJ20260501001"))
                .andExpect(jsonPath("$.data.travelers[0].name").value("张三"))
                .andExpect(jsonPath("$.data.travelers[0].idCard").value("1101**********1234"))
                .andExpect(jsonPath("$.data.travelers[0].phone").value("138****8000"));
    }

    @Test
    @DisplayName("#23 订单详情：订单不存在")
    void shouldFailDetailWhenOrderNotFound() throws Exception {
        when(orderService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/order/999"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("订单不存在")));
    }

    @Test
    @DisplayName("#24 订单详情：无权查看（用户ID不匹配）")
    void shouldFailDetailWhenUserMismatch() throws Exception {
        OrderMain o = buildOrder();
        o.setUserId(99999L);
        when(orderService.getById(1000L)).thenReturn(o);

        mockMvc.perform(get("/api/order/1000"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("无权")));
    }

    @Test
    @DisplayName("#25 订单详情：无出行人时也能正常返回")
    void shouldGetOrderDetailWithNoTravelers() throws Exception {
        when(orderService.getById(1000L)).thenReturn(buildOrder());
        when(orderService.getOrderTravelers(1000L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/order/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.travelers").isArray())
                .andExpect(jsonPath("$.data.travelers.length()").value(0));
    }

    // ==================== cancel #26-28 ====================

    @Test
    @DisplayName("#26 取消订单：正常取消并释放库存")
    void shouldCancelOrderAndReleaseStock() throws Exception {
        when(orderService.getById(1000L)).thenReturn(buildOrder());
        when(orderService.cancelOrder(eq(1000L), eq(USER_ID), any())).thenReturn(true);

        mockMvc.perform(post("/api/order/1000/cancel").param("reason", "不想去了"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(orderService).cancelOrder(1000L, USER_ID, "不想去了");
        verify(routeService).releaseStock(10L, LocalDate.of(2026, 5, 1), 3);
    }

    @Test
    @DisplayName("#27 取消订单：订单不存在")
    void shouldFailCancelWhenOrderNotFound() throws Exception {
        when(orderService.getById(999L)).thenReturn(null);

        mockMvc.perform(post("/api/order/999/cancel"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("订单不存在")));

        verify(orderService, never()).cancelOrder(anyLong(), any(), any());
    }

    @Test
    @DisplayName("#28 取消订单：释放库存失败不影响取消结果")
    void shouldStillSucceedWhenReleaseStockFails() throws Exception {
        when(orderService.getById(1000L)).thenReturn(buildOrder());
        when(orderService.cancelOrder(any(), any(), any())).thenReturn(true);
        doThrow(new RuntimeException("redis down"))
                .when(routeService).releaseStock(any(), any(), anyInt());

        mockMvc.perform(post("/api/order/1000/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== counts #29-30 ====================

    @Test
    @DisplayName("#29 订单数量：返回各状态计数")
    void shouldReturnOrderCounts() throws Exception {
        Map<Integer, Long> counts = new HashMap<>();
        counts.put(0, 3L);
        counts.put(1, 2L);
        counts.put(2, 1L);
        when(orderService.countByStatus(USER_ID)).thenReturn(counts);

        mockMvc.perform(get("/api/order/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pending").value(3))
                .andExpect(jsonPath("$.data.confirming").value(2))
                .andExpect(jsonPath("$.data.travelling").value(1));
    }

    @Test
    @DisplayName("#30 订单数量：无订单时返回0")
    void shouldReturnZeroCountsWhenNoOrders() throws Exception {
        when(orderService.countByStatus(USER_ID)).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/order/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pending").value(0))
                .andExpect(jsonPath("$.data.confirming").value(0))
                .andExpect(jsonPath("$.data.travelling").value(0));
    }

    // ==================== refund #31-32 ====================

    @Test
    @DisplayName("#31 申请退款：正常调用 service")
    void shouldApplyRefund() throws Exception {
        mockMvc.perform(post("/api/order/1000/refund").param("reason", "行程冲突"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(orderService).applyRefund(1000L, USER_ID, "行程冲突");
    }

    @Test
    @DisplayName("#32 申请退款：缺少 reason 参数")
    void shouldRejectRefundWithoutReason() throws Exception {
        mockMvc.perform(post("/api/order/1000/refund"))
                .andExpect(status().isBadRequest());
    }
}
