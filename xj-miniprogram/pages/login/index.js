/**
 * 登录页面
 * 流程：微信登录 → 完善信息（新用户）→ 绑定手机号
 */
const app = getApp();
const userApi = require('../../services/user');
const { redirectAfterLogin } = require('../../utils/auth');
const apiConfig = require('../../config/api');

Page({
  data: {
    // 当前步骤：1-欢迎页 2-完善信息 3-绑定手机号
    step: 1,
    // 是否同意协议
    agreed: false,
    // 登录中
    logging: false,
    // 是否新用户
    isNewUser: false,
    // 用户信息
    userInfo: {
      avatar: '',
      nickname: '',
    },
    // 手机号
    phone: '',
    // 验证码
    verifyCode: '',
    // 倒计时
    countdown: 0,
    // 是否可以跳过手机号绑定
    canSkipPhone: true,
    // 提交中
    submitting: false,
  },

  onLoad(options) {
    // 如果有来源页面，记录下来
    if (options.redirect) {
      wx.setStorageSync('redirectUrl', decodeURIComponent(options.redirect));
    }
  },

  /**
   * 切换协议同意状态
   */
  handleAgreeChange() {
    this.setData({ agreed: !this.data.agreed });
  },

  /**
   * 查看用户协议
   */
  handleViewAgreement() {
    wx.navigateTo({ url: '/pages/webview/index?type=user-agreement' });
  },

  /**
   * 查看隐私政策
   */
  handleViewPrivacy() {
    wx.navigateTo({ url: '/pages/webview/index?type=privacy-policy' });
  },

  /**
   * 微信一键登录
   */
  async handleWxLogin() {
    if (!this.data.agreed) {
      wx.showToast({ title: '请先同意用户协议', icon: 'none' });
      return;
    }

    if (this.data.logging) return;
    this.setData({ logging: true });

    try {
      // 获取微信登录code
      const { code } = await this.wxLogin();

      // 调用后端登录接口
      const res = await userApi.wxLogin({ code });

      // 保存token
      app.setLoginInfo(res.token);

      // 判断是否新用户
      if (res.isNewUser) {
        // 新用户，进入完善信息步骤
        this.setData({
          step: 2,
          isNewUser: true,
          logging: false,
        });
      } else {
        // 老用户，获取用户信息后跳转
        await this.loadUserInfo();
        this.loginSuccess();
      }
    } catch (e) {
      console.error('登录失败', e);
      this.setData({ logging: false });
    }
  },

  /**
   * wx.login Promise封装
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

  /**
   * 选择头像
   */
  handleChooseAvatar(e) {
    const avatarUrl = e.detail.avatarUrl;
    this.setData({ 'userInfo.avatar': avatarUrl });
  },

  /**
   * 输入昵称
   */
  handleNicknameInput(e) {
    this.setData({ 'userInfo.nickname': e.detail.value });
  },

  /**
   * 提交用户信息，进入下一步
   */
  async handleSubmitProfile() {
    const { avatar, nickname } = this.data.userInfo;

    if (!nickname || !nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      // 如果有头像，先上传
      let avatarUrl = avatar;
      if (avatar && avatar.startsWith('wxfile://')) {
        avatarUrl = await this.uploadAvatar(avatar);
      }

      // 更新用户信息
      await userApi.updateInfo({
        nickname: nickname.trim(),
        avatar: avatarUrl,
      });

      // 进入绑定手机号步骤
      this.setData({ step: 3, submitting: false });
    } catch (e) {
      console.error('更新用户信息失败', e);
      this.setData({ submitting: false });
    }
  },

  /**
   * 上传头像
   */
  async uploadAvatar(filePath) {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token');
      wx.uploadFile({
        url: apiConfig.baseUrl + '/api/common/upload',
        filePath,
        name: 'file',
        header: {
          'Authorization': token || '',
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data);
            if (data.code === 200 || data.code === 0) {
              resolve(data.data.url || data.data);
            } else {
              reject(new Error(data.message));
            }
          } else {
            reject(new Error('上传失败'));
          }
        },
        fail: reject,
      });
    });
  },

  /**
   * 微信一键获取手机号
   */
  async handleGetPhoneNumber(e) {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      // 用户拒绝授权
      return;
    }

    this.setData({ submitting: true });

    try {
      // 发送加密数据到后端解密并绑定
      await userApi.bindPhoneByWx({
        code: e.detail.code,
      });

      wx.showToast({ title: '绑定成功', icon: 'success' });

      // 刷新用户信息
      await this.loadUserInfo();

      // 登录成功
      setTimeout(() => {
        this.loginSuccess();
      }, 1000);
    } catch (e) {
      console.error('绑定手机号失败', e);
      this.setData({ submitting: false });
    }
  },

  /**
   * 输入手机号
   */
  handlePhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  /**
   * 输入验证码
   */
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

      // 开始倒计时
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
   * 手机号验证码绑定
   */
  async handleBindPhone() {
    const { phone, verifyCode } = this.data;

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
      await userApi.bindPhone({ phone, code: verifyCode });
      wx.showToast({ title: '绑定成功', icon: 'success' });

      // 刷新用户信息
      await this.loadUserInfo();

      // 登录成功
      setTimeout(() => {
        this.loginSuccess();
      }, 1000);
    } catch (e) {
      console.error('绑定手机号失败', e);
      this.setData({ submitting: false });
    }
  },

  /**
   * 跳过手机号绑定
   */
  handleSkipPhone() {
    this.loadUserInfo().then(() => {
      this.loginSuccess();
    });
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
