package com.xunjiqianxing.admin.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.promotion.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.common.utils.IdGenerator;
import com.xunjiqianxing.service.promotion.entity.Coupon;
import com.xunjiqianxing.service.promotion.entity.UserCoupon;
import com.xunjiqianxing.service.promotion.mapper.CouponMapper;
import com.xunjiqianxing.service.promotion.mapper.UserCouponMapper;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 优惠券管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserInfoMapper userInfoMapper;

    /**
     * 分页查询优惠券模板列表
     */
    public PageResult<CouponListVO> pageList(CouponQueryRequest request) {
        Page<Coupon> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getIsDeleted, 0);

        // 名称
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(Coupon::getName, request.getName());
        }

        // 类型
        if (request.getType() != null) {
            wrapper.eq(Coupon::getType, request.getType());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(Coupon::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Coupon::getCreatedAt);

        Page<Coupon> result = couponMapper.selectPage(page, wrapper);

        List<CouponListVO> list = result.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取优惠券详情
     */
    public CouponListVO getDetail(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BizException("优惠券不存在");
        }
        return convertToListVO(coupon);
    }

    /**
     * 创建优惠券模板
     */
    public Long create(CouponCreateRequest request) {
        // 参数校验
        validateCouponRequest(request);

        Coupon coupon = new Coupon();
        BeanUtil.copyProperties(request, coupon);
        coupon.setId(IdGenerator.nextId());
        coupon.setReceivedCount(0);
        coupon.setStatus(1); // 默认启用
        coupon.setIsDeleted(0);

        couponMapper.insert(coupon);
        log.info("创建优惠券模板: id={}, name={}", coupon.getId(), coupon.getName());

        return coupon.getId();
    }

    /**
     * 更新优惠券模板
     */
    public void update(CouponUpdateRequest request) {
        Coupon coupon = couponMapper.selectById(request.getId());
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BizException("优惠券不存在");
        }

        // 只能更新部分字段
        if (StringUtils.hasText(request.getName())) {
            coupon.setName(request.getName());
        }
        if (request.getThreshold() != null) {
            coupon.setThreshold(request.getThreshold());
        }
        if (request.getAmount() != null) {
            coupon.setAmount(request.getAmount());
        }
        if (request.getDiscount() != null) {
            coupon.setDiscount(request.getDiscount());
        }
        if (request.getMaxAmount() != null) {
            coupon.setMaxAmount(request.getMaxAmount());
        }
        if (request.getScope() != null) {
            coupon.setScope(request.getScope());
        }
        if (request.getScopeIds() != null) {
            coupon.setScopeIds(request.getScopeIds());
        }
        if (request.getValidEnd() != null) {
            coupon.setValidEnd(request.getValidEnd());
        }
        if (request.getTotalCount() != null) {
            if (request.getTotalCount() < coupon.getReceivedCount()) {
                throw new BizException("发行总量不能小于已领取数量");
            }
            coupon.setTotalCount(request.getTotalCount());
        }
        if (request.getPerLimit() != null) {
            coupon.setPerLimit(request.getPerLimit());
        }
        if (request.getMemberOnly() != null) {
            coupon.setMemberOnly(request.getMemberOnly());
        }
        if (request.getDescription() != null) {
            coupon.setDescription(request.getDescription());
        }

        couponMapper.updateById(coupon);
        log.info("更新优惠券模板: id={}", request.getId());
    }

    /**
     * 启用/停用优惠券
     */
    public void updateStatus(Long id, Integer status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BizException("优惠券不存在");
        }

        coupon.setStatus(status);
        couponMapper.updateById(coupon);

        log.info("更新优惠券状态: id={}, status={}", id, status);
    }

    /**
     * 删除优惠券模板
     */
    public void delete(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BizException("优惠券不存在");
        }

        // 检查是否有已发放的券
        Long issuedCount = userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getCouponId, id)
                        .eq(UserCoupon::getIsDeleted, 0)
        );
        if (issuedCount > 0) {
            throw new BizException("该优惠券已有发放记录，无法删除");
        }

        coupon.setIsDeleted(1);
        couponMapper.updateById(coupon);

        log.info("删除优惠券模板: id={}", id);
    }

    /**
     * 发放优惠券
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer issue(CouponIssueRequest request) {
        Coupon coupon = couponMapper.selectById(request.getCouponId());
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BizException("优惠券不存在");
        }

        if (coupon.getStatus() != 1) {
            throw new BizException("优惠券已停用");
        }

        // 获取要发放的用户列表
        List<Long> userIds;
        if (request.getIssueType() == 1) {
            // 指定用户
            if (CollectionUtils.isEmpty(request.getUserIds())) {
                throw new BizException("请选择要发放的用户");
            }
            userIds = request.getUserIds();
        } else if (request.getIssueType() == 2) {
            // 全部会员
            List<UserInfo> members = userInfoMapper.selectList(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getIsMember, 1)
                            .eq(UserInfo::getStatus, 1)
            );
            userIds = members.stream().map(UserInfo::getId).collect(Collectors.toList());
        } else {
            throw new BizException("无效的发放类型");
        }

        // 检查剩余库存
        int needCount = userIds.size() * request.getCount();
        int remainCount = coupon.getTotalCount() - coupon.getReceivedCount();
        if (needCount > remainCount) {
            throw new BizException("优惠券库存不足，剩余" + remainCount + "张");
        }

        // 计算有效期
        LocalDateTime validStart;
        LocalDateTime validEnd;
        if (coupon.getValidType() == 1) {
            // 固定时间
            validStart = coupon.getValidStart();
            validEnd = coupon.getValidEnd();
        } else {
            // 领取后N天
            validStart = LocalDateTime.now();
            validEnd = validStart.plusDays(coupon.getValidDays());
        }

        // 发放
        List<UserCoupon> coupons = new ArrayList<>();
        for (Long userId : userIds) {
            for (int i = 0; i < request.getCount(); i++) {
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setId(IdGenerator.nextId());
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(coupon.getId());
                userCoupon.setCouponName(coupon.getName());
                userCoupon.setCouponType(coupon.getType());
                userCoupon.setThreshold(coupon.getThreshold());
                userCoupon.setAmount(coupon.getAmount());
                userCoupon.setDiscount(coupon.getDiscount());
                userCoupon.setMaxAmount(coupon.getMaxAmount());
                userCoupon.setValidStart(validStart);
                userCoupon.setValidEnd(validEnd);
                userCoupon.setStatus(0); // 未使用
                userCoupon.setIsDeleted(0);
                coupons.add(userCoupon);
            }
        }

        // 批量插入
        for (UserCoupon uc : coupons) {
            userCouponMapper.insert(uc);
        }

        // 更新已领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() + coupons.size());
        couponMapper.updateById(coupon);

        log.info("发放优惠券: couponId={}, userCount={}, totalIssued={}",
                request.getCouponId(), userIds.size(), coupons.size());

        return coupons.size();
    }

    /**
     * 查询发放记录
     */
    public PageResult<UserCouponVO> pageRecords(UserCouponQueryRequest request) {
        Page<UserCoupon> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getIsDeleted, 0);

        // 优惠券ID
        if (request.getCouponId() != null) {
            wrapper.eq(UserCoupon::getCouponId, request.getCouponId());
        }

        // 用户ID
        if (request.getUserId() != null) {
            wrapper.eq(UserCoupon::getUserId, request.getUserId());
        }

        // 状态
        if (request.getStatus() != null) {
            wrapper.eq(UserCoupon::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(UserCoupon::getCreatedAt);

        Page<UserCoupon> result = userCouponMapper.selectPage(page, wrapper);

        List<UserCouponVO> list = result.getRecords().stream()
                .map(this::convertToUserCouponVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 优惠券统计
     */
    public CouponStatsVO getStats() {
        CouponStatsVO stats = new CouponStatsVO();

        // 启用的券模板数
        stats.setActiveTemplateCount(couponMapper.selectCount(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .eq(Coupon::getIsDeleted, 0)
        ));

        // 已发放总数
        stats.setTotalIssuedCount(userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getIsDeleted, 0)
        ));

        // 已使用数
        stats.setUsedCount(userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getStatus, 1)
                        .eq(UserCoupon::getIsDeleted, 0)
        ));

        // 已过期数
        stats.setExpiredCount(userCouponMapper.selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getStatus, 2)
                        .eq(UserCoupon::getIsDeleted, 0)
        ));

        return stats;
    }

    // ==================== 私有方法 ====================

    private void validateCouponRequest(CouponCreateRequest request) {
        if (request.getType() == 1 || request.getType() == 3) {
            // 满减券或无门槛券必须填优惠金额
            if (request.getAmount() == null) {
                throw new BizException("请填写优惠金额");
            }
        }
        if (request.getType() == 2) {
            // 折扣券必须填折扣率
            if (request.getDiscount() == null) {
                throw new BizException("请填写折扣率");
            }
        }
        if (request.getValidType() == 1) {
            // 固定时间必须填开始和结束时间
            if (request.getValidStart() == null || request.getValidEnd() == null) {
                throw new BizException("请填写有效期时间");
            }
        }
        if (request.getValidType() == 2) {
            // 动态有效期必须填天数
            if (request.getValidDays() == null || request.getValidDays() <= 0) {
                throw new BizException("请填写有效天数");
            }
        }
    }

    private CouponListVO convertToListVO(Coupon coupon) {
        CouponListVO vo = new CouponListVO();
        BeanUtil.copyProperties(coupon, vo);
        vo.setTypeDesc(getCouponTypeDesc(coupon.getType()));
        vo.setScopeDesc(getScopeDesc(coupon.getScope()));
        vo.setStatusDesc(coupon.getStatus() == 1 ? "启用" : "停用");
        vo.setRemainCount(coupon.getTotalCount() - coupon.getReceivedCount());
        return vo;
    }

    private UserCouponVO convertToUserCouponVO(UserCoupon uc) {
        UserCouponVO vo = new UserCouponVO();
        BeanUtil.copyProperties(uc, vo);
        vo.setCouponTypeDesc(getCouponTypeDesc(uc.getCouponType()));
        vo.setStatusDesc(getUserCouponStatusDesc(uc.getStatus()));

        // 用户信息
        UserInfo user = userInfoMapper.selectById(uc.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserPhone(user.getPhone());
        }

        return vo;
    }

    private String getCouponTypeDesc(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "满减券";
            case 2: return "折扣券";
            case 3: return "无门槛券";
            default: return "";
        }
    }

    private String getScopeDesc(Integer scope) {
        if (scope == null) return "全场";
        switch (scope) {
            case 0: return "全场";
            case 1: return "指定线路";
            case 2: return "指定分类";
            default: return "";
        }
    }

    private String getUserCouponStatusDesc(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "未使用";
            case 1: return "已使用";
            case 2: return "已过期";
            default: return "";
        }
    }

    /**
     * 优惠券统计
     */
    @lombok.Data
    public static class CouponStatsVO {
        private Long activeTemplateCount;
        private Long totalIssuedCount;
        private Long usedCount;
        private Long expiredCount;
    }
}
