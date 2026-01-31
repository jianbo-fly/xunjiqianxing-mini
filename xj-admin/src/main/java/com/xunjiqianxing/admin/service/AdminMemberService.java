package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.user.MemberExtendRequest;
import com.xunjiqianxing.admin.dto.user.MemberListVO;
import com.xunjiqianxing.admin.dto.user.MemberQueryRequest;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.member.entity.MemberOrder;
import com.xunjiqianxing.service.member.mapper.MemberOrderMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 会员管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final UserInfoMapper userInfoMapper;
    private final MemberOrderMapper memberOrderMapper;

    /**
     * 分页查询会员列表
     */
    public PageResult<MemberListVO> pageList(MemberQueryRequest request) {
        Page<UserInfo> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getIsMember, 1);

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

        // 会员状态筛选
        if (request.getMemberStatus() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (request.getMemberStatus() == 1) {
                // 有效会员
                wrapper.ge(UserInfo::getMemberExpireAt, now);
            } else if (request.getMemberStatus() == 2) {
                // 已过期
                wrapper.lt(UserInfo::getMemberExpireAt, now);
            }
        }

        wrapper.orderByDesc(UserInfo::getMemberExpireAt);

        Page<UserInfo> result = userInfoMapper.selectPage(page, wrapper);

        List<MemberListVO> list = result.getRecords().stream()
                .map(this::convertToMemberVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 延长会员时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void extend(MemberExtendRequest request) {
        UserInfo user = userInfoMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (user.getIsMember() != 1) {
            throw new BizException("该用户不是会员");
        }

        // 计算新的到期时间
        LocalDateTime currentExpire = user.getMemberExpireAt();
        LocalDateTime baseTime = currentExpire.isAfter(LocalDateTime.now()) ? currentExpire : LocalDateTime.now();
        LocalDateTime newExpire = baseTime.plusDays(request.getDays());

        user.setMemberExpireAt(newExpire);
        userInfoMapper.updateById(user);

        // TODO: 记录延期日志

        log.info("延长会员时间: userId={}, days={}, newExpireAt={}, reason={}",
                request.getUserId(), request.getDays(), newExpire, request.getReason());
    }

    /**
     * 获取会员统计
     */
    public MemberStatsVO getStats() {
        MemberStatsVO stats = new MemberStatsVO();

        LocalDateTime now = LocalDateTime.now();

        // 总会员数
        stats.setTotalCount(userInfoMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getIsMember, 1)
        ));

        // 有效会员数
        stats.setActiveCount(userInfoMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getIsMember, 1)
                        .ge(UserInfo::getMemberExpireAt, now)
        ));

        // 已过期会员数
        stats.setExpiredCount(userInfoMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getIsMember, 1)
                        .lt(UserInfo::getMemberExpireAt, now)
        ));

        // 本月新增会员数
        LocalDateTime monthStart = now.withDayOfMonth(1).with(LocalTime.MIN);
        stats.setMonthNewCount(memberOrderMapper.selectCount(
                new LambdaQueryWrapper<MemberOrder>()
                        .eq(MemberOrder::getStatus, 1)
                        .ge(MemberOrder::getPayTime, monthStart)
        ));

        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为会员 VO
     */
    private MemberListVO convertToMemberVO(UserInfo user) {
        MemberListVO vo = new MemberListVO();
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setMemberExpireAt(user.getMemberExpireAt());

        // 会员状态
        LocalDateTime now = LocalDateTime.now();
        if (user.getMemberExpireAt() != null && user.getMemberExpireAt().isAfter(now)) {
            vo.setMemberStatus(1);
            vo.setMemberStatusDesc("有效");
            vo.setRemainDays((int) ChronoUnit.DAYS.between(now, user.getMemberExpireAt()));
        } else {
            vo.setMemberStatus(2);
            vo.setMemberStatusDesc("已过期");
            vo.setRemainDays(0);
        }

        // 累计支付金额
        List<MemberOrder> orders = memberOrderMapper.selectList(
                new LambdaQueryWrapper<MemberOrder>()
                        .eq(MemberOrder::getUserId, user.getId())
                        .eq(MemberOrder::getStatus, 1)
        );
        BigDecimal totalPaid = orders.stream()
                .map(MemberOrder::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalPaidAmount(totalPaid);

        // 首次开通时间
        MemberOrder firstOrder = memberOrderMapper.selectOne(
                new LambdaQueryWrapper<MemberOrder>()
                        .eq(MemberOrder::getUserId, user.getId())
                        .eq(MemberOrder::getStatus, 1)
                        .orderByAsc(MemberOrder::getPayTime)
                        .last("LIMIT 1")
        );
        if (firstOrder != null) {
            vo.setFirstOpenAt(firstOrder.getPayTime());
        }

        return vo;
    }

    /**
     * 会员统计响应
     */
    @lombok.Data
    public static class MemberStatsVO {
        private Long totalCount;
        private Long activeCount;
        private Long expiredCount;
        private Long monthNewCount;
    }
}
