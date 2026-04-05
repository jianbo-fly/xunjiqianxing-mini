/**
 * 支付结果页
 */
const { go } = require('../../../utils/router');

Page({
  data: {
    orderId: '',
    amount: '',
    routeName: '',
    packageName: '',
    departureDate: '',
    travelerCount: '',
  },

  onLoad(options) {
    const {
      orderId = '',
      amount = '',
      routeName = '',
      packageName = '',
      departureDate = '',
      travelerCount = '',
    } = options;

    this.setData({
      orderId,
      amount: decodeURIComponent(amount),
      routeName: decodeURIComponent(routeName),
      packageName: decodeURIComponent(packageName),
      departureDate: decodeURIComponent(departureDate),
      travelerCount,
    });
  },

  /**
   * 查看订单
   */
  handleViewOrder() {
    if (this.data.orderId) {
      go.orderDetail(this.data.orderId);
    } else {
      go.orderList();
    }
  },

  /**
   * 返回首页
   */
  handleGoHome() {
    go.home();
  },

  /**
   * 出行准备横幅点击（预留）
   */
  handleBannerTap() {
    // 可跳转到攻略/装备指南页
  },
});
