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

            // 头像为空或是本地临时路径则需要完善资料
            String avatar = user.getAvatar();
            boolean needsProfile = avatar == null || avatar.isEmpty()
                    || avatar.startsWith("http://tmp")
                    || avatar.startsWith("http://127.")
                    || "微信用户".equals(user.getNickname());

            return Result.success(LoginResponse.builder()
                    .userId(user.getId())
                    .token(token)
                    .isNewUser(isNewUser)
                    .needsProfile(needsProfile)
                    .hasPhone(user.getPhone() != null && !user.getPhone().isEmpty())
                    .build());

        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            throw new BizException("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 微信一键登录 + 手机号授权
     * 前端按钮同时带 open-type="getPhoneNumber" 触发，一次完成登录与手机号绑定
     */
    @PostMapping("/loginByWxAll")
    @Operation(summary = "微信一键登录带手机号", description = "loginCode + phoneCode 同步登录并绑定手机号")
    public Result<LoginResponse> loginByWxAll(@Valid @RequestBody WxLoginWithPhoneRequest request) {
        try {
            // 1. 换 openid
            WxMaJscode2SessionResult session = wxMaService.getUserService()
                    .getSessionInfo(request.getLoginCode());
            String openid = session.getOpenid();
            String unionid = session.getUnionid();

            // 2. 换手机号
            String phoneNumber = wxMaService.getUserService()
                    .getPhoneNoInfo(request.getPhoneCode())
                    .getPhoneNumber();
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                throw new BizException("获取手机号失败");
            }

            // 3. 查/建用户
            UserInfo user = userService.getByOpenid(openid);
            boolean isNewUser = false;

            if (user == null) {
                // 同一手机号已被其他 openid 占用 → 直接拒绝，避免账号合并的复杂性
                UserInfo existByPhone = userService.getByPhone(phoneNumber);
                if (existByPhone != null) {
                    throw new BizException("该手机号已绑定其他账号,请更换手机号或使用原账号登录");
                }
                user = new UserInfo();
                user.setOpenid(openid);
                user.setUnionid(unionid);
                user.setPhone(phoneNumber);
                user.setNickname("微信用户");
                user.setLastLoginAt(LocalDateTime.now());
                user = userService.create(user);
                isNewUser = true;
                log.info("一键登录创建新用户, userId: {}, phone: {}", user.getId(), phoneNumber);
            } else {
                // 老用户：首次一键登录时补齐手机号；若已有且与本次不一致则拒绝
                String currentPhone = user.getPhone();
                if (currentPhone == null || currentPhone.isEmpty()) {
                    UserInfo existByPhone = userService.getByPhone(phoneNumber);
                    if (existByPhone != null && !existByPhone.getId().equals(user.getId())) {
                        throw new BizException("该手机号已绑定其他账号");
                    }
                    user.setPhone(phoneNumber);
                }
                user.setLastLoginAt(LocalDateTime.now());
                userService.update(user);
                log.info("一键登录成功, userId: {}, phone: {}", user.getId(), phoneNumber);
            }

            // 4. 发 token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();

            String avatar = user.getAvatar();
            boolean needsProfile = avatar == null || avatar.isEmpty()
                    || avatar.startsWith("http://tmp")
                    || avatar.startsWith("http://127.")
                    || "微信用户".equals(user.getNickname());

            return Result.success(LoginResponse.builder()
                    .userId(user.getId())
                    .token(token)
                    .isNewUser(isNewUser)
                    .needsProfile(needsProfile)
                    .hasPhone(true)
                    .build());

        } catch (WxErrorException e) {
            log.error("一键登录失败: {}", e.getMessage(), e);
            throw new BizException("一键登录失败: " + e.getMessage());
        }
    }

    /**
     * 手机号验证码登录
     * 未登录态下调用：手机号存在则直接登录，不存在则创建新账号
     */
    @PostMapping("/phoneLogin")
    @Operation(summary = "手机号验证码登录", description = "验证码登录，用户不存在时自动创建账号")
    public Result<LoginResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        // TODO: 验证短信验证码
        // smsService.verifyCode(request.getPhone(), request.getCode());

        String phone = request.getPhone();
        UserInfo user = userService.getByPhone(phone);
        boolean isNewUser = false;

        if (user == null) {
            // 新用户：创建账号，openid 使用占位值，后续可由 bindPhoneByWx 或单独接口补全
            user = new UserInfo();
            user.setOpenid("phone_" + phone);
            user.setPhone(phone);
            user.setNickname("手机用户");
            user.setLastLoginAt(LocalDateTime.now());
            user = userService.create(user);
            isNewUser = true;
            log.info("手机号登录创建新用户, userId: {}, phone: {}", user.getId(), phone);
        } else {
            user.setLastLoginAt(LocalDateTime.now());
            userService.update(user);
            log.info("手机号登录成功, userId: {}, phone: {}", user.getId(), phone);
        }

        // 生成token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        String avatar = user.getAvatar();
        boolean needsProfile = avatar == null || avatar.isEmpty()
                || avatar.startsWith("http://tmp")
                || avatar.startsWith("http://127.")
                || "手机用户".equals(user.getNickname())
                || "微信用户".equals(user.getNickname());

        return Result.success(LoginResponse.builder()
                .userId(user.getId())
                .token(token)
                .isNewUser(isNewUser)
                .needsProfile(needsProfile)
                .hasPhone(true)
                .build());
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

    /**
     * 微信一键绑定手机号
     */
    @PostMapping("/bindPhoneByWx")
    @Operation(summary = "微信一键绑定手机号", description = "通过微信getPhoneNumber获取手机号")
    public Result<Void> bindPhoneByWx(@Valid @RequestBody BindPhoneByWxRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        try {
            // 通过code获取手机号
            var phoneInfo = wxMaService.getUserService().getPhoneNoInfo(request.getCode());
            String phoneNumber = phoneInfo.getPhoneNumber();

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                throw new BizException("获取手机号失败");
            }

            // 检查手机号是否已被绑定
            UserInfo existUser = userService.getByPhone(phoneNumber);
            if (existUser != null && !existUser.getId().equals(userId)) {
                throw new BizException("该手机号已被其他账号绑定");
            }

            // 更新手机号
            user.setPhone(phoneNumber);
            userService.update(user);

            log.info("用户绑定手机号成功, userId: {}, phone: {}", userId, phoneNumber);
            return Result.success();
        } catch (WxErrorException e) {
            log.error("获取微信手机号失败: {}", e.getMessage(), e);
            throw new BizException("获取手机号失败: " + e.getMessage());
        }
    }

    /**
     * 验证码绑定手机号
     */
    @PostMapping("/bindPhone")
    @Operation(summary = "验证码绑定手机号")
    public Result<Void> bindPhone(@Valid @RequestBody BindPhoneRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo user = userService.getById(userId);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        // TODO: 验证短信验证码
        // smsService.verifyCode(request.getPhone(), request.getCode());

        // 检查手机号是否已被绑定
        UserInfo existUser = userService.getByPhone(request.getPhone());
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new BizException("该手机号已被其他账号绑定");
        }

        // 更新手机号
        user.setPhone(request.getPhone());
        userService.update(user);

        log.info("用户绑定手机号成功, userId: {}, phone: {}", userId, request.getPhone());
        return Result.success();
    }

    /**
     * 发送验证码
     */
    @PostMapping("/sendCode")
    @Operation(summary = "发送短信验证码")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        // TODO: 实现短信发送
        // smsService.sendVerifyCode(request.getPhone());
        log.info("发送验证码到手机号: {}", request.getPhone());
        return Result.success();
    }
}
