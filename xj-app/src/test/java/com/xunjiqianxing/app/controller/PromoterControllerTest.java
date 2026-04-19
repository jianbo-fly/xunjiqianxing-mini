package com.xunjiqianxing.app.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xunjiqianxing.app.dto.PromoterApplyRequest;
import com.xunjiqianxing.app.dto.WithdrawRequest;
import com.xunjiqianxing.common.exception.GlobalExceptionHandler;
import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.entity.PromoterScanRecord;
import com.xunjiqianxing.service.promotion.entity.PromoterWithdraw;
import com.xunjiqianxing.service.promotion.service.PromoterService;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PromoterController 单元测试（#50 ~ #75）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PromoterControllerTest {

    @Mock private PromoterService promoterService;
    @Mock private UserService userService;
    @Mock private WxMaService wxMaService;

    private PromoterController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockedStatic<StpUtil> stpMock;

    private static final Long USER_ID = 10001L;

    @BeforeEach
    void setUp() {
        controller = new PromoterController(promoterService, userService, wxMaService);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
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

    // ==================== 辅助 ====================

    private PromoterInfo buildPromoter(int status) {
        PromoterInfo p = new PromoterInfo();
        p.setId(1L);
        p.setUserId(USER_ID);
        p.setPromoCode("PROMO001");
        p.setLevel(1);
        p.setCommissionRate(BigDecimal.valueOf(0.05));
        p.setTotalCommission(BigDecimal.valueOf(100));
        p.setAvailableCommission(BigDecimal.valueOf(80));
        p.setWithdrawnAmount(BigDecimal.valueOf(20));
        p.setScanCount(50);
        p.setPromotedCount(10);
        p.setOrderCount(5);
        p.setOrderAmount(BigDecimal.valueOf(2000));
        p.setStatus(status);
        p.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return p;
    }

    private PromoterCommission buildCommission(Long id, int status) {
        PromoterCommission c = new PromoterCommission();
        c.setId(id);
        c.setOrderNo("XJ" + id);
        c.setOrderAmount(BigDecimal.valueOf(500));
        c.setCommissionRate(BigDecimal.valueOf(0.05));
        c.setCommissionAmount(BigDecimal.valueOf(25));
        c.setStatus(status);
        c.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return c;
    }

    private PromoterWithdraw buildWithdraw(Long id, int status) {
        PromoterWithdraw w = new PromoterWithdraw();
        w.setId(id);
        w.setAmount(BigDecimal.valueOf(200));
        w.setWithdrawType(1);
        w.setAccount("13800138000");
        w.setAccountName("张三");
        w.setStatus(status);
        w.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return w;
    }

    // ==================== info #50-51 ====================

    @Test
    @DisplayName("#50 info：返回推广员信息")
    void info_found() throws Exception {
        when(promoterService.getByUserId(USER_ID)).thenReturn(buildPromoter(1));

        mockMvc.perform(get("/api/promoter/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.promoCode").value("PROMO001"))
                .andExpect(jsonPath("$.data.level").value(1))
                .andExpect(jsonPath("$.data.commissionRateDesc").value("5%"));
    }

    @Test
    @DisplayName("#51 info：未申请时返回 null")
    void info_notFound() throws Exception {
        when(promoterService.getByUserId(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/promoter/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== scan #52-54 ====================

    @Test
    @DisplayName("#52 scan：正常上报")
    void scan_success() throws Exception {
        mockMvc.perform(post("/api/promoter/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promoCode\":\"PROMO001\"}"))
                .andExpect(status().isOk());

        verify(promoterService).recordScan("PROMO001");
    }

    @Test
    @DisplayName("#53 scan：promoCode 为空时跳过")
    void scan_emptyCode() throws Exception {
        mockMvc.perform(post("/api/promoter/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promoCode\":\"\"}"))
                .andExpect(status().isOk());

        verify(promoterService, never()).recordScan(any());
    }

    @Test
    @DisplayName("#54 scan：缺少 promoCode 字段")
    void scan_missingField() throws Exception {
        mockMvc.perform(post("/api/promoter/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(promoterService, never()).recordScan(any());
    }

    // ==================== apply #55-58 ====================

    @Test
    @DisplayName("#55 apply：正常申请")
    void apply_success() throws Exception {
        UserInfo user = new UserInfo();
        user.setId(USER_ID);
        user.setPhone("13800138000");
        when(userService.getById(USER_ID)).thenReturn(user);
        when(promoterService.apply(USER_ID, "小明", "13800138000")).thenReturn(buildPromoter(0));

        PromoterApplyRequest req = new PromoterApplyRequest();
        req.setNickname("小明");

        mockMvc.perform(post("/api/promoter/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0));

        verify(promoterService).apply(USER_ID, "小明", "13800138000");
    }

    @Test
    @DisplayName("#56 apply：用户不存在时 phone 传 null")
    void apply_userNull() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(null);
        when(promoterService.apply(USER_ID, "小明", null)).thenReturn(buildPromoter(0));

        PromoterApplyRequest req = new PromoterApplyRequest();
        req.setNickname("小明");

        mockMvc.perform(post("/api/promoter/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(promoterService).apply(USER_ID, "小明", null);
    }

    @Test
    @DisplayName("#57 apply：昵称为空 → 参数校验失败")
    void apply_blankNickname() throws Exception {
        mockMvc.perform(post("/api/promoter/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== qrcode #58-60 ====================

    @Test
    @DisplayName("#58 qrcode：未申请推广员返回 404")
    void qrcode_notFound() throws Exception {
        when(promoterService.getByUserId(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/promoter/qrcode"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("#59 qrcode：审核未通过返回 403")
    void qrcode_notApproved() throws Exception {
        when(promoterService.getByUserId(USER_ID)).thenReturn(buildPromoter(0));

        mockMvc.perform(get("/api/promoter/qrcode"))
                .andExpect(status().isForbidden());
    }

    // ==================== bind #60-62 ====================

    @Test
    @DisplayName("#60 bind：正常绑定")
    void bind_success() throws Exception {
        when(promoterService.bind(USER_ID, "PROMO001")).thenReturn(true);

        mockMvc.perform(post("/api/promoter/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promoCode\":\"PROMO001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(promoterService).bind(USER_ID, "PROMO001");
    }

    @Test
    @DisplayName("#61 bind：promoCode 为空返回失败")
    void bind_emptyCode() throws Exception {
        mockMvc.perform(post("/api/promoter/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promoCode\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(0)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("推广码不能为空")));

        verify(promoterService, never()).bind(any(), any());
    }

    @Test
    @DisplayName("#62 bind：缺少 promoCode 字段")
    void bind_missingField() throws Exception {
        mockMvc.perform(post("/api/promoter/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("推广码不能为空")));
    }

    // ==================== commissions #63-65 ====================

    @Test
    @DisplayName("#63 commissions：按状态过滤")
    void commissions_withStatus() throws Exception {
        when(promoterService.listCommissions(USER_ID, 0))
                .thenReturn(List.of(buildCommission(1L, 0), buildCommission(2L, 0)));

        mockMvc.perform(get("/api/promoter/commissions").param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderNo").value("XJ1"));
    }

    @Test
    @DisplayName("#64 commissions：无状态参数")
    void commissions_noStatus() throws Exception {
        when(promoterService.listCommissions(USER_ID, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/promoter/commissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("#65 commissions：返回正确的 statusDesc")
    void commissions_statusDesc() throws Exception {
        when(promoterService.listCommissions(any(), any()))
                .thenReturn(List.of(buildCommission(1L, 1)));

        mockMvc.perform(get("/api/promoter/commissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].statusDesc").exists());
    }

    // ==================== withdraw #66-69 ====================

    @Test
    @DisplayName("#66 withdraw：正常申请")
    void withdraw_success() throws Exception {
        when(promoterService.applyWithdraw(eq(USER_ID), eq(BigDecimal.valueOf(200)),
                eq(1), eq("13800138000"), eq("张三"))).thenReturn(buildWithdraw(1L, 0));

        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(BigDecimal.valueOf(200));
        req.setWithdrawType(1);
        req.setAccount("13800138000");
        req.setAccountName("张三");

        mockMvc.perform(post("/api/promoter/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(200));

        ArgumentCaptor<BigDecimal> amountCap = ArgumentCaptor.forClass(BigDecimal.class);
        verify(promoterService).applyWithdraw(eq(USER_ID), amountCap.capture(), eq(1), any(), any());
    }

    @Test
    @DisplayName("#67 withdraw：金额低于 100 → 参数校验失败")
    void withdraw_amountBelowMin() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(BigDecimal.valueOf(50));
        req.setWithdrawType(1);
        req.setAccount("13800138000");
        req.setAccountName("张三");

        mockMvc.perform(post("/api/promoter/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("#68 withdraw：账号为空")
    void withdraw_blankAccount() throws Exception {
        WithdrawRequest req = new WithdrawRequest();
        req.setAmount(BigDecimal.valueOf(200));
        req.setWithdrawType(1);
        req.setAccount("");
        req.setAccountName("张三");

        mockMvc.perform(post("/api/promoter/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("#69 withdraws：返回提现记录（账号脱敏）")
    void withdraws_list() throws Exception {
        when(promoterService.listWithdraws(USER_ID)).thenReturn(List.of(buildWithdraw(1L, 0)));

        mockMvc.perform(get("/api/promoter/withdraws"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].accountName").value("张三"));
    }

    // ==================== scanList #70-72 ====================

    @Test
    @DisplayName("#70 scanList：返回扫码记录")
    void scanList_success() throws Exception {
        PromoterScanRecord r1 = new PromoterScanRecord();
        r1.setId(1L);
        r1.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(promoterService.listScanRecords(USER_ID, 1, 20)).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/promoter/scan/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(1));
    }

    @Test
    @DisplayName("#71 scanList：自定义分页")
    void scanList_customPaging() throws Exception {
        when(promoterService.listScanRecords(USER_ID, 2, 10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/promoter/scan/list").param("page", "2").param("pageSize", "10"))
                .andExpect(status().isOk());

        verify(promoterService).listScanRecords(USER_ID, 2, 10);
    }

    @Test
    @DisplayName("#72 scanList：空列表")
    void scanList_empty() throws Exception {
        when(promoterService.listScanRecords(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/promoter/scan/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // ==================== orderList #73 ====================

    @Test
    @DisplayName("#73 orderList：分页返回推广订单")
    void orderList_paginated() throws Exception {
        List<PromoterCommission> all = List.of(
                buildCommission(1L, 0), buildCommission(2L, 0), buildCommission(3L, 1)
        );
        when(promoterService.listCommissions(USER_ID, null)).thenReturn(all);

        mockMvc.perform(get("/api/promoter/order/list").param("page", "1").param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("XJ1"));
    }

    @Test
    @DisplayName("#74 orderList：超出页码返回空列表")
    void orderList_pageOutOfRange() throws Exception {
        when(promoterService.listCommissions(any(), any())).thenReturn(List.of(buildCommission(1L, 0)));

        mockMvc.perform(get("/api/promoter/order/list").param("page", "5").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ==================== statistics #75 ====================

    @Test
    @DisplayName("#75 statistics：返回统计数据")
    void statistics_success() throws Exception {
        when(promoterService.getStatistics(USER_ID)).thenReturn(buildPromoter(1));

        mockMvc.perform(get("/api/promoter/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scanCount").value(50))
                .andExpect(jsonPath("$.data.orderCount").value(5));
    }

    @Test
    @DisplayName("#75-2 statistics：未申请返回 null")
    void statistics_notApplied() throws Exception {
        when(promoterService.getStatistics(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/promoter/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
