/**
 * 订单详情页
 */
const orderApi = require('../../../services/order');
const payApi = require('../../../services/pay');
const { go, redirectTo, routes } = require('../../../utils/router');

Page({
  data: {
    // 页面状态
    loading: true,
    // 订单ID
    orderId: '',
    // 订单详情
    order: null,
    // 倒计时
    countdown: '',
    countdownTimer: null,
  },

  onLoad(options) {
    const { id } = options;
    if (!id) {
      wx.showToast({ title: '订单不存在', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({ orderId: id });
    this.loadDetail();
  },

  onUnload() {
    // 清除倒计时
    if (this.data.countdownTimer) {
      clearInterval(this.data.countdownTimer);
    }
  },

  /**
   * 加载订单详情
   */
  async loadDetail() {
    try {
      this.setData({ loading: true });

      const order = await orderApi.getDetail(this.data.orderId);
      const formattedOrder = this.formatOrder(order);

      this.setData({
        order: formattedOrder,
        loading: false,
      });

      // 如果是待支付状态，启动倒计时
      if (order.status === 0 && order.expireAt) {
        this.startCountdown(order.expireAt);
      }
    } catch (err) {
      console.error('加载订单详情失败', err);
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 格式化订单数据
   */
  formatOrder(order) {
    // 分类出行人
    const adultTravelers = (order.travelers || []).filter(t => t.travelerType === 1);
    const childTravelers = (order.travelers || []).filter(t => t.travelerType === 2);

    return {
      ...order,
      // 格式化日期
      startDateText: this.formatDate(order.startDate),
      endDateText: order.endDate ? this.formatDate(order.endDate) : '',
      createdAtText: this.formatDateTime(order.createdAt),
      payTimeText: order.payTime ? this.formatDateTime(order.payTime) : '',
      // 格式化金额
      totalAmountText: (order.totalAmount || 0).toFixed(2),
      discountAmountText: (order.discountAmount || 0).toFixed(2),
      payAmountText: (order.payAmount || 0).toFixed(2),
      adultPriceText: (order.adultPrice || 0).toFixed(2),
      childPriceText: (order.childPrice || 0).toFixed(2),
      adultTotalText: ((order.adultPrice || 0) * (order.adultCount || 0)).toFixed(2),
      childTotalText: ((order.childPrice || 0) * (order.childCount || 0)).toFixed(2),
      // 人数文本
      peopleText: this.getPeopleText(order.adultCount, order.childCount),
      // 出行人分类
      adultTravelers,
      childTravelers,
      // 按钮状态
      showPayBtn: order.status === 0,
      showCancelBtn: order.status === 0,
      showRefundBtn: [1, 2, 3].includes(order.status),
      showContactBtn: [1, 2, 3].includes(order.status),
      showBuyAgainBtn: [4, 5, 7, 8].includes(order.status),
      // 状态提示
      statusTip: this.getStatusTip(order.status),
    };
  },

  /**
   * 格式化日期
   */
  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekDay = ['日', '一', '二', '三', '四', '五', '六'][date.getDay()];
    return `${year}年${month}月${day}日 周${weekDay}`;
  },

  /**
   * 格式化日期时间
   */
  formatDateTime(dateStr) {
    if (!dateStr) return '';
    return dateStr.replace('T', ' ').substring(0, 19);
  },

  /**
   * 获取人数文本
   */
  getPeopleText(adultCount, childCount) {
    const parts = [];
    if (adultCount > 0) parts.push(`${adultCount}成人`);
    if (childCount > 0) parts.push(`${childCount}儿童`);
    return parts.join(' ');
  },

  /**
   * 获取状态提示
   */
  getStatusTip(status) {
    const tips = {
      0: '请在30分钟内完成支付，超时订单将自动取消',
      1: '订单已支付，等待商家确认',
      2: '订单已确认，请准时出行',
      3: '旅途愉快！',
      4: '感谢您的出行，期待下次相遇',
      5: '订单已取消',
      6: '退款申请中，请耐心等待',
      7: '退款已完成',
      8: '订单已关闭',
    };
    return tips[status] || '';
  },

  /**
   * 启动支付倒计时
   */
  startCountdown(expireAt) {
    const expireTime = new Date(expireAt).getTime();

    const updateCountdown = () => {
      const now = Date.now();
      const diff = expireTime - now;

      if (diff <= 0) {
        this.setData({ countdown: '已超时' });
        clearInterval(this.data.countdownTimer);
        // 刷新订单状态
        this.loadDetail();
        return;
      }

      const minutes = Math.floor(diff / 60000);
      const seconds = Math.floor((diff % 60000) / 1000);
      this.setData({
        countdown: `${minutes}分${seconds.toString().padStart(2, '0')}秒`,
      });
    };

    updateCountdown();
    const timer = setInterval(updateCountdown, 1000);
    this.setData({ countdownTimer: timer });
  },

  /**
   * 去支付
   */
  async handlePay() {
    const { orderId } = this.data;

    try {
      wx.showLoading({ title: '正在支付...' });

      // 创建支付
      const payParams = await payApi.createOrderPayment(orderId);
      wx.hideLoading();

      // 调起微信支付
      await payApi.wxPay(payParams);

      // 支付成功
      wx.showToast({ title: '支付成功', icon: 'success' });

      // 刷新订单详情
      setTimeout(() => this.loadDetail(), 1500);

    } catch (err) {
      wx.hideLoading();

      if (err.code === -2) {
        wx.showToast({ title: '已取消支付', icon: 'none' });
      } else {
        wx.showToast({ title: err.message || '支付失败', icon: 'none' });
      }
    }
  },

  /**
   * 取消订单
   */
  handleCancel() {
    wx.showModal({
      title: '提示',
      content: '确定要取消此订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '取消中...' });
            await orderApi.cancel(this.data.orderId);
            wx.hideLoading();
            wx.showToast({ title: '已取消', icon: 'success' });
            this.loadDetail();
          } catch (err) {
            wx.hideLoading();
            wx.showToast({ title: err.message || '取消失败', icon: 'none' });
          }
        }
      },
    });
  },

  /**
   * 申请退款
   */
  handleRefund() {
    go.orderRefund(this.data.orderId);
  },

  /**
   * 联系客服
   */
  handleContact() {
    wx.makePhoneCall({
      phoneNumber: '400-888-8888',
      fail: () => {
        wx.showToast({ title: '拨打失败', icon: 'none' });
      },
    });
  },

  /**
   * 再次购买
   */
  handleBuyAgain() {
    const { order } = this.data;
    if (order && order.productId) {
      go.routeDetail(order.productId);
    }
  },

  /**
   * 复制订单号
   */
  handleCopyOrderNo() {
    const { order } = this.data;
    if (order && order.orderNo) {
      wx.setClipboardData({
        data: order.orderNo,
        success: () => {
          wx.showToast({ title: '已复制', icon: 'success' });
        },
      });
    }
  },

  /**
   * 查看线路详情
   */
  handleViewProduct() {
    const { order } = this.data;
    if (order && order.productId) {
      go.routeDetail(order.productId);
    }
  },
});
