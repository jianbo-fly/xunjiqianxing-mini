/**
 * 推广中心页
 */
const promoterApi = require('../../../services/promoter');
const { go } = require('../../../utils/router');

Page({
  data: {
    loading: true,
    promoter: null,
  },

  onLoad() {
    this.loadPromoterInfo();
  },

  /**
   * 加载推广员信息
   */
  async loadPromoterInfo() {
    try {
      const [info, stats] = await Promise.all([
        promoterApi.getInfo(),
        promoterApi.getStatistics(),
      ]);

      this.setData({
        promoter: {
          ...info,
          scanCount: stats.scanCount || 0,
          orderCount: stats.orderCount || 0,
          points: stats.points || 0,
        },
        loading: false,
      });
    } catch (err) {
      console.error('加载推广员信息失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 保存推广码图片
   */
  handleSaveImage() {
    const qrCodeUrl = this.data.promoter?.qrCodeUrl;
    if (!qrCodeUrl) {
      wx.showToast({ title: '推广码未生成', icon: 'none' });
      return;
    }

    wx.saveImageToPhotosAlbum({
      filePath: qrCodeUrl,
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

  /**
   * 分享好友
   */
  handleShare() {
    // 触发分享 - 需通过 onShareAppMessage 实现
  },

  /**
   * 推广明细
   */
  handlePromoterDetail() {
    go.promoterBindList();
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    const promoter = this.data.promoter || {};
    return {
      title: `${promoter.name || '好友'}邀你一起旅行`,
      path: `/pages/index/index?promoterCode=${promoter.promoterCode || ''}`,
    };
  },
});
