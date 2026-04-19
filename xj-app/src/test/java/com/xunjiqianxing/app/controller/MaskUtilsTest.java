package com.xunjiqianxing.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderController 脱敏方法单元测试（#112 ~ #115）
 */
class MaskUtilsTest {

    private OrderController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderController(null, null, null, null, null, null);
    }

    private String maskIdCard(String idCard) {
        return ReflectionTestUtils.invokeMethod(controller, "maskIdCard", idCard);
    }

    private String maskPhone(String phone) {
        return ReflectionTestUtils.invokeMethod(controller, "maskPhone", phone);
    }

    // #112 maskIdCard 正常脱敏
    @Test
    @DisplayName("#112 maskIdCard 正常 18 位身份证 → 前 4 后 4 可见")
    void testMaskIdCardNormal() {
        String result = maskIdCard("110101199001011234");
        assertEquals("1101**********1234", result);
        assertEquals(18, result.length());
    }

    @Test
    @DisplayName("#112-2 maskIdCard 15 位身份证")
    void testMaskIdCard15() {
        String result = maskIdCard("110101900101123");
        assertEquals("1101**********1123", result);
    }

    // #113 maskPhone 正常脱敏
    @Test
    @DisplayName("#113 maskPhone 正常 11 位手机号 → 前 3 后 4 可见")
    void testMaskPhoneNormal() {
        String result = maskPhone("13800138000");
        assertEquals("138****8000", result);
    }

    // #114 输入为 null
    @Test
    @DisplayName("#114 maskIdCard/maskPhone 输入为 null → 返回 null")
    void testMaskNull() {
        assertNull(maskIdCard(null));
        assertNull(maskPhone(null));
    }

    // #115 输入长度不足
    @Test
    @DisplayName("#115 maskIdCard 长度不足 → 返回原字符串")
    void testMaskIdCardShort() {
        assertEquals("1234567", maskIdCard("1234567"));
    }

    @Test
    @DisplayName("#115-2 maskPhone 长度不足 → 返回原字符串")
    void testMaskPhoneShort() {
        assertEquals("123456", maskPhone("123456"));
    }
}
