package com.xunjiqianxing.service.order.service;

import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderTraveler;

import java.util.List;

/**
 * 订单服务
 */
public interface OrderService {

    /**
     * 创建订单
     */
    OrderMain createOrder(OrderMain order, List<OrderTraveler> travelers);

    /**
     * 根据ID获取订单
     */
    OrderMain getById(Long id);

    /**
     * 根据订单编号获取订单
     */
    OrderMain getByOrderNo(String orderNo);

    /**
     * 分页查询用户订单
     */
    PageResult<OrderMain> pageUserOrders(Long userId, Integer status, PageQuery pageQuery);

    /**
     * 获取订单出行人列表
     */
    List<OrderTraveler> getOrderTravelers(Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, Long userId, String reason);

    /**
     * 支付成功回调
     */
    boolean paySuccess(String orderNo, String payTradeNo);

    /**
     * 更新订单状态
     */
    boolean updateStatus(Long orderId, Integer fromStatus, Integer toStatus);

    /**
     * 申请退款
     */
    boolean applyRefund(Long orderId, Long userId, String reason);
}
