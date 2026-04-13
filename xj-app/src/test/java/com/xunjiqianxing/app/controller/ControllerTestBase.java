package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller 层测试基类
 * 启动完整 Spring 上下文 + MockMvc，使用 test profile
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected static final Long TEST_USER_ID = 10001L;

    /**
     * 模拟已登录用户，返回 MockedStatic 需在测试方法中 try-with-resources 关闭
     */
    protected MockedStatic<StpUtil> mockLogin() {
        return mockLogin(TEST_USER_ID);
    }

    protected MockedStatic<StpUtil> mockLogin(Long userId) {
        MockedStatic<StpUtil> stpMock = Mockito.mockStatic(StpUtil.class);
        stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);
        stpMock.when(StpUtil::getLoginIdAsString).thenReturn(String.valueOf(userId));
        stpMock.when(StpUtil::getTokenValue).thenReturn("test-token-" + userId);
        stpMock.when(StpUtil::checkLogin).then(invocation -> null);
        return stpMock;
    }
}
