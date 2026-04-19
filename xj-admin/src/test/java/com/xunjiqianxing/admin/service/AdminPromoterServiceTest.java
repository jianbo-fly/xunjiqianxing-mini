package com.xunjiqianxing.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.promotion.PromoterAuditRequest;
import com.xunjiqianxing.admin.dto.promotion.PromoterListVO;
import com.xunjiqianxing.admin.dto.promotion.PromoterQueryRequest;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.promotion.entity.PromoterBindLog;
import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.mapper.PromoterBindLogMapper;
import com.xunjiqianxing.service.promotion.mapper.PromoterCommissionMapper;
import com.xunjiqianxing.service.promotion.mapper.PromoterInfoMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminPromoterService 单元测试（#278 ~ #282, #296 ~ #297）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminPromoterServiceTest {

    @InjectMocks
    private AdminPromoterService adminPromoterService;

    @Mock
    private PromoterInfoMapper promoterInfoMapper;
    @Mock
    private PromoterBindLogMapper promoterBindLogMapper;
    @Mock
    private PromoterCommissionMapper promoterCommissionMapper;
    @Mock
    private UserInfoMapper userInfoMapper;

    private PromoterInfo buildPromoter(Long id, Long userId, int status) {
        PromoterInfo p = new PromoterInfo();
        p.setId(id);
        p.setUserId(userId);
        p.setPromoCode("ABC123");
        p.setRealName("张三");
        p.setPhone("13800138000");
        p.setLevel(1);
        p.setCommissionRate(new BigDecimal("0.05"));
        p.setStatus(status);
        p.setIsDeleted(0);
        p.setCreatedAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        return p;
    }

    // #278 审核通过推广员申请
    @Test
    @DisplayName("#278 审核通过推广员申请")
    void testAuditApprove() {
        PromoterInfo promoter = buildPromoter(1L, 100L, 0);
        when(promoterInfoMapper.selectById(1L)).thenReturn(promoter);
        when(promoterInfoMapper.updateById(any())).thenReturn(1);

        UserInfo user = new UserInfo();
        user.setId(100L);
        user.setIsPromoter(0);
        when(userInfoMapper.selectById(100L)).thenReturn(user);
        when(userInfoMapper.updateById(any())).thenReturn(1);

        PromoterAuditRequest req = new PromoterAuditRequest();
        req.setId(1L);
        req.setStatus(1);
        req.setCommissionRate(new BigDecimal("0.10"));
        req.setLevel(2);

        assertDoesNotThrow(() -> adminPromoterService.audit(req));
        verify(promoterInfoMapper).updateById(argThat(p -> {
            PromoterInfo pi = (PromoterInfo) p;
            return pi.getStatus() == 1
                    && pi.getCommissionRate().compareTo(new BigDecimal("0.10")) == 0
                    && pi.getLevel() == 2;
        }));
        verify(userInfoMapper).updateById(argThat(u -> ((UserInfo) u).getIsPromoter() == 1));
    }

    // #279 审核驳回推广员申请+备注
    @Test
    @DisplayName("#279 审核驳回推广员申请")
    void testAuditReject() {
        PromoterInfo promoter = buildPromoter(1L, 100L, 0);
        when(promoterInfoMapper.selectById(1L)).thenReturn(promoter);
        when(promoterInfoMapper.updateById(any())).thenReturn(1);

        PromoterAuditRequest req = new PromoterAuditRequest();
        req.setId(1L);
        req.setStatus(2);
        req.setRemark("资质不符");

        assertDoesNotThrow(() -> adminPromoterService.audit(req));
        verify(promoterInfoMapper).updateById(argThat(p -> {
            PromoterInfo pi = (PromoterInfo) p;
            return pi.getStatus() == 2 && "资质不符".equals(pi.getRemark());
        }));
        verify(userInfoMapper, never()).updateById(any());
    }

    // #278 补充：重复审核
    @Test
    @DisplayName("#278-2 重复审核 → BizException")
    void testAuditAlreadyAudited() {
        PromoterInfo promoter = buildPromoter(1L, 100L, 1);
        when(promoterInfoMapper.selectById(1L)).thenReturn(promoter);

        PromoterAuditRequest req = new PromoterAuditRequest();
        req.setId(1L);
        req.setStatus(1);

        assertThrows(BizException.class, () -> adminPromoterService.audit(req));
    }

    // #280 待审核数量统计
    @Test
    @DisplayName("#280 待审核数量统计")
    void testPendingApplyCount() {
        when(promoterInfoMapper.selectCount(any())).thenReturn(3L);

        Long count = adminPromoterService.getPendingApplyCount();

        assertEquals(3L, count);
    }

    // #281 推广员启用/停用
    @Test
    @DisplayName("#281 推广员启用/停用")
    void testUpdateStatus() {
        PromoterInfo promoter = buildPromoter(1L, 100L, 1);
        when(promoterInfoMapper.selectById(1L)).thenReturn(promoter);
        when(promoterInfoMapper.updateById(any())).thenReturn(1);

        UserInfo user = new UserInfo();
        user.setId(100L);
        user.setIsPromoter(1);
        when(userInfoMapper.selectById(100L)).thenReturn(user);
        when(userInfoMapper.updateById(any())).thenReturn(1);

        adminPromoterService.updateStatus(1L, 2);

        verify(promoterInfoMapper).updateById(argThat(p -> ((PromoterInfo) p).getStatus() == 2));
        verify(userInfoMapper).updateById(argThat(u -> ((UserInfo) u).getIsPromoter() == 0));
    }

    @Test
    @DisplayName("#281-2 停用后重新启用")
    void testUpdateStatusReEnable() {
        PromoterInfo promoter = buildPromoter(1L, 100L, 2);
        when(promoterInfoMapper.selectById(1L)).thenReturn(promoter);
        when(promoterInfoMapper.updateById(any())).thenReturn(1);

        UserInfo user = new UserInfo();
        user.setId(100L);
        user.setIsPromoter(0);
        when(userInfoMapper.selectById(100L)).thenReturn(user);
        when(userInfoMapper.updateById(any())).thenReturn(1);

        adminPromoterService.updateStatus(1L, 1);

        verify(userInfoMapper).updateById(argThat(u -> ((UserInfo) u).getIsPromoter() == 1));
    }

    // #296 推广员绑定日志查询
    @Test
    @DisplayName("#296 推广员绑定日志查询")
    void testGetBindLogs() {
        Page<PromoterBindLog> pageResult = new Page<>(1, 10);
        PromoterBindLog log = new PromoterBindLog();
        log.setId(1L);
        log.setPromoterId(1L);
        log.setUserId(200L);
        log.setCreatedAt(LocalDateTime.now());
        pageResult.setRecords(List.of(log));
        pageResult.setTotal(1);

        when(promoterBindLogMapper.selectPage(any(), any())).thenReturn(pageResult);
        when(userInfoMapper.selectById(200L)).thenReturn(null);

        PageResult<?> result = adminPromoterService.getBindLogs(1L, 1, 10);

        assertEquals(1, result.getList().size());
    }

    // #297 推广员佣金记录查询
    @Test
    @DisplayName("#297 推广员佣金记录查询")
    void testGetCommissions() {
        Page<PromoterCommission> pageResult = new Page<>(1, 10);
        PromoterCommission commission = new PromoterCommission();
        commission.setId(1L);
        commission.setPromoterId(1L);
        commission.setFromUserId(200L);
        commission.setCommissionAmount(BigDecimal.valueOf(50));
        commission.setStatus(0);
        commission.setCreatedAt(LocalDateTime.now());
        pageResult.setRecords(List.of(commission));
        pageResult.setTotal(1);

        when(promoterCommissionMapper.selectPage(any(), any())).thenReturn(pageResult);
        when(userInfoMapper.selectById(200L)).thenReturn(null);

        PageResult<?> result = adminPromoterService.getCommissions(1L, 1, 10);

        assertEquals(1, result.getList().size());
    }

    // 推广员不存在
    @Test
    @DisplayName("推广员不存在 → BizException")
    void testAuditNotFound() {
        when(promoterInfoMapper.selectById(999L)).thenReturn(null);

        PromoterAuditRequest req = new PromoterAuditRequest();
        req.setId(999L);
        req.setStatus(1);

        assertThrows(BizException.class, () -> adminPromoterService.audit(req));
    }

    // 申请列表分页
    @Test
    @DisplayName("申请列表分页查询")
    void testPageApplies() {
        Page<PromoterInfo> page = new Page<>(1, 10);
        page.setRecords(List.of(buildPromoter(1L, 100L, 0)));
        page.setTotal(1);

        when(promoterInfoMapper.selectPage(any(), any())).thenReturn(page);
        when(userInfoMapper.selectById(anyLong())).thenReturn(null);

        PromoterQueryRequest req = new PromoterQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        PageResult<PromoterListVO> result = adminPromoterService.pageApplies(req);

        assertEquals(1, result.getList().size());
    }
}
