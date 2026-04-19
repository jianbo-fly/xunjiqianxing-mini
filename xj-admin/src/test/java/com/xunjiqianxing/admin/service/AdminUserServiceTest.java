package com.xunjiqianxing.admin.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.user.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.entity.UserTraveler;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import com.xunjiqianxing.service.user.mapper.UserTravelerMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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
 * AdminUserService 单元测试（#294）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminUserServiceTest {

    @InjectMocks
    private AdminUserService adminUserService;

    @Mock
    private UserInfoMapper userInfoMapper;
    @Mock
    private UserTravelerMapper userTravelerMapper;
    @Mock
    private OrderMainMapper orderMainMapper;

    @org.junit.jupiter.api.BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration cfg = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(cfg, "");
        TableInfoHelper.initTableInfo(assistant, UserInfo.class);
        TableInfoHelper.initTableInfo(assistant, OrderMain.class);
        TableInfoHelper.initTableInfo(assistant, UserTraveler.class);
    }

    private UserInfo buildUser(Long id) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setNickname("张三");
        u.setPhone("13800138000");
        u.setGender(1);
        u.setIsMember(1);
        u.setIsLeader(0);
        u.setIsPromoter(0);
        u.setPoints(100);
        u.setStatus(1);
        u.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return u;
    }

    // #294 用户列表
    @Test
    @DisplayName("#294 用户列表分页查询")
    void testPageList() {
        Page<UserInfo> page = new Page<>(1, 10);
        page.setRecords(List.of(buildUser(1L)));
        page.setTotal(1);

        when(userInfoMapper.selectPage(any(), any())).thenReturn(page);
        when(orderMainMapper.selectCount(any())).thenReturn(3L);

        UserQueryRequest req = new UserQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        PageResult<UserListVO> result = adminUserService.pageList(req);

        assertEquals(1, result.getList().size());
        assertEquals("张三", result.getList().get(0).getNickname());
    }

    // #294 用户详情
    @Test
    @DisplayName("#294-2 用户详情")
    void testGetDetail() {
        UserInfo user = buildUser(1L);
        when(userInfoMapper.selectById(1L)).thenReturn(user);
        when(orderMainMapper.selectCount(any())).thenReturn(5L);

        OrderMain order = new OrderMain();
        order.setPayAmount(BigDecimal.valueOf(999));
        when(orderMainMapper.selectList(any())).thenReturn(List.of(order));
        when(userTravelerMapper.selectCount(any())).thenReturn(2L);

        UserDetailVO detail = adminUserService.getDetail(1L);

        assertNotNull(detail);
        assertEquals("张三", detail.getNickname());
        assertEquals(5, detail.getOrderCount());
        assertEquals(2, detail.getTravelerCount());
    }

    @Test
    @DisplayName("#294-3 用户不存在")
    void testGetDetailNotFound() {
        when(userInfoMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> adminUserService.getDetail(999L));
    }

    @Test
    @DisplayName("#294-4 禁用/启用用户")
    void testUpdateStatus() {
        UserInfo user = buildUser(1L);
        when(userInfoMapper.selectById(1L)).thenReturn(user);
        when(userInfoMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> adminUserService.updateStatus(1L, 0));
        verify(userInfoMapper).updateById(argThat(u -> ((UserInfo) u).getStatus() == 0));
    }

    @Test
    @DisplayName("#294-5 调整积分（正常）")
    void testAdjustPointsSuccess() {
        UserInfo user = buildUser(1L);
        user.setPoints(100);
        when(userInfoMapper.selectById(1L)).thenReturn(user);
        when(userInfoMapper.updateById(any())).thenReturn(1);

        PointsAdjustRequest req = new PointsAdjustRequest();
        req.setUserId(1L);
        req.setAmount(50);
        req.setReason("活动奖励");

        assertDoesNotThrow(() -> adminUserService.adjustPoints(req));
        verify(userInfoMapper).updateById(argThat(u -> ((UserInfo) u).getPoints() == 150));
    }

    @Test
    @DisplayName("#294-6 调整积分（不足）→ BizException")
    void testAdjustPointsInsufficient() {
        UserInfo user = buildUser(1L);
        user.setPoints(30);
        when(userInfoMapper.selectById(1L)).thenReturn(user);

        PointsAdjustRequest req = new PointsAdjustRequest();
        req.setUserId(1L);
        req.setAmount(-50);
        req.setReason("扣减");

        BizException ex = assertThrows(BizException.class, () -> adminUserService.adjustPoints(req));
        assertTrue(ex.getMessage().contains("积分不足"));
    }
}
