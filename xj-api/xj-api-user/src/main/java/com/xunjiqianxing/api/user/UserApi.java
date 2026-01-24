package com.xunjiqianxing.api.user;

import com.xunjiqianxing.api.user.dto.UserDTO;

/**
 * 用户服务接口
 * 供其他模块调用
 */
public interface UserApi {

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUser(Long userId);

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsUser(Long userId);

    /**
     * 扣减用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @return 是否成功
     */
    boolean deductPoints(Long userId, Integer points);

    /**
     * 增加用户积分
     *
     * @param userId 用户ID
     * @param points 积分数量
     * @return 是否成功
     */
    boolean addPoints(Long userId, Integer points);

    /**
     * 检查用户是否为会员
     *
     * @param userId 用户ID
     * @return 是否会员
     */
    boolean isMember(Long userId);

    /**
     * 更新用户会员状态
     *
     * @param userId    用户ID
     * @param isMember  是否会员
     * @param expireAt  过期时间
     * @return 是否成功
     */
    boolean updateMemberStatus(Long userId, boolean isMember, java.time.LocalDateTime expireAt);

    /**
     * 绑定推广员
     *
     * @param userId     用户ID
     * @param promoterId 推广员ID
     * @return 是否成功
     */
    boolean bindPromoter(Long userId, Long promoterId);

    /**
     * 解绑推广员
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unbindPromoter(Long userId);
}
