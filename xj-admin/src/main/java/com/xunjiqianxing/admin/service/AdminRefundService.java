package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.order.RefundAuditRequest;
import com.xunjiqianxing.admin.dto.order.RefundListVO;
import com.xunjiqianxing.admin.dto.order.RefundQueryRequest;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.order.entity.OrderMain;
import com.xunjiqianxing.service.order.entity.OrderRefund;
import com.xunjiqianxing.service.order.enums.OrderStatus;
import com.xunjiqianxing.service.order.mapper.OrderMainMapper;
import com.xunjiqianxing.service.order.mapper.OrderRefundMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 退款管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRefundService {

    private final OrderRefundMapper orderRefundMapper;
    private final OrderMainMapper orderMainMapper;
    private final UserInfoMapper userInfoMapper;

    /**
     * 分页查询退款列表
     */
    public PageResult<RefundListVO> pageList(RefundQueryRequest request) {
        Page<OrderRefund> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();

        // 退款编号
        if (StringUtils.hasText(request.getRefundNo())) {
            wrapper.eq(OrderRefund::getRefundNo, request.getRefundNo());
        }

        // 订单编号
        if (StringUtils.hasText(request.getOrderNo())) {
            wrapper.eq(OrderRefund::getOrderNo, request.getOrderNo());
        }

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(OrderRefund::getUserId, request.getUserId());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(OrderRefund::getStatus, request.getStatus());
        }

        // 申请时间
        if (request.getCreateDateBegin() != null) {
            wrapper.ge(OrderRefund::getCreatedAt, request.getCreateDateBegin().atStartOfDay());
        }
        if (request.getCreateDateEnd() != null) {
            wrapper.le(OrderRefund::getCreatedAt, request.getCreateDateEnd().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(OrderRefund::getCreatedAt);

        Page<OrderRefund> result = orderRefundMapper.selectPage(page, wrapper);

        List<RefundListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取退款详情
     */
    public RefundListVO getDetail(Long id) {
        OrderRefund refund = orderRefundMapper.selectById(id);
        if (refund == null) {
            throw new BizException("退款记录不存在");
        }
        return convertToListVO(refund);
    }

    /**
     * 审核退款
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(RefundAuditRequest request) {
        OrderRefund refund = orderRefundMapper.selectById(request.getId());
        if (refund == null) {
            throw new BizException("退款记录不存在");
        }

        if (refund.getStatus() != 0) {
            throw new BizException("该退款已审核");
        }

        // 获取订单
        OrderMain order = orderMainMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BizException("订单不存在");
        }

        // 获取当前用户ID
        Long userId = (Long) StpUtil.getSession().get("userId");

        if (request.getStatus() == 1) {
            // 审核通过
            refund.setStatus(1);
            refund.setActualAmount(request.getActualAmount() != null ? 
                    request.getActualAmount() : refund.getRefundAmount());
            refund.setAuditTime(LocalDateTime.now());
            refund.setAuditBy(userId);
            refund.setAuditRemark(request.getAuditRemark());

            // 更新订单状态
            order.setStatus(OrderStatus.REFUNDED.getCode());
            order.setRefundAmount(refund.getActualAmount());
            orderMainMapper.updateById(order);

            // TODO: 触发微信退款

            log.info("退款审核通过: refundNo={}, actualAmount={}", 
                    refund.getRefundNo(), refund.getActualAmount());

        } else if (request.getStatus() == 2) {
            // 审核驳回
            if (!StringUtils.hasText(request.getAuditRemark())) {
                throw new BizException("驳回时必须填写审核备注");
            }

            refund.setStatus(2);
            refund.setAuditTime(LocalDateTime.now());
            refund.setAuditBy(userId);
            refund.setAuditRemark(request.getAuditRemark());

            // 恢复订单状态为已确认
            order.setStatus(OrderStatus.CONFIRMED.getCode());
            orderMainMapper.updateById(order);

            log.info("退款审核驳回: refundNo={}, reason={}", 
                    refund.getRefundNo(), request.getAuditRemark());

        } else {
            throw new BizException("无效的审核状态");
        }

        orderRefundMapper.updateById(refund);
    }

    /**
     * 获取待审核退款数量
     */
    public Long getPendingCount() {
        return orderRefundMapper.selectCount(
                new LambdaQueryWrapper<OrderRefund>()
                        .eq(OrderRefund::getStatus, 0)
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为列表 VO
     */
    private RefundListVO convertToListVO(OrderRefund refund) {
        RefundListVO vo = new RefundListVO();
        BeanUtil.copyProperties(refund, vo);

        // 状态描述
        vo.setStatusDesc(getStatusDesc(refund.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(refund.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
        }

        // 订单信息
        OrderMain order = orderMainMapper.selectById(refund.getOrderId());
        if (order != null) {
            vo.setProductName(order.getProductName());
        }

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "";
        }
    }
}
