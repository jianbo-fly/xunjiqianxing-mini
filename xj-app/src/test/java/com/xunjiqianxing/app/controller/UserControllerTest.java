package com.xunjiqianxing.app.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunjiqianxing.app.dto.*;
import com.xunjiqianxing.app.service.SmsService;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.exception.GlobalExceptionHandler;
import com.xunjiqianxing.service.user.entity.UserInfo;
import com.xunjiqianxing.service.user.service.UserService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.error.WxError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试（#33 ~ #49, #256 ~ #267）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    @Mock private WxMaService wxMaService;
    @Mock private WxMaUserService wxMaUserService;
    @Mock private UserService userService;

    private StubSmsService smsService;
    private UserController userController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockedStatic<StpUtil> stpMock;

    private static final Long USER_ID = 10001L;

    // ==================== SmsService 桩（Java25 + Mockito 无法 mock 具体类） ====================
    static class StubSmsService extends SmsService {
        String throwSend = null;
        String throwVerify = null;
        List<String> sentPhones = new ArrayList<>();
        List<String[]> verifyCalls = new ArrayList<>();

        StubSmsService() { super(null); }

        @Override
        public void sendVerifyCode(String phone) {
            sentPhones.add(phone);
            if (throwSend != null) throw new BizException(throwSend);
        }

        @Override
        public void verifyCode(String phone, String code) {
            verifyCalls.add(new String[]{phone, code});
            if (throwVerify != null) throw new BizException(throwVerify);
        }
    }

    @BeforeEach
    void setUp() {
        smsService = new StubSmsService();
        userController = new UserController(wxMaService, userService, smsService);

        objectMapper = new ObjectMapper();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setMessageConverters(converter)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        when(wxMaService.getUserService()).thenReturn(wxMaUserService);

        stpMock = Mockito.mockStatic(StpUtil.class);
        stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
        stpMock.when(StpUtil::getTokenValue).thenReturn("test-token");
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    // ==================== 辅助方法 ====================

    private WxMaJscode2SessionResult buildSession(String openid) {
        WxMaJscode2SessionResult s = new WxMaJscode2SessionResult();
        s.setOpenid(openid);
        s.setUnionid("union-" + openid);
        return s;
    }

    private WxMaPhoneNumberInfo buildPhoneInfo(String phone) {
        WxMaPhoneNumberInfo info = new WxMaPhoneNumberInfo();
        info.setPhoneNumber(phone);
        return info;
    }

    private UserInfo buildUser(Long id, String phone) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setOpenid("openid-" + id);
        u.setNickname("张三");
        u.setAvatar("http://cdn.example.com/a.png");
        u.setPhone(phone);
        return u;
    }

    private WxLoginRequest wxLoginReq(String code) {
        WxLoginRequest r = new WxLoginRequest();
        r.setCode(code);
        return r;
    }

    // ==================== loginByCode #33-37 ====================

    @Test
    @DisplayName("#33 loginByCode 新用户：创建账号并返回 isNewUser=true")
    void loginByCode_newUser() throws Exception {
        when(wxMaUserService.getSessionInfo("code-1")).thenReturn(buildSession("openid-A"));
        when(userService.getByOpenid("openid-A")).thenReturn(null);
        UserInfo created = buildUser(1L, null);
        created.setNickname("微信用户");
        created.setAvatar(null);
        when(userService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/user/loginByCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wxLoginReq("code-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.needsProfile").value(true))
                .andExpect(jsonPath("$.data.hasPhone").value(false))
                .andExpect(jsonPath("$.data.userId").value(1));

        stpMock.verify(() -> StpUtil.login(1L));
    }

    @Test
    @DisplayName("#34 loginByCode 老用户：更新登录时间")
    void loginByCode_oldUser() throws Exception {
        when(wxMaUserService.getSessionInfo("code-2")).thenReturn(buildSession("openid-B"));
        when(userService.getByOpenid("openid-B")).thenReturn(buildUser(2L, "13800138000"));

        mockMvc.perform(post("/api/user/loginByCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wxLoginReq("code-2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andExpect(jsonPath("$.data.hasPhone").value(true))
                .andExpect(jsonPath("$.data.needsProfile").value(false));

        verify(userService).update(any());
    }

    @Test
    @DisplayName("#35 loginByCode 头像为空 → needsProfile=true")
    void loginByCode_needsProfileWhenAvatarEmpty() throws Exception {
        when(wxMaUserService.getSessionInfo(any())).thenReturn(buildSession("openid-C"));
        UserInfo u = buildUser(3L, "13800138000");
        u.setAvatar("");
        when(userService.getByOpenid("openid-C")).thenReturn(u);

        mockMvc.perform(post("/api/user/loginByCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wxLoginReq("code-3"))))
                .andExpect(jsonPath("$.data.needsProfile").value(true));
    }

    @Test
    @DisplayName("#36 loginByCode 微信 API 抛异常")
    void loginByCode_wxError() throws Exception {
        WxError err = WxError.builder().errorCode(40029).errorMsg("invalid code").build();
        when(wxMaUserService.getSessionInfo(any())).thenThrow(new WxErrorException(err));

        mockMvc.perform(post("/api/user/loginByCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wxLoginReq("bad-code"))))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("微信登录失败")));
    }

    @Test
    @DisplayName("#37 loginByCode 参数校验：code 为空")
    void loginByCode_blankCode() throws Exception {
        mockMvc.perform(post("/api/user/loginByCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== loginByWxAll #38-42 ====================

    @Test
    @DisplayName("#38 loginByWxAll 新用户：同时获取 openid + phone")
    void loginByWxAll_newUser() throws Exception {
        when(wxMaUserService.getSessionInfo("login-code")).thenReturn(buildSession("openid-D"));
        when(wxMaUserService.getPhoneNoInfo("phone-code")).thenReturn(buildPhoneInfo("13800138000"));
        when(userService.getByOpenid("openid-D")).thenReturn(null);
        when(userService.getByPhone("13800138000")).thenReturn(null);
        UserInfo created = buildUser(10L, "13800138000");
        created.setNickname("微信用户");
        created.setAvatar(null);
        when(userService.create(any())).thenReturn(created);

        WxLoginWithPhoneRequest req = new WxLoginWithPhoneRequest();
        req.setLoginCode("login-code");
        req.setPhoneCode("phone-code");

        mockMvc.perform(post("/api/user/loginByWxAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasPhone").value(true))
                .andExpect(jsonPath("$.data.isNewUser").value(true));
    }

    @Test
    @DisplayName("#39 loginByWxAll 新用户：手机号已被占用 → 拒绝")
    void loginByWxAll_phoneAlreadyBoundToOthers() throws Exception {
        when(wxMaUserService.getSessionInfo(any())).thenReturn(buildSession("openid-E"));
        when(wxMaUserService.getPhoneNoInfo(any())).thenReturn(buildPhoneInfo("13800138000"));
        when(userService.getByOpenid("openid-E")).thenReturn(null);
        when(userService.getByPhone("13800138000")).thenReturn(buildUser(99L, "13800138000"));

        WxLoginWithPhoneRequest req = new WxLoginWithPhoneRequest();
        req.setLoginCode("l");
        req.setPhoneCode("p");

        mockMvc.perform(post("/api/user/loginByWxAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已绑定其他账号")));

        verify(userService, never()).create(any());
    }

    @Test
    @DisplayName("#40 loginByWxAll 获取手机号为空 → 失败")
    void loginByWxAll_emptyPhone() throws Exception {
        when(wxMaUserService.getSessionInfo(any())).thenReturn(buildSession("openid-F"));
        when(wxMaUserService.getPhoneNoInfo(any())).thenReturn(buildPhoneInfo(""));

        WxLoginWithPhoneRequest req = new WxLoginWithPhoneRequest();
        req.setLoginCode("l");
        req.setPhoneCode("p");

        mockMvc.perform(post("/api/user/loginByWxAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("获取手机号失败")));
    }

    @Test
    @DisplayName("#41 loginByWxAll 老用户补齐手机号")
    void loginByWxAll_oldUserFillPhone() throws Exception {
        when(wxMaUserService.getSessionInfo(any())).thenReturn(buildSession("openid-G"));
        when(wxMaUserService.getPhoneNoInfo(any())).thenReturn(buildPhoneInfo("13800138001"));
        UserInfo user = buildUser(20L, null);
        when(userService.getByOpenid("openid-G")).thenReturn(user);
        when(userService.getByPhone("13800138001")).thenReturn(null);

        WxLoginWithPhoneRequest req = new WxLoginWithPhoneRequest();
        req.setLoginCode("l");
        req.setPhoneCode("p");

        mockMvc.perform(post("/api/user/loginByWxAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<UserInfo> cap = ArgumentCaptor.forClass(UserInfo.class);
        verify(userService).update(cap.capture());
        org.junit.jupiter.api.Assertions.assertEquals("13800138001", cap.getValue().getPhone());
    }

    @Test
    @DisplayName("#42 loginByWxAll 参数校验：loginCode 为空")
    void loginByWxAll_blankLoginCode() throws Exception {
        mockMvc.perform(post("/api/user/loginByWxAll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginCode\":\"\",\"phoneCode\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== phoneLogin #43-47 ====================

    @Test
    @DisplayName("#43 phoneLogin 新用户：创建账号")
    void phoneLogin_newUser() throws Exception {
        when(userService.getByPhone("13800138000")).thenReturn(null);
        UserInfo created = buildUser(30L, "13800138000");
        created.setNickname("手机用户");
        created.setAvatar(null);
        when(userService.create(any())).thenReturn(created);

        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13800138000");
        req.setCode("1234");

        mockMvc.perform(post("/api/user/phoneLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
                .andExpect(jsonPath("$.data.needsProfile").value(true));

        org.junit.jupiter.api.Assertions.assertEquals(1, smsService.verifyCalls.size());
    }

    @Test
    @DisplayName("#44 phoneLogin 老用户：正常登录")
    void phoneLogin_oldUser() throws Exception {
        when(userService.getByPhone("13800138000")).thenReturn(buildUser(31L, "13800138000"));

        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13800138000");
        req.setCode("1234");

        mockMvc.perform(post("/api/user/phoneLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.data.isNewUser").value(false))
                .andExpect(jsonPath("$.data.hasPhone").value(true));
    }

    @Test
    @DisplayName("#45 phoneLogin 验证码错误")
    void phoneLogin_invalidCode() throws Exception {
        smsService.throwVerify = "验证码错误";

        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13800138000");
        req.setCode("0000");

        mockMvc.perform(post("/api/user/phoneLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("验证码错误")));

        verify(userService, never()).getByPhone(any());
    }

    @Test
    @DisplayName("#46 phoneLogin 手机号格式非法")
    void phoneLogin_invalidPhone() throws Exception {
        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("12345");
        req.setCode("1234");

        mockMvc.perform(post("/api/user/phoneLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("#47 phoneLogin 验证码为空 → 参数校验失败")
    void phoneLogin_blankCode() throws Exception {
        PhoneLoginRequest req = new PhoneLoginRequest();
        req.setPhone("13800138000");
        req.setCode("");

        mockMvc.perform(post("/api/user/phoneLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==================== getUserInfo #48-49 ====================

    @Test
    @DisplayName("#48 getUserInfo 正常返回")
    void getUserInfo_success() throws Exception {
        UserInfo u = buildUser(USER_ID, "13800138000");
        u.setIsMember(1);
        u.setIsLeader(0);
        u.setIsPromoter(1);
        when(userService.getById(USER_ID)).thenReturn(u);

        mockMvc.perform(get("/api/user/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("张三"))
                .andExpect(jsonPath("$.data.isMember").value(true))
                .andExpect(jsonPath("$.data.isLeader").value(false))
                .andExpect(jsonPath("$.data.isPromoter").value(true));
    }

    @Test
    @DisplayName("#49 getUserInfo 用户不存在")
    void getUserInfo_notFound() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/user/info"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户不存在")));
    }

    // ==================== updateUserInfo #256-258 ====================

    @Test
    @DisplayName("#256 updateUserInfo 更新全部字段")
    void updateUserInfo_allFields() throws Exception {
        UserInfo u = buildUser(USER_ID, "13800138000");
        when(userService.getById(USER_ID)).thenReturn(u);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNickname("新名字");
        req.setAvatar("http://cdn.example.com/new.png");
        req.setGender(1);

        mockMvc.perform(put("/api/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<UserInfo> cap = ArgumentCaptor.forClass(UserInfo.class);
        verify(userService).update(cap.capture());
        org.junit.jupiter.api.Assertions.assertEquals("新名字", cap.getValue().getNickname());
        org.junit.jupiter.api.Assertions.assertEquals(1, cap.getValue().getGender());
    }

    @Test
    @DisplayName("#257 updateUserInfo 仅更新昵称")
    void updateUserInfo_partialUpdate() throws Exception {
        UserInfo u = buildUser(USER_ID, "13800138000");
        u.setGender(0);
        when(userService.getById(USER_ID)).thenReturn(u);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNickname("新名字");

        mockMvc.perform(put("/api/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<UserInfo> cap = ArgumentCaptor.forClass(UserInfo.class);
        verify(userService).update(cap.capture());
        org.junit.jupiter.api.Assertions.assertEquals("新名字", cap.getValue().getNickname());
        org.junit.jupiter.api.Assertions.assertEquals(0, cap.getValue().getGender());
    }

    @Test
    @DisplayName("#258 updateUserInfo 用户不存在")
    void updateUserInfo_userNotFound() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(null);

        mockMvc.perform(put("/api/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户不存在")));
    }

    // ==================== logout #259 ====================

    @Test
    @DisplayName("#259 logout 调用 StpUtil.logout")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        stpMock.verify(StpUtil::logout);
    }

    // ==================== bindPhoneByWx #260-262 ====================

    @Test
    @DisplayName("#260 bindPhoneByWx 正常绑定")
    void bindPhoneByWx_success() throws Exception {
        UserInfo u = buildUser(USER_ID, null);
        when(userService.getById(USER_ID)).thenReturn(u);
        when(wxMaUserService.getPhoneNoInfo("wx-code")).thenReturn(buildPhoneInfo("13800138000"));
        when(userService.getByPhone("13800138000")).thenReturn(null);

        BindPhoneByWxRequest req = new BindPhoneByWxRequest();
        req.setCode("wx-code");

        mockMvc.perform(post("/api/user/bindPhoneByWx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<UserInfo> cap = ArgumentCaptor.forClass(UserInfo.class);
        verify(userService).update(cap.capture());
        org.junit.jupiter.api.Assertions.assertEquals("13800138000", cap.getValue().getPhone());
    }

    @Test
    @DisplayName("#261 bindPhoneByWx 手机号已被其他账号绑定")
    void bindPhoneByWx_phoneTaken() throws Exception {
        UserInfo u = buildUser(USER_ID, null);
        when(userService.getById(USER_ID)).thenReturn(u);
        when(wxMaUserService.getPhoneNoInfo(any())).thenReturn(buildPhoneInfo("13800138000"));
        when(userService.getByPhone("13800138000")).thenReturn(buildUser(999L, "13800138000"));

        BindPhoneByWxRequest req = new BindPhoneByWxRequest();
        req.setCode("wx-code");

        mockMvc.perform(post("/api/user/bindPhoneByWx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已被其他账号绑定")));

        verify(userService, never()).update(any());
    }

    @Test
    @DisplayName("#262 bindPhoneByWx 微信返回空手机号")
    void bindPhoneByWx_emptyPhone() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(buildUser(USER_ID, null));
        when(wxMaUserService.getPhoneNoInfo(any())).thenReturn(buildPhoneInfo(""));

        BindPhoneByWxRequest req = new BindPhoneByWxRequest();
        req.setCode("wx-code");

        mockMvc.perform(post("/api/user/bindPhoneByWx")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("获取手机号失败")));
    }

    // ==================== bindPhone #263-265 ====================

    @Test
    @DisplayName("#263 bindPhone 正常绑定")
    void bindPhone_success() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(buildUser(USER_ID, null));
        when(userService.getByPhone("13800138000")).thenReturn(null);

        BindPhoneRequest req = new BindPhoneRequest();
        req.setPhone("13800138000");
        req.setCode("1234");

        mockMvc.perform(post("/api/user/bindPhone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(1, smsService.verifyCalls.size());
    }

    @Test
    @DisplayName("#264 bindPhone 验证码错误")
    void bindPhone_invalidCode() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(buildUser(USER_ID, null));
        smsService.throwVerify = "验证码错误";

        BindPhoneRequest req = new BindPhoneRequest();
        req.setPhone("13800138000");
        req.setCode("0000");

        mockMvc.perform(post("/api/user/bindPhone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("验证码错误")));

        verify(userService, never()).update(any());
    }

    @Test
    @DisplayName("#265 bindPhone 手机号已被绑定")
    void bindPhone_phoneTaken() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(buildUser(USER_ID, null));
        when(userService.getByPhone("13800138000")).thenReturn(buildUser(999L, "13800138000"));

        BindPhoneRequest req = new BindPhoneRequest();
        req.setPhone("13800138000");
        req.setCode("1234");

        mockMvc.perform(post("/api/user/bindPhone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已被其他账号绑定")));
    }

    // ==================== sendCode #266-267 ====================

    @Test
    @DisplayName("#266 sendCode 正常发送")
    void sendCode_success() throws Exception {
        SendCodeRequest req = new SendCodeRequest();
        req.setPhone("13800138000");

        mockMvc.perform(post("/api/user/sendCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(1, smsService.sentPhones.size());
        org.junit.jupiter.api.Assertions.assertEquals("13800138000", smsService.sentPhones.get(0));
    }

    @Test
    @DisplayName("#267 sendCode 发送过于频繁")
    void sendCode_tooFrequent() throws Exception {
        smsService.throwSend = "验证码发送过于频繁，请稍后再试";

        SendCodeRequest req = new SendCodeRequest();
        req.setPhone("13800138000");

        mockMvc.perform(post("/api/user/sendCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("过于频繁")));
    }
}
