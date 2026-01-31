package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.user.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.member.entity.LeaderApply;
import com.xunjiqianxing.service.member.entity.LeaderInfo;
import com.xunjiqianxing.service.member.mapper.LeaderApplyMapper;
import com.xunjiqianxing.service.member.mapper.LeaderInfoMapper;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 领队管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLeaderService {

    private final LeaderApplyMapper leaderApplyMapper;
    private final LeaderInfoMapper leaderInfoMapper;
    private final UserInfoMapper userInfoMapper;

    /**
     * 分页查询领队申请列表
     */
    public PageResult<LeaderApplyVO> pageApplies(LeaderApplyQueryRequest request) {
        Page<LeaderApply> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<LeaderApply> wrapper = new LambdaQueryWrapper<>();

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(LeaderApply::getUserId, request.getUserId());
        }

        // 手机号
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(LeaderApply::getPhone, request.getPhone());
        }

        // 姓名
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(LeaderApply::getRealName, request.getRealName());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(LeaderApply::getStatus, request.getStatus());
        }

        // 申请时间
        if (request.getCreateDateBegin() != null) {
            wrapper.ge(LeaderApply::getCreatedAt, request.getCreateDateBegin().atStartOfDay());
        }
        if (request.getCreateDateEnd() != null) {
            wrapper.le(LeaderApply::getCreatedAt, request.getCreateDateEnd().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(LeaderApply::getCreatedAt);

        Page<LeaderApply> result = leaderApplyMapper.selectPage(page, wrapper);

        List<LeaderApplyVO> list = result.getRecords().stream()
                .map(this::convertToApplyVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取申请详情
     */
    public LeaderApplyVO getApplyDetail(Long id) {
        LeaderApply apply = leaderApplyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("申请记录不存在");
        }
        return convertToApplyVO(apply);
    }

    /**
     * 审核领队申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(LeaderApplyAuditRequest request) {
        LeaderApply apply = leaderApplyMapper.selectById(request.getId());
        if (apply == null) {
            throw new BizException("申请记录不存在");
        }

        if (apply.getStatus() != 0) {
            throw new BizException("该申请已审核");
        }

        // 获取当前用户ID
        Long userId = (Long) StpUtil.getSession().get("userId");

        if (request.getStatus() == 1) {
            // 审核通过
            apply.setStatus(1);
            apply.setAuditTime(LocalDateTime.now());
            apply.setAuditBy(userId);
            apply.setAuditRemark(request.getAuditRemark());

            // 创建领队信息
            LeaderInfo leader = new LeaderInfo();
            leader.setId(IdGenerator.nextId());
            leader.setUserId(apply.getUserId());
            leader.setRealName(apply.getRealName());
            leader.setPhone(apply.getPhone());
            leader.setExpertise(apply.getExpertise());
            leader.setIntro(apply.getIntro());
            leader.setCertificateImages(apply.getCertificateImages());
            leader.setTripCount(0);
            leader.setTotalCommission(BigDecimal.ZERO);
            leader.setStatus(1);
            leaderInfoMapper.insert(leader);

            // 更新用户领队状态
            UserInfo user = userInfoMapper.selectById(apply.getUserId());
            if (user != null) {
                user.setIsLeader(1);
                userInfoMapper.updateById(user);
            }

            log.info("领队申请审核通过: applyId={}, userId={}", request.getId(), apply.getUserId());

        } else if (request.getStatus() == 2) {
            // 审核驳回
            if (!StringUtils.hasText(request.getAuditRemark())) {
                throw new BizException("驳回时必须填写审核备注");
            }

            apply.setStatus(2);
            apply.setAuditTime(LocalDateTime.now());
            apply.setAuditBy(userId);
            apply.setAuditRemark(request.getAuditRemark());

            log.info("领队申请审核驳回: applyId={}, reason={}", request.getId(), request.getAuditRemark());

        } else {
            throw new BizException("无效的审核状态");
        }

        leaderApplyMapper.updateById(apply);
    }

    /**
     * 分页查询领队列表
     */
    public PageResult<LeaderListVO> pageLeaders(LeaderApplyQueryRequest request) {
        Page<LeaderInfo> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<LeaderInfo> wrapper = new LambdaQueryWrapper<>();

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(LeaderInfo::getUserId, request.getUserId());
        }

        // 手机号
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(LeaderInfo::getPhone, request.getPhone());
        }

        // 姓名
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(LeaderInfo::getRealName, request.getRealName());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(LeaderInfo::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(LeaderInfo::getCreatedAt);

        Page<LeaderInfo> result = leaderInfoMapper.selectPage(page, wrapper);

        List<LeaderListVO> list = result.getRecords().stream()
                .map(this::convertToLeaderVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 启用/禁用领队
     */
    public void updateStatus(Long id, Integer status) {
        LeaderInfo leader = leaderInfoMapper.selectById(id);
        if (leader == null) {
            throw new BizException("领队不存在");
        }

        leader.setStatus(status);
        leaderInfoMapper.updateById(leader);

        // 同步更新用户表
        UserInfo user = userInfoMapper.selectById(leader.getUserId());
        if (user != null) {
            user.setIsLeader(status);
            userInfoMapper.updateById(user);
        }

        log.info("更新领队状态: leaderId={}, status={}", id, status);
    }

    /**
     * 获取待审核申请数量
     */
    public Long getPendingApplyCount() {
        return leaderApplyMapper.selectCount(
                new LambdaQueryWrapper<LeaderApply>()
                        .eq(LeaderApply::getStatus, 0)
        );
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为申请 VO
     */
    private LeaderApplyVO convertToApplyVO(LeaderApply apply) {
        LeaderApplyVO vo = new LeaderApplyVO();
        BeanUtil.copyProperties(apply, vo);

        // 状态描述
        vo.setStatusDesc(getApplyStatusDesc(apply.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(apply.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        return vo;
    }

    /**
     * 转换为领队 VO
     */
    private LeaderListVO convertToLeaderVO(LeaderInfo leader) {
        LeaderListVO vo = new LeaderListVO();
        BeanUtil.copyProperties(leader, vo);
        vo.setStatusDesc(leader.getStatus() == 1 ? "正常" : "禁用");
        return vo;
    }

    /**
     * 获取申请状态描述
     */
    private String getApplyStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "";
        }
    }
}
