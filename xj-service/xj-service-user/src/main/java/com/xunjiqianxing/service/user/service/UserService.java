package com.xunjiqianxing.service.user.service;

import com.xunjiqianxing.service.user.entity.UserInfo;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 根据ID获取用户
     */
    UserInfo getById(Long id);

    /**
     * 根据openid获取用户
     */
    UserInfo getByOpenid(String openid);

    /**
     * 根据手机号获取用户
     */
    UserInfo getByPhone(String phone);

    /**
     * 创建用户
     */
    UserInfo create(UserInfo user);

    /**
     * 更新用户
     */
    boolean update(UserInfo user);

    /**
     * 增加积分
     */
    boolean addPoints(Long userId, Integer points);

    /**
     * 扣减积分
     */
    boolean deductPoints(Long userId, Integer points);

    /**
     * 更新会员状态
     */
    boolean updateMemberStatus(Long userId, boolean isMember, java.time.LocalDateTime expireAt);

    /**
     * 绑定推广员
     */
    boolean bindPromoter(Long userId, Long promoterId);

    /**
     * 解绑推广员
     */
    boolean unbindPromoter(Long userId);
}
