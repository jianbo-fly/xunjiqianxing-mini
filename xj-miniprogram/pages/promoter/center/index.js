/**
 * 推广中心页
 */
const promoterApi = require('../../../services/promoter');
const { go, navigateBack } = require('../../../utils/router');

Page({
  data: {
    statusBarHeight: 0,
    navBarHeight: 44,
    loading: true,
    promoter: null,
  },

  onLoad() {
    const windowInfo = wx.getWindowInfo();
    const deviceInfo = wx.getDeviceInfo();
    const statusBarHeight = windowInfo.statusBarHeight || 0;
    const navBarHeight = deviceInfo.platform === 'ios' ? 44 : 48;
    this.setData({ statusBarHeight, navBarHeight });
    this.loadPromoterInfo();
  },

  async loadPromoterInfo() {
    // 从本地缓存读取用户信息，补充头像和昵称（后端推广员接口暂不返回这两个字段）
    const userInfo = wx.getStorageSync('userInfo') || {};

    try {
      const [info, stats] = await Promise.all([
        promoterApi.getInfo(),
        promoterApi.getStatistics(),
      ]);

      // 未申请推广员，跳转申请页
      if (!info) {
        wx.redirectTo({ url: '/pages/promoter/apply/index' });
        return;
      }

      this.setData({
        promoter: {
          ...info,
          nickname: userInfo.nickname || userInfo.name || '推广员',
          avatar: userInfo.avatar || '',
          scanCount: (stats && stats.scanCount) || 0,
          orderCount: (stats && stats.orderCount) || 0,
          points: (stats && stats.availableCommission) || 0,
        },
        loading: false,
      });

      // 已审核通过才生成二维码
      if (info.status === 1) {
        this.loadQrCode();
      }
    } catch (err) {
      console.error('加载推广员信息失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  async loadQrCode() {
    try {
      const { dataUri, filePath } = await promoterApi.downloadQrCode();
      this.setData({
        'promoter.qrCodeUrl': dataUri,
        'promoter.qrFilePath': filePath,
      });
    } catch (err) {
      console.error('加载推广码失败', err);
    }
  },

  handleBack() {
    navigateBack();
  },

  handleEditProfile() {
    go.promoterApply();
  },

  handleSaveImage() {
    const { qrFilePath, qrCodeUrl } = this.data.promoter || {};
    if (!qrCodeUrl) {
      wx.showToast({ title: '推广码未生成', icon: 'none' });
      return;
    }
    wx.saveImageToPhotosAlbum({
      filePath: qrFilePath || qrCodeUrl,
      success: () => {
        wx.showToast({ title: '已保存到相册', icon: 'success' });
      },
      fail: (err) => {
        if (err.errMsg.includes('auth deny')) {
          wx.showModal({
            title: '提示',
            content: '需要您授权保存图片到相册',
            confirmText: '去授权',
            success: (res) => {
              if (res.confirm) wx.openSetting();
            },
          });
        }
      },
    });
  },

  handleShare() {
    // 触发系统分享，实际分享由 onShareAppMessage 处理
    wx.showShareMenu({ withShareTicket: true });
  },

  handlePromoterDetail() {
    go.promoterBindList();
  },

  onShareAppMessage() {
    const promoter = this.data.promoter || {};
    return {
      title: `${promoter.nickname || promoter.name || '好友'}邀你一起旅行`,
      path: `/pages/index/index?promoterCode=${promoter.promoCode || ''}`,
    };
  },
});
