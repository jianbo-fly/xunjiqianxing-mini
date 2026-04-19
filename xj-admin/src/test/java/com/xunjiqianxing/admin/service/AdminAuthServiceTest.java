package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.xunjiqianxing.admin.dto.AdminLoginRequest;
import com.xunjiqianxing.admin.dto.AdminLoginResponse;
import com.xunjiqianxing.admin.dto.AdminUserVO;
import com.xunjiqianxing.admin.entity.SystemAdmin;
import com.xunjiqianxing.admin.entity.SystemSupplier;
import com.xunjiqianxing.admin.mapper.SystemAdminMapper;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminAuthService 单元测试（#283 ~ #285）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminAuthServiceTest {

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Mock
    private SystemAdminMapper systemAdminMapper;
    @Mock
    private SystemSupplierMapper systemSupplierMapper;

    private MockedStatic<StpUtil> stpMock;

    @BeforeEach
    void setUp() {
        stpMock = Mockito.mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    private SystemAdmin buildAdmin(Long id, String username, String password, String role, int status) {
        SystemAdmin admin = new SystemAdmin();
        admin.setId(id);
        admin.setUsername(username);
        admin.setPassword(BCrypt.hashpw(password));
        admin.setNickname("管理员");
        admin.setRole(role);
        admin.setStatus(status);
        return admin;
    }

    private SystemSupplier buildSupplier(Long id, String username, String password, int status) {
        SystemSupplier supplier = new SystemSupplier();
        supplier.setId(id);
        supplier.setUsername(username);
        supplier.setPassword(BCrypt.hashpw(password));
        supplier.setName("测试供应商");
        supplier.setStatus(status);
        return supplier;
    }

    // #283 管理员登录鉴权
    @Test
    @DisplayName("#283 管理员登录成功 → token 校验通过")
    void testAdminLoginSuccess() {
        SystemAdmin admin = buildAdmin(1L, "admin", "admin123", "super_admin", 1);
        when(systemAdminMapper.selectOne(any())).thenReturn(admin);
        when(systemAdminMapper.updateById(any())).thenReturn(1);

        SaSession session = new SaSession("test-session");
        stpMock.when(() -> StpUtil.login(any())).thenAnswer(inv -> null);
        stpMock.when(StpUtil::getSession).thenReturn(session);
        stpMock.when(StpUtil::getTokenValue).thenReturn("test-token-123");

        AdminLoginRequest req = new AdminLoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        AdminLoginResponse resp = adminAuthService.login(req);

        assertNotNull(resp);
        assertEquals("test-token-123", resp.getToken());
        assertEquals("admin", resp.getRoleType());
        assertEquals("超级管理员", resp.getRoleName());
    }

    @Test
    @DisplayName("#283-2 供应商登录成功")
    void testSupplierLoginSuccess() {
        when(systemAdminMapper.selectOne(any())).thenReturn(null);

        SystemSupplier supplier = buildSupplier(100L, "supplier1", "pass123", 1);
        when(systemSupplierMapper.selectOne(any())).thenReturn(supplier);
        when(systemSupplierMapper.updateById(any())).thenReturn(1);

        SaSession session = new SaSession("test-session");
        stpMock.when(() -> StpUtil.login(any())).thenAnswer(inv -> null);
        stpMock.when(StpUtil::getSession).thenReturn(session);
        stpMock.when(StpUtil::getTokenValue).thenReturn("supplier-token");

        AdminLoginRequest req = new AdminLoginRequest();
        req.setUsername("supplier1");
        req.setPassword("pass123");

        AdminLoginResponse resp = adminAuthService.login(req);

        assertEquals("supplier", resp.getRoleType());
    }

    // #284 未登录访问 → 用户名或密码错误
    @Test
    @DisplayName("#284 用户名不存在 → BizException")
    void testLoginUserNotFound() {
        when(systemAdminMapper.selectOne(any())).thenReturn(null);
        when(systemSupplierMapper.selectOne(any())).thenReturn(null);

        AdminLoginRequest req = new AdminLoginRequest();
        req.setUsername("unknown");
        req.setPassword("pass");

        BizException ex = assertThrows(BizException.class, () -> adminAuthService.login(req));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("#284-2 密码错误 → BizException")
    void testLoginWrongPassword() {
        SystemAdmin admin = buildAdmin(1L, "admin", "correct", "admin", 1);
        when(systemAdminMapper.selectOne(any())).thenReturn(admin);

        AdminLoginRequest req = new AdminLoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong");

        assertThrows(BizException.class, () -> adminAuthService.login(req));
    }

    @Test
    @DisplayName("#284-3 账号已禁用 → BizException")
    void testLoginDisabledAccount() {
        SystemAdmin admin = buildAdmin(1L, "admin", "admin123", "admin", 0);
        when(systemAdminMapper.selectOne(any())).thenReturn(admin);

        AdminLoginRequest req = new AdminLoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");

        BizException ex = assertThrows(BizException.class, () -> adminAuthService.login(req));
        assertTrue(ex.getMessage().contains("禁用"));
    }

    // #285 供应商权限隔离：getCurrentUser 返回不同权限
    @Test
    @DisplayName("#285 供应商权限隔离 → 返回受限权限列表")
    void testSupplierPermissions() {
        SaSession session = new SaSession("test-session");
        session.set("roleType", "supplier");
        session.set("supplierId", 100L);
        stpMock.when(StpUtil::getSession).thenReturn(session);

        SystemSupplier supplier = buildSupplier(100L, "supplier1", "pass", 1);
        when(systemSupplierMapper.selectById(100L)).thenReturn(supplier);

        AdminUserVO vo = adminAuthService.getCurrentUser();

        assertEquals("supplier", vo.getRoleType());
        assertTrue(vo.getPermissions().contains("route:own:*"));
        assertFalse(vo.getPermissions().contains("route:*"));
    }

    @Test
    @DisplayName("#285-2 超级管理员拥有全部权限")
    void testSuperAdminPermissions() {
        SaSession session = new SaSession("test-session");
        session.set("roleType", "admin");
        session.set("userId", 1L);
        stpMock.when(StpUtil::getSession).thenReturn(session);

        SystemAdmin admin = buildAdmin(1L, "admin", "pass", "super_admin", 1);
        when(systemAdminMapper.selectById(1L)).thenReturn(admin);

        AdminUserVO vo = adminAuthService.getCurrentUser();

        assertEquals("admin", vo.getRoleType());
        assertTrue(vo.getPermissions().contains("route:*"));
        assertTrue(vo.getPermissions().contains("order:*"));
    }
}
