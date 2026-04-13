/**
 * 推广员审核状态页
 * 申请提交后跳转到此页，展示审核进度
 */
const promoterApi = require('../../../services/promoter');
const { navigateBack } = require('../../../utils/router');

Page({
  data: {
    promoter: null,
  },

  onLoad() {
    this.loadPromoterInfo();
  },

  onShow() {
    this.loadPromoterInfo();
  },

  async loadPromoterInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {};

    try {
      const info = await promoterApi.getInfo();

      if (!info) {
        wx.showToast({ title: '暂无申请记录', icon: 'none' });
        setTimeout(() => navigateBack(), 1500);
        return;
      }

      if (info.status === 1) {
        wx.redirectTo({ url: '/pages/promoter/center/index' });
        return;
      }

      this.setData({
        promoter: {
          ...info,
          nickname: userInfo.nickname || info.nickname || '--',
          applyTime: info.applyTime?this.formatDateTime(info.applyTime):"" || info.createdAt || '--',
          type: info.type || '推广员',
        },
      });
    } catch (err) {
      console.error('获取推广员信息失败', err);
    }
  },

  handleContact() {
    wx.makePhoneCall({
      phoneNumber: '400-000-0000',
      fail() {},
    });
  },

    /**
   * 格式化日期时间
   */
    formatDateTime(dateStr) {
      if (!dateStr) return '';
      return dateStr.replace('T', ' ').substring(0, 16);
    },

});
