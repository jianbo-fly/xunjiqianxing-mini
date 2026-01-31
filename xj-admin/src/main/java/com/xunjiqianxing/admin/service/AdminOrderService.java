package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.DesensitizedUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.order.*;
import com.xunjiqianxing.admin.entity.SystemSupplier;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.entity.OrderTraveler;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderRefundMapper;
import com.xunjiqianxing.service.order.mapper.OrderTravelerMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 订单管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderMainMapper orderMainMapper;
    private final OrderTravelerMapper orderTravelerMapper;
    private final OrderRefundMapper orderRefundMapper;
    private final UserInfoMapper userInfoMapper;
    private final SystemSupplierMapper systemSupplierMapper;

    /**
     * 分页查询订单列表
     */
    public PageResult<OrderListVO> pageList(OrderQueryRequest request) {
        Page<OrderMain> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getIsDeleted, 0);

        // 供应商只能查看自己的订单
        Long supplierId = getCurrentSupplierId();
        if (supplierId != null) {
            wrapper.eq(OrderMain::getSupplierId, supplierId);
        } else if (request.getSupplierId() != null) {
            wrapper.eq(OrderMain::getSupplierId, request.getSupplierId());
        }

        // 订单编号
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.eq(OrderMain::getOrderNo, request.getOrderNo());
        }

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(OrderMain::getUserId, request.getUserId());
        }

        // 商品ID
        if (request.getProductId() != null) {
            wrapper.eq(OrderMain::getProductId, request.getProductId());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(OrderMain::getStatus, request.getStatus());
        }

        // 联系人
        if (StringUtils.hasText(request.getContactName())) {
            wrapper.like(OrderMain::getContactName, request.getContactName());
        }

        // 联系电话
        if (StringUtils.hasText(request.getContactPhone())) {
            wrapper.like(OrderMain::getContactPhone, request.getContactPhone());
        }

        // 出发日期
        if (request.getStartDateBegin() != null) {
            wrapper.ge(OrderMain::getStartDate, request.getStartDateBegin());
        }
        if (request.getStartDateEnd() != null) {
            wrapper.le(OrderMain::getStartDate, request.getStartDateEnd());
        }

        // 下单时间
        if (request.getCreateDateBegin() != null) {
            wrapper.ge(OrderMain::getCreatedAt, request.getCreateDateBegin().atStartOfDay());
        }
        if (request.getCreateDateEnd() != null) {
            wrapper.le(OrderMain::getCreatedAt, request.getCreateDateEnd().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(OrderMain::getCreatedAt);

        Page<OrderMain> result = orderMainMapper.selectPage(page, wrapper);

        List<OrderListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取订单详情
     */
    public OrderDetailVO getDetail(String orderNo) {
        OrderMain order = getOrderByNo(orderNo);

        // 检查权限
        checkOrderPermission(order);

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtil.copyProperties(order, vo);

        // 状态描述
        vo.setStatusDesc(OrderStatus.getDesc(order.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserPhone(user.getPhone());
        }

        // 供应商信息
        if (order.getSupplierId() != null) {
            SystemSupplier supplier = systemSupplierMapper.selectById(order.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            }
        }

        // 出行人信息
        List<OrderTraveler> travelers = orderTravelerMapper.selectList(
                new LambdaQueryWrapper<OrderTraveler>()
                        .eq(OrderTraveler::getOrderId, order.getId())
        );
        vo.setTravelers(travelers.stream()
                .map(this::convertToTravelerVO)
                .collect(Collectors.toList()));

        // 退款信息
        OrderRefund refund = orderRefundMapper.selectOne(
                new LambdaQueryWrapper<OrderRefund>()
                        .eq(OrderRefund::getOrderId, order.getId())
                        .orderByDesc(OrderRefund::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (refund != null) {
            vo.setRefund(convertToRefundVO(refund));
        }

        return vo;
    }

    /**
     * 确认/驳回订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrReject(OrderConfirmRequest request) {
        OrderMain order = getOrderByNo(request.getOrderNo());

        // 检查权限
        checkOrderPermission(order);

        // 只有待确认状态可以操作
        if (order.getStatus() != OrderStatus.PENDING_CONFIRM.getCode()) {
            throw new BizException("当前订单状态不允许此操作");
        }

        if ("confirm".equals(request.getAction())) {
            // 确认订单
            order.setStatus(OrderStatus.CONFIRMED.getCode());
            order.setConfirmTime(LocalDateTime.now());
            log.info("订单已确认: orderNo={}", request.getOrderNo());
        } else if ("reject".equals(request.getAction())) {
            // 驳回订单
            if (!StringUtils.hasText(request.getRejectReason())) {
                throw new BizException("请填写驳回原因");
            }
            order.setStatus(OrderStatus.CANCELLED.getCode());
            order.setRejectReason(request.getRejectReason());
            order.setCancelTime(LocalDateTime.now());
            order.setCancelType(2); // 商家驳回
            // TODO: 触发退款
            log.info("订单已驳回: orderNo={}, reason={}", request.getOrderNo(), request.getRejectReason());
        } else {
            throw new BizException("无效的操作类型");
        }

        if (StringUtils.hasText(request.getRemark())) {
            order.setAdminRemark(request.getRemark());
        }

        orderMainMapper.updateById(order);
    }

    /**
     * 添加备注
     */
    public void addRemark(OrderRemarkRequest request) {
        OrderMain order = getOrderByNo(request.getOrderNo());

        // 检查权限
        checkOrderPermission(order);

        order.setAdminRemark(request.getRemark());
        orderMainMapper.updateById(order);

        log.info("添加订单备注: orderNo={}", request.getOrderNo());
    }

    /**
     * 获取订单统计
     */
    public OrderStatsVO getStats() {
        OrderStatsVO stats = new OrderStatsVO();

        Long supplierId = getCurrentSupplierId();

        // 各状态订单数
        stats.setPendingPayCount(countByStatus(supplierId, OrderStatus.PENDING_PAY.getCode()));
        stats.setPendingConfirmCount(countByStatus(supplierId, OrderStatus.PENDING_CONFIRM.getCode()));
        stats.setPendingTravelCount(countByStatus(supplierId, OrderStatus.CONFIRMED.getCode()));
        stats.setTravelingCount(countByStatus(supplierId, OrderStatus.TRAVELING.getCode()));
        stats.setCompletedCount(countByStatus(supplierId, OrderStatus.COMPLETED.getCode()));
        stats.setRefundingCount(countByStatus(supplierId, OrderStatus.REFUND_APPLY.getCode()));

        // 今日订单
        LocalDate today = LocalDate.now();
        stats.setTodayOrderCount(countByDate(supplierId, today, today));
        stats.setTodaySalesAmount(sumAmountByDate(supplierId, today, today));

        // 本月订单
        LocalDate monthStart = today.withDayOfMonth(1);
        stats.setMonthOrderCount(countByDate(supplierId, monthStart, today));
        stats.setMonthSalesAmount(sumAmountByDate(supplierId, monthStart, today));

        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 根据订单号获取订单
     */
    private OrderMain getOrderByNo(String orderNo) {
        OrderMain order = orderMainMapper.selectOne(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getOrderNo, orderNo)
                        .eq(OrderMain::getIsDeleted, 0)
        );
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    /**
     * 获取当前供应商ID
     */
    private Long getCurrentSupplierId() {
        String roleType = (String) StpUtil.getSession().get("roleType");
        if ("supplier".equals(roleType)) {
            return (Long) StpUtil.getSession().get("supplierId");
        }
        return null;
    }

    /**
     * 检查订单权限
     */
    private void checkOrderPermission(OrderMain order) {
        Long supplierId = getCurrentSupplierId();
        if (supplierId != null && !supplierId.equals(order.getSupplierId())) {
            throw new BizException("无权限操作此订单");
        }
    }

    /**
     * 统计指定状态的订单数
     */
    private Long countByStatus(Long supplierId, Integer status) {
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getStatus, status)
                .eq(OrderMain::getIsDeleted, 0);
        if (supplierId != null) {
            wrapper.eq(OrderMain::getSupplierId, supplierId);
        }
        return orderMainMapper.selectCount(wrapper);
    }

    /**
     * 统计日期范围内的订单数
     */
    private Long countByDate(Long supplierId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<OrderMain>()
                .ge(OrderMain::getCreatedAt, startDate.atStartOfDay())
                .le(OrderMain::getCreatedAt, endDate.atTime(LocalTime.MAX))
                .ne(OrderMain::getStatus, OrderStatus.PENDING_PAY.getCode())
                .ne(OrderMain::getStatus, OrderStatus.CANCELLED.getCode())
                .ne(OrderMain::getStatus, OrderStatus.CLOSED.getCode())
                .eq(OrderMain::getIsDeleted, 0);
        if (supplierId != null) {
            wrapper.eq(OrderMain::getSupplierId, supplierId);
        }
        return orderMainMapper.selectCount(wrapper);
    }

    /**
     * 统计日期范围内的销售额
     */
    private BigDecimal sumAmountByDate(Long supplierId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<OrderMain>()
                .ge(OrderMain::getCreatedAt, startDate.atStartOfDay())
                .le(OrderMain::getCreatedAt, endDate.atTime(LocalTime.MAX))
                .ne(OrderMain::getStatus, OrderStatus.PENDING_PAY.getCode())
                .ne(OrderMain::getStatus, OrderStatus.CANCELLED.getCode())
                .ne(OrderMain::getStatus, OrderStatus.CLOSED.getCode())
                .ne(OrderMain::getStatus, OrderStatus.REFUNDED.getCode())
                .eq(OrderMain::getIsDeleted, 0);
        if (supplierId != null) {
            wrapper.eq(OrderMain::getSupplierId, supplierId);
        }

        List<OrderMain> orders = orderMainMapper.selectList(wrapper);
        return orders.stream()
                .map(OrderMain::getPayAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 转换为列表 VO
     */
    private OrderListVO convertToListVO(OrderMain order) {
        OrderListVO vo = new OrderListVO();
        BeanUtil.copyProperties(order, vo);

        // 状态描述
        vo.setStatusDesc(OrderStatus.getDesc(order.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserPhone(user.getPhone());
        }

        // 供应商信息
        if (order.getSupplierId() != null) {
            SystemSupplier supplier = systemSupplierMapper.selectById(order.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            }
        }

        return vo;
    }

    /**
     * 转换为出行人 VO
     */
    private OrderDetailVO.TravelerVO convertToTravelerVO(OrderTraveler traveler) {
        OrderDetailVO.TravelerVO vo = new OrderDetailVO.TravelerVO();
        vo.setName(traveler.getName());
        // 身份证脱敏
        vo.setIdCard(DesensitizedUtil.idCardNum(traveler.getIdCard(), 6, 4));
        vo.setPhone(traveler.getPhone());
        vo.setTravelerType(traveler.getTravelerType());
        vo.setTravelerTypeDesc(traveler.getTravelerType() == 1 ? "成人" : "儿童");
        return vo;
    }

    /**
     * 转换为退款 VO
     */
    private OrderDetailVO.RefundVO convertToRefundVO(OrderRefund refund) {
        OrderDetailVO.RefundVO vo = new OrderDetailVO.RefundVO();
        BeanUtil.copyProperties(refund, vo);
        return vo;
    }
}
