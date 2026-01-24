package com.xunjiqianxing.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.service.user.entity.UserTraveler;
import com.xunjiqianxing.service.user.mapper.UserTravelerMapper;
import com.xunjiqianxing.service.user.service.TravelerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 出行人服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TravelerServiceImpl implements TravelerService {

    private final UserTravelerMapper travelerMapper;

    @Override
    public List<UserTraveler> listByUserId(Long userId) {
        return travelerMapper.selectList(
                new LambdaQueryWrapper<UserTraveler>()
                        .eq(UserTraveler::getUserId, userId)
                        .eq(UserTraveler::getIsDeleted, 0)
                        .orderByDesc(UserTraveler::getIsDefault)
                        .orderByDesc(UserTraveler::getCreatedAt)
        );
    }

    @Override
    public UserTraveler getById(Long id) {
        return travelerMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserTraveler add(UserTraveler traveler) {
        // 检查是否超过最大数量
        Long count = travelerMapper.selectCount(
                new LambdaQueryWrapper<UserTraveler>()
                        .eq(UserTraveler::getUserId, traveler.getUserId())
                        .eq(UserTraveler::getIsDeleted, 0)
        );
        if (count >= 20) {
            throw new BizException("最多添加20个出行人");
        }

        // 如果是第一个，设为默认
        if (count == 0) {
            traveler.setIsDefault(1);
        } else if (traveler.getIsDefault() == null) {
            traveler.setIsDefault(0);
        }

        // 如果设为默认，取消其他默认
        if (traveler.getIsDefault() != null && traveler.getIsDefault() == 1) {
            cancelOtherDefault(traveler.getUserId(), null);
        }

        travelerMapper.insert(traveler);
        return traveler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(UserTraveler traveler) {
        UserTraveler existing = travelerMapper.selectById(traveler.getId());
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BizException("出行人不存在");
        }
        if (!existing.getUserId().equals(traveler.getUserId())) {
            throw new BizException("无权限修改");
        }

        // 如果设为默认，取消其他默认
        if (traveler.getIsDefault() != null && traveler.getIsDefault() == 1) {
            cancelOtherDefault(traveler.getUserId(), traveler.getId());
        }

        return travelerMapper.updateById(traveler) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long userId, Long travelerId) {
        UserTraveler existing = travelerMapper.selectById(travelerId);
        if (existing == null || existing.getIsDeleted() == 1) {
            return true;
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BizException("无权限删除");
        }

        int rows = travelerMapper.update(null,
                new LambdaUpdateWrapper<UserTraveler>()
                        .eq(UserTraveler::getId, travelerId)
                        .set(UserTraveler::getIsDeleted, 1)
        );

        // 如果删除的是默认出行人，设置另一个为默认
        if (rows > 0 && existing.getIsDefault() == 1) {
            UserTraveler another = travelerMapper.selectOne(
                    new LambdaQueryWrapper<UserTraveler>()
                            .eq(UserTraveler::getUserId, userId)
                            .eq(UserTraveler::getIsDeleted, 0)
                            .orderByDesc(UserTraveler::getCreatedAt)
                            .last("LIMIT 1")
            );
            if (another != null) {
                travelerMapper.update(null,
                        new LambdaUpdateWrapper<UserTraveler>()
                                .eq(UserTraveler::getId, another.getId())
                                .set(UserTraveler::getIsDefault, 1)
                );
            }
        }

        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long userId, Long travelerId) {
        UserTraveler existing = travelerMapper.selectById(travelerId);
        if (existing == null || existing.getIsDeleted() == 1) {
            throw new BizException("出行人不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BizException("无权限操作");
        }

        // 取消其他默认
        cancelOtherDefault(userId, travelerId);

        // 设为默认
        return travelerMapper.update(null,
                new LambdaUpdateWrapper<UserTraveler>()
                        .eq(UserTraveler::getId, travelerId)
                        .set(UserTraveler::getIsDefault, 1)
        ) > 0;
    }

    @Override
    public UserTraveler getDefault(Long userId) {
        return travelerMapper.selectOne(
                new LambdaQueryWrapper<UserTraveler>()
                        .eq(UserTraveler::getUserId, userId)
                        .eq(UserTraveler::getIsDefault, 1)
                        .eq(UserTraveler::getIsDeleted, 0)
        );
    }

    private void cancelOtherDefault(Long userId, Long excludeId) {
        LambdaUpdateWrapper<UserTraveler> wrapper = new LambdaUpdateWrapper<UserTraveler>()
                .eq(UserTraveler::getUserId, userId)
                .eq(UserTraveler::getIsDefault, 1)
                .set(UserTraveler::getIsDefault, 0);
        if (excludeId != null) {
            wrapper.ne(UserTraveler::getId, excludeId);
        }
        travelerMapper.update(null, wrapper);
    }
}
