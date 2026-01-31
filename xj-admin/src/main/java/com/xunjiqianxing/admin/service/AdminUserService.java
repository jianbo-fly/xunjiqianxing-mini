package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 用户管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserInfoMapper userInfoMapper;
    private final UserTravelerMapper userTravelerMapper;
    private final OrderMainMapper orderMainMapper;

    /**
     * 分页查询用户列表
     */
    public PageResult<UserListVO> pageList(UserQueryRequest request) {
        Page<UserInfo> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(UserInfo::getId, request.getUserId());
        }

        // 手机号
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(UserInfo::getPhone, request.getPhone());
        }

        // 昵称
        if (StringUtils.hasText(request.getNickname())) {
            wrapper.like(UserInfo::getNickname, request.getNickname());
        }

        // 会员状态
        if (request.getIsMember() != null) {
            wrapper.eq(UserInfo::getIsMember, request.getIsMember());
        }

        // 领队状态
        if (request.getIsLeader() != null) {
            wrapper.eq(UserInfo::getIsLeader, request.getIsLeader());
        }

        // 推广员状态
        if (request.getIsPromoter() != null) {
            wrapper.eq(UserInfo::getIsPromoter, request.getIsPromoter());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(UserInfo::getStatus, request.getStatus());
        }

        // 注册时间
        if (request.getCreateDateBegin() != null) {
            wrapper.ge(UserInfo::getCreatedAt, request.getCreateDateBegin().atStartOfDay());
        }
        if (request.getCreateDateEnd() != null) {
            wrapper.le(UserInfo::getCreatedAt, request.getCreateDateEnd().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(UserInfo::getCreatedAt);

        Page<UserInfo> result = userInfoMapper.selectPage(page, wrapper);

        List<UserListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取用户详情
     */
    public UserDetailVO getDetail(Long id) {
        UserInfo user = userInfoMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        UserDetailVO vo = new UserDetailVO();
        BeanUtil.copyProperties(user, vo);

        // 统计订单数
        Long orderCount = orderMainMapper.selectCount(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getUserId, id)
                        .eq(OrderMain::getIsDeleted, 0)
        );
        vo.setOrderCount(orderCount.intValue());

        // 统计消费总额
        List<OrderMain> orders = orderMainMapper.selectList(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getUserId, id)
                        .ne(OrderMain::getStatus, 0) // 排除待支付
                        .ne(OrderMain::getStatus, 5) // 排除已取消
                        .ne(OrderMain::getStatus, 8) // 排除已关闭
                        .eq(OrderMain::getIsDeleted, 0)
        );
        BigDecimal totalAmount = orders.stream()
                .map(OrderMain::getPayAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalAmount(totalAmount);

        // 统计出行人数量
        Long travelerCount = userTravelerMapper.selectCount(
                new LambdaQueryWrapper<UserTraveler>()
                        .eq(UserTraveler::getUserId, id)
                        .eq(UserTraveler::getIsDeleted, 0)
        );
        vo.setTravelerCount(travelerCount.intValue());

        return vo;
    }

    /**
     * 禁用/启用用户
     */
    public void updateStatus(Long id, Integer status) {
        UserInfo user = userInfoMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        user.setStatus(status);
        userInfoMapper.updateById(user);

        log.info("更新用户状态: userId={}, status={}", id, status);
    }

    /**
     * 调整积分
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustPoints(PointsAdjustRequest request) {
        UserInfo user = userInfoMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        int newPoints = user.getPoints() + request.getAmount();
        if (newPoints < 0) {
            throw new BizException("积分不足");
        }

        user.setPoints(newPoints);
        userInfoMapper.updateById(user);

        // TODO: 记录积分变更日志

        log.info("调整用户积分: userId={}, amount={}, newPoints={}, reason={}",
                request.getUserId(), request.getAmount(), newPoints, request.getReason());
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为列表 VO
     */
    private UserListVO convertToListVO(UserInfo user) {
        UserListVO vo = new UserListVO();
        BeanUtil.copyProperties(user, vo);

        // 性别描述
        vo.setGenderDesc(getGenderDesc(user.getGender()));

        // 统计订单数
        Long orderCount = orderMainMapper.selectCount(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getUserId, user.getId())
                        .eq(OrderMain::getIsDeleted, 0)
        );
        vo.setOrderCount(orderCount.intValue());

        return vo;
    }

    /**
     * 获取性别描述
     */
    private String getGenderDesc(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }
}
