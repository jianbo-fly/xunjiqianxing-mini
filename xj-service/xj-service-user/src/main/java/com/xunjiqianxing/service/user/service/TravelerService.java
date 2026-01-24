package com.xunjiqianxing.service.user.service;

import com.xunjiqianxing.service.user.entity.UserTraveler;

import java.util.List;

/**
 * 出行人服务
 */
public interface TravelerService {

    /**
     * 获取用户的所有出行人
     */
    List<UserTraveler> listByUserId(Long userId);

    /**
     * 获取出行人详情
     */
    UserTraveler getById(Long id);

    /**
     * 添加出行人
     */
    UserTraveler add(UserTraveler traveler);

    /**
     * 更新出行人
     */
    boolean update(UserTraveler traveler);

    /**
     * 删除出行人
     */
    boolean delete(Long userId, Long travelerId);

    /**
     * 设置默认出行人
     */
    boolean setDefault(Long userId, Long travelerId);

    /**
     * 获取默认出行人
     */
    UserTraveler getDefault(Long userId);
}
