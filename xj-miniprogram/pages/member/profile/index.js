/**
 * 个人资料页
 */
const app = getApp();
const userApi = require('../../../services/user');

const GENDER_OPTIONS = ['未设置', '男', '女'];

Page({
  data: {
    userInfo: null,
    nickname: '',
    genderText: '未设置',
  },

  onLoad() {
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {};
    this.setData({
      userInfo,
      nickname: userInfo.nickname || '',
      genderText: GENDER_OPTIONS[userInfo.gender] || '未设置',
    });
  },

  /**
   * 选择头像
   */
  handleChooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.updateProfile({ avatar: tempFilePath });
        this.setData({ 'userInfo.avatar': tempFilePath });
      },
    });
  },

  /**
   * 昵称输入
   */
  handleNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },

  /**
   * 昵称失焦保存
   */
  handleNicknameSave() {
    const { nickname, userInfo } = this.data;
    const trimmed = nickname.trim();
    if (trimmed && trimmed !== userInfo.nickname) {
      this.updateProfile({ nickname: trimmed });
    }
  },

  /**
   * 性别选择
   */
  handleGenderChange() {
    wx.showActionSheet({
      itemList: ['男', '女'],
      success: (res) => {
        const gender = res.tapIndex + 1; // 1=男 2=女
        this.setData({ genderText: GENDER_OPTIONS[gender] });
        this.updateProfile({ gender });
      },
    });
  },

  /**
   * 更新用户信息
   */
  async updateProfile(params) {
    try {
      await userApi.updateInfo(params);
      // 更新本地缓存
      const userInfo = { ...this.data.userInfo, ...params };
      this.setData({ userInfo });
      app.globalData.userInfo = userInfo;
      wx.setStorageSync('userInfo', userInfo);
      wx.showToast({ title: '已保存', icon: 'success' });
    } catch (err) {
      console.error('更新用户信息失败', err);
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },
});
