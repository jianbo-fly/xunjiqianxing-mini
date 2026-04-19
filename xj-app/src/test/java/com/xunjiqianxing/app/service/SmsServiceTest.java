package com.xunjiqianxing.app.service;

import com.xunjiqianxing.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SmsService 单元测试（#276 ~ #277）
 * Java 25 不支持 Mockito inline mock 具体类，手动构造 + ReflectionTestUtils 注入
 */
class SmsServiceTest {

    private SmsService smsService;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        valueOperations = mock(ValueOperations.class);
        redisTemplate = mock(StringRedisTemplate.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

        // StringRedisTemplate 是具体类，无法 inline mock，
        // 但我们只需要 override hasKey / opsForValue / delete —— 使用 doReturn
        doReturn(valueOperations).when(redisTemplate).opsForValue();

        smsService = new SmsService(redisTemplate);
    }

    // #276 60 秒内重复发送 → 频率限制拦截
    @Test
    @DisplayName("#276 60 秒内重复发送 → BizException")
    void testSendCode_rateLimited() {
        doReturn(true).when(redisTemplate).hasKey(contains("sms:"));

        BizException ex = assertThrows(BizException.class,
                () -> smsService.sendVerifyCode("13800138000"));

        assertTrue(ex.getMessage().contains("频繁"));
    }

    // #277 超过 60 秒后重新发送 → 发送成功
    @Test
    @DisplayName("#277 超过 60 秒后重新发送 → 发送成功")
    void testSendCode_success() {
        doReturn(false).when(redisTemplate).hasKey(anyString());

        assertDoesNotThrow(() -> smsService.sendVerifyCode("13800138000"));

        verify(valueOperations).set(contains("sms:code:"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(valueOperations).set(contains("rate:limit:sms:"), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
    }

    // 验证码校验成功
    @Test
    @DisplayName("verifyCode 校验成功")
    void testVerifyCode_success() {
        when(valueOperations.get(contains("sms:code:"))).thenReturn("123456");
        doReturn(true).when(redisTemplate).delete(anyString());

        assertDoesNotThrow(() -> smsService.verifyCode("13800138000", "123456"));

        verify(redisTemplate).delete(contains("sms:code:"));
    }

    // 验证码已过期
    @Test
    @DisplayName("verifyCode 已过期")
    void testVerifyCode_expired() {
        when(valueOperations.get(anyString())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> smsService.verifyCode("13800138000", "123456"));

        assertTrue(ex.getMessage().contains("过期"));
    }

    // 验证码错误
    @Test
    @DisplayName("verifyCode 错误")
    void testVerifyCode_wrong() {
        when(valueOperations.get(anyString())).thenReturn("654321");

        BizException ex = assertThrows(BizException.class,
                () -> smsService.verifyCode("13800138000", "123456"));

        assertTrue(ex.getMessage().contains("错误"));
    }
}
