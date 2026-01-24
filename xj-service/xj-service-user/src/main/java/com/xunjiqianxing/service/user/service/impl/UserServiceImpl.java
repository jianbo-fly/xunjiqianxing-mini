package com.xunjiqianxing.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.ResultCode;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.mapper.UserInfoMapper;
import com.xunjiqianxing.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;

    @Override
    public UserInfo getById(Long id) {
        return userInfoMapper.selectById(id);
    }

    @Override
    public UserInfo getByOpenid(String openid) {
        return userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getOpenid, openid)
        );
    }

    @Override
    public UserInfo getByPhone(String phone) {
        return userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getPhone, phone)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo create(UserInfo user) {
        user.setPoints(0);
        user.setIsMember(0);
        user.setIsLeader(0);
        user.setIsPromoter(0);
        user.setStatus(1);
        userInfoMapper.insert(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(UserInfo user) {
        return userInfoMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, Integer points) {
        if (points <= 0) {
            throw new BizException("积分数量必须大于0");
        }
        int rows = userInfoMapper.update(null,
                new LambdaUpdateWrapper<UserInfo>()
                        .eq(UserInfo::getId, userId)
                        .setSql("points = points + " + points)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductPoints(Long userId, Integer points) {
        if (points <= 0) {
            throw new BizException("积分数量必须大于0");
        }
        int rows = userInfoMapper.update(null,
                new LambdaUpdateWrapper<UserInfo>()
                        .eq(UserInfo::getId, userId)
                        .ge(UserInfo::getPoints, points)
                        .setSql("points = points - " + points)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberStatus(Long userId, boolean isMember, LocalDateTime expireAt) {
        int rows = userInfoMapper.update(null,
                new LambdaUpdateWrapper<UserInfo>()
                        .eq(UserInfo::getId, userId)
                        .set(UserInfo::getIsMember, isMember ? 1 : 0)
                        .set(UserInfo::getMemberExpireAt, expireAt)
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindPromoter(Long userId, Long promoterId) {
        UserInfo user = userInfoMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        // 已绑定则不覆盖
        if (user.getBindpromoterId() != null) {
            log.info("用户{}已绑定推广员{}, 不覆盖", userId, user.getBindpromoterId());
            return false;
        }

        int rows = userInfoMapper.update(null,
                new LambdaUpdateWrapper<UserInfo>()
                        .eq(UserInfo::getId, userId)
                        .isNull(UserInfo::getBindpromoterId)
                        .set(UserInfo::getBindpromoterId, promoterId)
                        .set(UserInfo::getBindpromoterAt, LocalDateTime.now())
        );
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindPromoter(Long userId) {
        int rows = userInfoMapper.update(null,
                new LambdaUpdateWrapper<UserInfo>()
                        .eq(UserInfo::getId, userId)
                        .set(UserInfo::getBindpromoterId, null)
                        .set(UserInfo::getBindpromoterAt, null)
        );
        return rows > 0;
    }
}
