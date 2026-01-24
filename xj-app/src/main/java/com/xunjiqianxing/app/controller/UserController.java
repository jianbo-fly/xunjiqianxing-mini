package com.xunjiqianxing.app.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.dev33.satoken.stp.StpUtil;
import com.xunjiqianxing.app.dto.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "用户登录、信息管理等")
public class UserController {

    private final WxMaService wxMaService;
    private final UserService userService;

    /**
     * 微信小程序登录
     */
    @PostMapping("/loginByCode")
    @Operation(summary = "微信小程序登录", description = "通过微信code换取登录态")
    public Result<LoginResponse> loginByCode(@Valid @RequestBody WxLoginRequest request) {
        try {
            // 调用微信接口获取openid
            WxMaJscode2SessionResult session = wxMaService.getUserService()
                    . getSessionInfo(request.getCode());

            String openid = session.getOpenid();
            String unionid = session.getUnionid();

            log.info("微信登录成功, openid: {}", openid);

            // 查找或创建用户
            UserInfo user = userService.getByOpenid(openid);
            boolean isNewUser = false;

            if (user == null) {
                // 新用户，创建账号
                user = new UserInfo();
                user.setOpenid(openid);
                user.setUnionid(unionid);
                user.setNickname("微信用户");
                user.setLastLoginAt(LocalDateTime.now());
                user = userService.create(user);
                isNewUser = true;
                log.info("创建新用户, userId: {}", user.getId());
            } else {
                // 更新最后登录时间
                user.setLastLoginAt(LocalDateTime.now());
                userService.update(user);
            }

            // 生成token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();

            return Result.success(LoginResponse.builder()
                    .userId(user.getId())
                    .token(token)
                    .isNewUser(isNewUser)
                    .build());

        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            throw new BizException("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setIsMember(user.getIsMember() != null && user.getIsMember() == 1);
        vo.setIsLeader(user.getIsLeader() != null && user.getIsLeader() == 1);
        vo.setIsPromoter(user.getIsPromoter() != null && user.getIsPromoter() == 1);

        return Result.success(vo);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新昵称、头像等")
    public Result<Void> updateUserInfo(@Valid @RequestBody UpdateUserRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        userService.update(user);
        return Result.success();
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
