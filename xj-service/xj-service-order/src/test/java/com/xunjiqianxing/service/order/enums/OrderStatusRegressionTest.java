package com.xunjiqianxing.service.order.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 回归测试（#238 ~ #240）
 * 确保 OrderStatus 枚举唯一定义在 service-order 包中，且退款策略正确
 */
class OrderStatusRegressionTest {

    // #238 OrderStatus 存在于 service-order 包
    @Test
    @DisplayName("#238 OrderStatus 枚举存在于 service-order 包")
    void testOrderStatusExistsInServiceOrder() {
        assertNotNull(OrderStatus.PENDING_PAY);
        assertEquals("com.xunjiqianxing.service.order.enums",
                OrderStatus.class.getPackageName());
    }

    // #240 refundableStatuses() 只包含 BOOKED(1)
    @Test
    @DisplayName("#240 refundableStatuses() 只包含 BOOKED(1)")
    void testRefundableStatusesOnlyBooked() {
        List<Integer> refundable = OrderStatus.refundableStatuses();

        assertEquals(1, refundable.size());
        assertTrue(refundable.contains(OrderStatus.BOOKED.getCode()));
        assertFalse(refundable.contains(OrderStatus.TRAVELING.getCode()),
                "出行中(TRAVELING)不应在可退款列表中");
    }

    // 补充：OrderStatus 枚举完整性
    @Test
    @DisplayName("OrderStatus 包含所有预期状态")
    void testOrderStatusValues() {
        assertEquals(8, OrderStatus.values().length);
        assertEquals(0, OrderStatus.PENDING_PAY.getCode());
        assertEquals(1, OrderStatus.BOOKED.getCode());
        assertEquals(2, OrderStatus.TRAVELING.getCode());
        assertEquals(3, OrderStatus.COMPLETED.getCode());
        assertEquals(4, OrderStatus.CANCELLED.getCode());
        assertEquals(5, OrderStatus.REFUND_APPLY.getCode());
        assertEquals(6, OrderStatus.REFUNDED.getCode());
        assertEquals(7, OrderStatus.CLOSED.getCode());
    }

    @Test
    @DisplayName("getDesc 返回正确描述")
    void testGetDesc() {
        assertEquals("待支付", OrderStatus.getDesc(0));
        assertEquals("已预订", OrderStatus.getDesc(1));
        assertEquals("出行中", OrderStatus.getDesc(2));
        assertEquals("", OrderStatus.getDesc(99));
        assertEquals("", OrderStatus.getDesc(null));
    }
}
