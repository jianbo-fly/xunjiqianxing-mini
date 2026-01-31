package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.promotion.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.promotion.entity.PromoterBindLog;
import com.xunjiqianxing.service.promotion.entity.PromoterCommission;
import com.xunjiqianxing.service.promotion.entity.PromoterInfo;
import com.xunjiqianxing.service.promotion.mapper.PromoterBindLogMapper;
import com.xunjiqianxing.service.promotion.mapper.PromoterCommissionMapper;
import com.xunjiqianxing.service.promotion.mapper.PromoterInfoMapper;
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
 * 管理后台 - 推广员管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPromoterService {

    private final PromoterInfoMapper promoterInfoMapper;
    private final PromoterBindLogMapper promoterBindLogMapper;
    private final PromoterCommissionMapper promoterCommissionMapper;
    private final UserInfoMapper userInfoMapper;

    /**
     * 分页查询推广员申请列表
     */
    public PageResult<PromoterListVO> pageApplies(PromoterQueryRequest request) {
        Page<PromoterInfo> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<PromoterInfo> wrapper = new LambdaQueryWrapper<PromoterInfo>()
                .eq(PromoterInfo::getStatus, 0) // 待审核
                .eq(PromoterInfo::getIsDeleted, 0);

        // 申请时间
        if (request.getCreateDateBegin() != null) {
            wrapper.ge(PromoterInfo::getCreatedAt, request.getCreateDateBegin().atStartOfDay());
        }
        if (request.getCreateDateEnd() != null) {
            wrapper.le(PromoterInfo::getCreatedAt, request.getCreateDateEnd().atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(PromoterInfo::getCreatedAt);

        Page<PromoterInfo> result = promoterInfoMapper.selectPage(page, wrapper);

        List<PromoterListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 分页查询推广员列表
     */
    public PageResult<PromoterListVO> pageList(PromoterQueryRequest request) {
        Page<PromoterInfo> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<PromoterInfo> wrapper = new LambdaQueryWrapper<PromoterInfo>()
                .eq(PromoterInfo::getIsDeleted, 0);

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(PromoterInfo::getUserId, request.getUserId());
        }

        // 手机号
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(PromoterInfo::getPhone, request.getPhone());
        }

        // 姓名
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(PromoterInfo::getRealName, request.getRealName());
        }

        // 推广码
        if (StringUtils.hasText(request.getPromoCode())) {
            wrapper.eq(PromoterInfo::getPromoCode, request.getPromoCode());
        }

        // 等级
        if (request.getLevel() != null) {
            wrapper.eq(PromoterInfo::getLevel, request.getLevel());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(PromoterInfo::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(PromoterInfo::getCreatedAt);

        Page<PromoterInfo> result = promoterInfoMapper.selectPage(page, wrapper);

        List<PromoterListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取推广员详情
     */
    public PromoterDetailVO getDetail(Long id) {
        PromoterInfo promoter = promoterInfoMapper.selectById(id);
        if (promoter == null || promoter.getIsDeleted() == 1) {
            throw new BizException("推广员不存在");
        }

        PromoterDetailVO vo = new PromoterDetailVO();
        BeanUtil.copyProperties(promoter, vo);
        vo.setLevelDesc(getLevelDesc(promoter.getLevel()));
        vo.setStatusDesc(getStatusDesc(promoter.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(promoter.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        return vo;
    }

    /**
     * 审核推广员申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(PromoterAuditRequest request) {
        PromoterInfo promoter = promoterInfoMapper.selectById(request.getId());
        if (promoter == null || promoter.getIsDeleted() == 1) {
            throw new BizException("推广员不存在");
        }

        if (promoter.getStatus() != 0) {
            throw new BizException("该申请已审核");
        }

        if (request.getStatus() == 1) {
            // 审核通过
            promoter.setStatus(1);
            promoter.setAuditAt(LocalDateTime.now());
            promoter.setRemark(request.getRemark());

            // 设置佣金比例
            if (request.getCommissionRate() != null) {
                promoter.setCommissionRate(request.getCommissionRate());
            } else {
                promoter.setCommissionRate(new BigDecimal("0.05")); // 默认5%
            }

            // 设置等级
            if (request.getLevel() != null) {
                promoter.setLevel(request.getLevel());
            } else {
                promoter.setLevel(1); // 默认普通等级
            }

            // 更新用户为推广员
            UserInfo user = userInfoMapper.selectById(promoter.getUserId());
            if (user != null) {
                user.setIsPromoter(1);
                userInfoMapper.updateById(user);
            }

            log.info("推广员申请审核通过: promoterId={}, userId={}", request.getId(), promoter.getUserId());

        } else if (request.getStatus() == 2) {
            // 审核驳回
            promoter.setStatus(2);
            promoter.setAuditAt(LocalDateTime.now());
            promoter.setRemark(request.getRemark());

            log.info("推广员申请审核驳回: promoterId={}, reason={}", request.getId(), request.getRemark());

        } else {
            throw new BizException("无效的审核状态");
        }

        promoterInfoMapper.updateById(promoter);
    }

    /**
     * 更新推广员信息
     */
    public void update(PromoterUpdateRequest request) {
        PromoterInfo promoter = promoterInfoMapper.selectById(request.getId());
        if (promoter == null || promoter.getIsDeleted() == 1) {
            throw new BizException("推广员不存在");
        }

        if (request.getLevel() != null) {
            promoter.setLevel(request.getLevel());
        }
        if (request.getCommissionRate() != null) {
            promoter.setCommissionRate(request.getCommissionRate());
        }
        if (request.getRemark() != null) {
            promoter.setRemark(request.getRemark());
        }

        promoterInfoMapper.updateById(promoter);
        log.info("更新推广员信息: promoterId={}", request.getId());
    }

    /**
     * 启用/禁用推广员
     */
    public void updateStatus(Long id, Integer status) {
        PromoterInfo promoter = promoterInfoMapper.selectById(id);
        if (promoter == null || promoter.getIsDeleted() == 1) {
            throw new BizException("推广员不存在");
        }

        promoter.setStatus(status);
        promoterInfoMapper.updateById(promoter);

        // 同步更新用户表
        UserInfo user = userInfoMapper.selectById(promoter.getUserId());
        if (user != null) {
            user.setIsPromoter(status == 1 ? 1 : 0);
            userInfoMapper.updateById(user);
        }

        log.info("更新推广员状态: promoterId={}, status={}", id, status);
    }

    /**
     * 获取绑定记录
     */
    public PageResult<BindLogVO> getBindLogs(Long promoterId, Integer page, Integer pageSize) {
        Page<PromoterBindLog> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<PromoterBindLog> wrapper = new LambdaQueryWrapper<PromoterBindLog>()
                .eq(PromoterBindLog::getPromoterId, promoterId)
                .orderByDesc(PromoterBindLog::getCreatedAt);

        Page<PromoterBindLog> result = promoterBindLogMapper.selectPage(pageObj, wrapper);

        List<BindLogVO> list = result.getRecords().stream()
                .map(this::convertToBindLogVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), page, pageSize);
    }

    /**
     * 获取佣金记录
     */
    public PageResult<CommissionVO> getCommissions(Long promoterId, Integer page, Integer pageSize) {
        Page<PromoterCommission> pageObj = new Page<>(page, pageSize);

        LambdaQueryWrapper<PromoterCommission> wrapper = new LambdaQueryWrapper<PromoterCommission>()
                .eq(PromoterCommission::getPromoterId, promoterId)
                .orderByDesc(PromoterCommission::getCreatedAt);

        Page<PromoterCommission> result = promoterCommissionMapper.selectPage(pageObj, wrapper);

        List<CommissionVO> list = result.getRecords().stream()
                .map(this::convertToCommissionVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), page, pageSize);
    }

    /**
     * 待审核申请数量
     */
    public Long getPendingApplyCount() {
        return promoterInfoMapper.selectCount(
                new LambdaQueryWrapper<PromoterInfo>()
                        .eq(PromoterInfo::getStatus, 0)
                        .eq(PromoterInfo::getIsDeleted, 0)
        );
    }

    // ==================== 私有方法 ====================

    private PromoterListVO convertToListVO(PromoterInfo promoter) {
        PromoterListVO vo = new PromoterListVO();
        BeanUtil.copyProperties(promoter, vo);
        vo.setLevelDesc(getLevelDesc(promoter.getLevel()));
        vo.setStatusDesc(getStatusDesc(promoter.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(promoter.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }

        return vo;
    }

    private BindLogVO convertToBindLogVO(PromoterBindLog log) {
        BindLogVO vo = new BindLogVO();
        BeanUtil.copyProperties(log, vo);

        // 用户信息
        UserInfo user = userInfoMapper.selectById(log.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
            vo.setUserPhone(user.getPhone());
        }

        vo.setValid(log.getUnbindTime() == null);

        return vo;
    }

    private CommissionVO convertToCommissionVO(PromoterCommission commission) {
        CommissionVO vo = new CommissionVO();
        BeanUtil.copyProperties(commission, vo);
        vo.setStatusDesc(getCommissionStatusDesc(commission.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(commission.getFromUserId());
        if (user != null) {
            vo.setFromUserNickname(user.getNickname());
        }

        return vo;
    }

    private String getLevelDesc(Integer level) {
        if (level == null) return "";
        switch (level) {
            case 1: return "普通";
            case 2: return "银牌";
            case 3: return "金牌";
            default: return "";
        }
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审核";
            case 1: return "正常";
            case 2: return "禁用";
            default: return "";
        }
    }

    private String getCommissionStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待结算";
            case 1: return "已结算";
            case 2: return "已取消";
            default: return "";
        }
    }
}
