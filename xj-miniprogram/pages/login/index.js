/**
 * 登录页面
 * 流程：
 *   1. 微信一键登录 → 老用户已有手机号直接成功；新用户/未绑手机号跳 step 2 绑定
 *   2. 手机号验证码登录 → 跳 step 2，走 phoneLogin 接口建号
 *
 * 备注：后端已预留 /api/user/loginByWxAll 接口，待小程序账号获得
 *      getPhoneNumber 权限后，可改按钮 open-type 启用一键登录带手机号能力。
 */
const app = getApp();
const userApi = require('../../services/user');
const { redirectAfterLogin } = require('../../utils/auth');

Page({
  data: {
    // 当前步骤：1-欢迎页 2-手机号验证码（降级路径）
    step: 1,
    // 是否同意协议
    agreed: false,
    // 一键登录中
    logging: false,
    // 是否从手机号登录入口进入（未登录态，提交走 phoneLogin）
    fromPhoneEntry: false,
    // 微信登录拿到但未持久化的 token（等手机号绑定成功后才真正落盘）
    // 放 data 只是为了方便 onUnload 清理，不会参与模板渲染
    pendingToken: '',
    // 手机号
    phone: '',
    // 验证码
    verifyCode: '',
    // 倒计时
    countdown: 0,
    // 提交中
    submitting: false,
  },

  onLoad(options) {
    if (options.redirect) {
      wx.setStorageSync('redirectUrl', decodeURIComponent(options.redirect));
    }
  },

  handleAgreeChange() {
    this.setData({ agreed: !this.data.agreed });
  },

  handleViewAgreement() {
    wx.navigateTo({ url: '/pages/webview/index?type=user-agreement' });
  },

  handleViewPrivacy() {
    wx.navigateTo({ url: '/pages/webview/index?type=privacy-policy' });
  },

  /**
   * 手机号验证码登录入口（未登录态走 phoneLogin）
   */
  handlePhoneLogin() {
    if (!this.data.agreed) {
      wx.showToast({ title: '请先同意用户协议', icon: 'none' });
      return;
    }
    this.setData({ step: 2, fromPhoneEntry: true });
  },

  /**
   * wx.login Promise 封装
   */
  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res.code) {
            resolve(res);
          } else {
            wx.showToast({ title: '微信登录失败', icon: 'none' });
            reject(new Error('wx.login failed'));
          }
        },
        fail: reject,
      });
    });
  },

  /**
   * 微信一键登录
   * - 老用户已绑手机号：直接持久化 token 并登录成功
   * - 新用户 / 未绑手机号：token 暂存 pendingToken,跳 step 2 强制绑定;
   *                        只有 bindPhone 成功后才真正落盘登录
   */
  async handleWxLogin() {
    if (!this.data.agreed) {
      wx.showToast({ title: '请先同意用户协议', icon: 'none' });
      return;
    }
    if (this.data.logging) return;
    this.setData({ logging: true });

    try {
      const { code } = await this.wxLogin();
      const res = await userApi.wxLogin({ code });

      if (res.hasPhone) {
        // 老用户:持久化 token,直接成功
        app.setLoginInfo(res.token);
        await this.loadUserInfo();
        this.loginSuccess();
      } else {
        // 新用户:token 只放 data,不落盘,强制进入手机号绑定
        this.setData({
          step: 2,
          fromPhoneEntry: false,
          pendingToken: res.token,
          logging: false,
        });
      }
    } catch (err) {
      console.error('登录失败', err);
      this.setData({ logging: false });
    }
  },

  /**
   * 加载用户信息
   */
  async loadUserInfo() {
    try {
      const userInfo = await userApi.getInfo();
      app.globalData.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);
    } catch (e) {
      console.error('获取用户信息失败', e);
    }
  },

  handlePhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  handleCodeInput(e) {
    this.setData({ verifyCode: e.detail.value });
  },

  /**
   * 发送验证码
   */
  async handleSendCode() {
    const { phone, countdown } = this.data;

    if (countdown > 0) return;

    if (!phone || !/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    try {
      await userApi.sendVerifyCode({ phone });
      wx.showToast({ title: '验证码已发送', icon: 'success' });

      this.setData({ countdown: 60 });
      this.countdownTimer = setInterval(() => {
        if (this.data.countdown <= 1) {
          clearInterval(this.countdownTimer);
          this.setData({ countdown: 0 });
        } else {
          this.setData({ countdown: this.data.countdown - 1 });
        }
      }, 1000);
    } catch (e) {
      console.error('发送验证码失败', e);
    }
  },

  /**
   * 手机号验证码提交
   * - fromPhoneEntry=true（未登录态）：走 phoneLogin 接口,成功后持久化 token
   * - fromPhoneEntry=false（微信登录后，token 未落盘）：走 bindPhone 接口,
   *     用 pendingToken 通过 header 临时鉴权,成功后才真正 setLoginInfo 落盘
   */
  async handleBindPhone() {
    const { phone, verifyCode, fromPhoneEntry, pendingToken } = this.data;

    if (!phone || !/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    if (!verifyCode || verifyCode.length < 4) {
      wx.showToast({ title: '请输入验证码', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      if (fromPhoneEntry) {
        const res = await userApi.phoneLogin({ phone, code: verifyCode });
        app.setLoginInfo(res.token);
      } else {
        // 用未落盘的 pendingToken 通过 header 鉴权
        await userApi.bindPhone(
          { phone, code: verifyCode },
          { header: { Authorization: pendingToken } }
        );
        // 绑定成功,才真正把 token 落盘
        app.setLoginInfo(pendingToken);
        this.setData({ pendingToken: '' });
      }

      wx.showToast({ title: '登录成功', icon: 'success' });
      await this.loadUserInfo();
      setTimeout(() => {
        this.loginSuccess();
      }, 800);
    } catch (e) {
      console.error('手机号登录/绑定失败', e);
      this.setData({ submitting: false });
    }
  },

  /**
   * 登录成功，跳转
   */
  loginSuccess() {
    wx.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(() => {
      redirectAfterLogin();
    }, 500);
  },

  onUnload() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
  },
});
