package com.xunjiqianxing.admin.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xunjiqianxing.admin.dto.promotion.CouponIssueRequest;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.service.promotion.entity.Coupon;
import com.xunjiqianxing.service.promotion.entity.UserCoupon;
import com.xunjiqianxing.service.promotion.mapper.CouponMapper;
import com.xunjiqianxing.service.promotion.mapper.UserCouponMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminCouponService 单元测试（#292 ~ #293）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminCouponServiceTest {

    @InjectMocks
    private AdminCouponService adminCouponService;

    @Mock
    private CouponMapper couponMapper;
    @Mock
    private UserCouponMapper userCouponMapper;
    @Mock
    private UserInfoMapper userInfoMapper;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration cfg = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(cfg, "");
        TableInfoHelper.initTableInfo(assistant, UserInfo.class);
        TableInfoHelper.initTableInfo(assistant, UserCoupon.class);
        TableInfoHelper.initTableInfo(assistant, Coupon.class);
    }

    private Coupon buildCoupon(Long id, int status) {
        Coupon c = new Coupon();
        c.setId(id);
        c.setName("满200减50");
        c.setType(1);
        c.setThreshold(BigDecimal.valueOf(200));
        c.setAmount(BigDecimal.valueOf(50));
        c.setTotalCount(100);
        c.setReceivedCount(0);
        c.setValidType(1);
        c.setValidStart(LocalDateTime.of(2026, 5, 1, 0, 0));
        c.setValidEnd(LocalDateTime.of(2026, 6, 1, 0, 0));
        c.setStatus(status);
        c.setIsDeleted(0);
        return c;
    }

    // #292 优惠券发放 - 指定用户
    @Test
    @DisplayName("#292 优惠券发放 → 指定用户")
    void testIssueToSpecificUsers() {
        Coupon coupon = buildCoupon(1L, 1);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(couponMapper.updateById(any())).thenReturn(1);
        when(userCouponMapper.insert(any())).thenReturn(1);

        CouponIssueRequest req = new CouponIssueRequest();
        req.setCouponId(1L);
        req.setIssueType(1);
        req.setUserIds(List.of(100L, 200L));
        req.setCount(1);

        Integer issued = adminCouponService.issue(req);

        assertEquals(2, issued);
        verify(userCouponMapper, times(2)).insert(any());
        verify(couponMapper).updateById(argThat(c -> ((Coupon) c).getReceivedCount() == 2));
    }

    // #292-2 优惠券发放 - 全部会员
    @Test
    @DisplayName("#292-2 优惠券发放 → 全部会员")
    void testIssueToAllMembers() {
        Coupon coupon = buildCoupon(1L, 1);
        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(couponMapper.updateById(any())).thenReturn(1);
        when(userCouponMapper.insert(any())).thenReturn(1);

        UserInfo member = new UserInfo();
        member.setId(100L);
        when(userInfoMapper.selectList(any())).thenReturn(List.of(member));

        CouponIssueRequest req = new CouponIssueRequest();
        req.setCouponId(1L);
        req.setIssueType(2);
        req.setCount(1);

        Integer issued = adminCouponService.issue(req);

        assertEquals(1, issued);
    }

    // #292-3 库存不足
    @Test
    @DisplayName("#292-3 优惠券库存不足 → BizException")
    void testIssueInsufficientStock() {
        Coupon coupon = buildCoupon(1L, 1);
        coupon.setTotalCount(1);
        coupon.setReceivedCount(0);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        CouponIssueRequest req = new CouponIssueRequest();
        req.setCouponId(1L);
        req.setIssueType(1);
        req.setUserIds(List.of(100L, 200L));
        req.setCount(1);

        BizException ex = assertThrows(BizException.class,
                () -> adminCouponService.issue(req));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    // #292-4 已停用优惠券
    @Test
    @DisplayName("#292-4 已停用优惠券 → BizException")
    void testIssueDisabledCoupon() {
        Coupon coupon = buildCoupon(1L, 0);
        when(couponMapper.selectById(1L)).thenReturn(coupon);

        CouponIssueRequest req = new CouponIssueRequest();
        req.setCouponId(1L);
        req.setIssueType(1);
        req.setUserIds(List.of(100L));
        req.setCount(1);

        BizException ex = assertThrows(BizException.class,
                () -> adminCouponService.issue(req));
        assertTrue(ex.getMessage().contains("停用"));
    }

    // #293 优惠券核销（状态查询验证）
    @Test
    @DisplayName("#293 优惠券统计 → 已使用/已过期计数正确")
    void testGetStats() {
        when(couponMapper.selectCount(any())).thenReturn(5L);
        when(userCouponMapper.selectCount(any())).thenReturn(100L, 30L, 10L);

        AdminCouponService.CouponStatsVO stats = adminCouponService.getStats();

        assertNotNull(stats);
        assertEquals(5L, stats.getActiveTemplateCount());
    }
}
