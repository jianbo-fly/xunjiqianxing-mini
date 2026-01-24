package com.xunjiqianxing.service.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.base.PageQuery;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.custom.entity.CustomDemand;
import com.xunjiqianxing.service.custom.mapper.CustomDemandMapper;
import com.xunjiqianxing.service.custom.service.CustomDemandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 定制需求服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomDemandServiceImpl implements CustomDemandService {

    private final CustomDemandMapper customDemandMapper;

    @Override
    public CustomDemand submit(CustomDemand demand) {
        demand.setStatus(0); // 待处理
        customDemandMapper.insert(demand);
        log.info("定制需求提交成功: id={}, userId={}", demand.getId(), demand.getUserId());
        return demand;
    }

    @Override
    public PageResult<CustomDemand> pageUserDemands(Long userId, PageQuery pageQuery) {
        Page<CustomDemand> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());

        Page<CustomDemand> result = customDemandMapper.selectPage(page,
                new LambdaQueryWrapper<CustomDemand>()
                        .eq(CustomDemand::getUserId, userId)
                        .orderByDesc(CustomDemand::getCreatedAt)
        );

        return PageResult.of(result.getRecords(), result.getTotal(),
                pageQuery.getPage(), pageQuery.getPageSize());
    }

    @Override
    public CustomDemand getById(Long id) {
        return customDemandMapper.selectById(id);
    }

    @Override
    public boolean cancel(Long id, Long userId) {
        CustomDemand demand = getById(id);
        if (demand == null) {
            throw new BizException("需求不存在");
        }
        if (!demand.getUserId().equals(userId)) {
            throw new BizException("无权操作");
        }
        if (demand.getStatus() != 0) {
            throw new BizException("当前状态不允许取消");
        }

        int rows = customDemandMapper.update(null,
                new LambdaUpdateWrapper<CustomDemand>()
                        .eq(CustomDemand::getId, id)
                        .eq(CustomDemand::getStatus, 0)
                        .set(CustomDemand::getStatus, 3)
        );
        return rows > 0;
    }
}
