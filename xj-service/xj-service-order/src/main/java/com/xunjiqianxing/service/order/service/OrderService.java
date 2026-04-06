package com.xunjiqianxing.service.order.service;

import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderTraveler;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 记录订单操作日志
     *
     * @param orderId      订单ID
     * @param orderNo      订单编号
     * @param fromStatus   变更前状态（创建时传 null）
     * @param toStatus     变更后状态
     * @param operatorType 操作者类型: user / admin / system
     * @param operatorId   操作者ID（system 传 null）
     * @param remark       备注
     */
    void addLog(Long orderId, String orderNo, Integer fromStatus, Integer toStatus,
                String operatorType, Long operatorId, String remark);

    /**
     * 查询超时未支付的订单（expireAt < now AND status = 0）
     */
    List<OrderMain> getExpiredPendingOrders(LocalDateTime now);

    /**
     * 关闭超时订单（系统自动）
     */
    boolean closeExpiredOrder(Long orderId);

    /**
     * 查询出行日已到但仍为「已预订」的订单（start_date <= today AND status = 1）
     */
    List<OrderMain> getBookedOrdersToTravel(LocalDate today);

    /**
     * 查询出行结束但仍为「出行中」的订单（end_date < today AND status = 2）
     */
    List<OrderMain> getTravelingOrdersToComplete(LocalDate today);

    /**
     * 批量将「已预订」改为「出行中」
     */
    int batchMarkTraveling(List<Long> orderIds);

    /**
     * 批量将「出行中」改为「已完成」
     */
    int batchMarkCompleted(List<Long> orderIds);
}
