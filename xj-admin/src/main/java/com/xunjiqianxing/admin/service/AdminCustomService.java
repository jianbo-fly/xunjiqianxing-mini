package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.custom.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.custom.entity.CustomDemand;
import com.xunjiqianxing.service.custom.entity.CustomFollowRecord;
import com.xunjiqianxing.service.custom.mapper.CustomDemandMapper;
import com.xunjiqianxing.service.custom.mapper.CustomFollowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台 - 定制需求管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCustomService {

    private final CustomDemandMapper customDemandMapper;
    private final CustomFollowRecordMapper customFollowRecordMapper;

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            0, "待处理",
            1, "跟进中",
            2, "已完成",
            3, "已关闭"
    );

    /**
     * 分页查询定制需求列表
     */
    public PageResult<CustomListVO> pageList(CustomQueryRequest request) {
        Page<CustomDemand> page = new Page<>(request.getPage(), request.getPageSize());

        LambdaQueryWrapper<CustomDemand> wrapper = new LambdaQueryWrapper<>();

        // 手机号筛选
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(CustomDemand::getPhone, request.getPhone());
        }

        // 目的地筛选
        if (StringUtils.hasText(request.getDestination())) {
            wrapper.like(CustomDemand::getDestination, request.getDestination());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            wrapper.eq(CustomDemand::getStatus, request.getStatus());
        }

        // 出行日期筛选
        if (request.getStartDateBegin() != null) {
            wrapper.ge(CustomDemand::getTravelDateStart, request.getStartDateBegin());
        }
        if (request.getStartDateEnd() != null) {
            wrapper.le(CustomDemand::getTravelDateStart, request.getStartDateEnd());
        }

        wrapper.orderByDesc(CustomDemand::getCreatedAt);

        Page<CustomDemand> result = customDemandMapper.selectPage(page, wrapper);

        List<CustomListVO> list = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return PageResult.of(list, result.getTotal(), request.getPage(), request.getPageSize());
    }

    /**
     * 获取定制需求详情
     */
    public CustomDetailVO getDetail(Long id) {
        CustomDemand demand = customDemandMapper.selectById(id);
        if (demand == null) {
            throw new BizException("需求不存在");
        }

        CustomDetailVO vo = toDetailVO(demand);

        // 获取跟进记录
        LambdaQueryWrapper<CustomFollowRecord> wrapper = new LambdaQueryWrapper<CustomFollowRecord>()
                .eq(CustomFollowRecord::getDemandId, id)
                .orderByDesc(CustomFollowRecord::getCreatedAt);
        List<CustomFollowRecord> records = customFollowRecordMapper.selectList(wrapper);

        List<FollowRecordVO> followRecords = records.stream()
                .map(this::toFollowRecordVO)
                .collect(Collectors.toList());
        vo.setFollowRecords(followRecords);

        return vo;
    }

    /**
     * 更新需求状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, CustomStatusRequest request) {
        CustomDemand demand = customDemandMapper.selectById(id);
        if (demand == null) {
            throw new BizException("需求不存在");
        }

        // 更新状态
        LambdaUpdateWrapper<CustomDemand> wrapper = new LambdaUpdateWrapper<CustomDemand>()
                .eq(CustomDemand::getId, id)
                .set(CustomDemand::getStatus, request.getStatus())
                .set(CustomDemand::getUpdatedAt, LocalDateTime.now());
        customDemandMapper.update(null, wrapper);

        // 如果是关闭，添加跟进记录
        if (request.getStatus() == 3 && StringUtils.hasText(request.getRemark())) {
            addFollowRecordInternal(id, "关闭原因：" + request.getRemark());
        }

        // 如果是开始跟进，添加系统跟进记录
        if (request.getStatus() == 1) {
            addFollowRecordInternal(id, "开始跟进此需求");
        }

        // 如果是完成，添加系统跟进记录
        if (request.getStatus() == 2) {
            addFollowRecordInternal(id, "需求已完成");
        }
    }

    /**
     * 添加跟进记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void addFollowRecord(Long id, CustomFollowRequest request) {
        CustomDemand demand = customDemandMapper.selectById(id);
        if (demand == null) {
            throw new BizException("需求不存在");
        }

        addFollowRecordInternal(id, request.getContent());
    }

    /**
     * 内部添加跟进记录
     */
    private void addFollowRecordInternal(Long demandId, String content) {
        CustomFollowRecord record = new CustomFollowRecord();
        record.setDemandId(demandId);
        record.setContent(content);

        // 获取当前操作人信息
        Object loginId = StpUtil.getLoginId();
        if (loginId != null) {
            record.setOperatorId(Long.parseLong(loginId.toString()));
            // 从session获取用户名，如果没有则用ID
            String username = (String) StpUtil.getSession().get("username");
            record.setOperatorName(username != null ? username : loginId.toString());
        }

        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        customFollowRecordMapper.insert(record);
    }

    /**
     * 转换为列表VO
     */
    private CustomListVO toListVO(CustomDemand demand) {
        CustomListVO vo = new CustomListVO();
        vo.setId(demand.getId());
        vo.setUserId(demand.getUserId());
        vo.setPhone(demand.getPhone());
        vo.setDestination(demand.getDestination());
        vo.setStartDate(demand.getTravelDateStart());
        vo.setDays(demand.getTravelDays());
        vo.setAdultCount(demand.getAdultCount());
        vo.setChildCount(demand.getChildCount());
        vo.setBudget(demand.getBudget());
        vo.setStatus(demand.getStatus());
        vo.setStatusDesc(STATUS_MAP.getOrDefault(demand.getStatus(), "未知"));
        vo.setCreatedAt(demand.getCreatedAt());
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private CustomDetailVO toDetailVO(CustomDemand demand) {
        CustomDetailVO vo = new CustomDetailVO();
        vo.setId(demand.getId());
        vo.setUserId(demand.getUserId());
        vo.setPhone(demand.getPhone());
        vo.setDestination(demand.getDestination());
        vo.setStartDate(demand.getTravelDateStart());
        vo.setEndDate(demand.getTravelDateEnd());
        vo.setDateType(demand.getTravelDateType());
        vo.setDays(demand.getTravelDays());
        vo.setAdultCount(demand.getAdultCount());
        vo.setChildCount(demand.getChildCount());
        vo.setBudget(demand.getBudget());
        vo.setRequirements(demand.getRequirements());
        vo.setRequirementsText(demand.getRequirementsText());
        vo.setStatus(demand.getStatus());
        vo.setStatusDesc(STATUS_MAP.getOrDefault(demand.getStatus(), "未知"));
        vo.setCreatedAt(demand.getCreatedAt());
        vo.setUpdatedAt(demand.getUpdatedAt());
        return vo;
    }

    /**
     * 转换为跟进记录VO
     */
    private FollowRecordVO toFollowRecordVO(CustomFollowRecord record) {
        FollowRecordVO vo = new FollowRecordVO();
        vo.setId(record.getId());
        vo.setContent(record.getContent());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
