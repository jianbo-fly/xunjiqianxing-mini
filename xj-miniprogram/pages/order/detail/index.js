/**
 * 订单详情页
 */
const orderApi = require('../../../services/order');
const payApi = require('../../../services/pay');
const { go, redirectTo, routes } = require('../../../utils/router');

Page({
  // 倒计时定时器（实例变量，不放 data 避免触发 setData 重渲染）
  _countdownTimer: null,

  data: {
    // 页面状态
    loading: true,
    // 滚动区高度（自定义导航栏，动态计算）
    scrollHeight: 0,
    // 订单ID
    orderId: '',
    // 订单详情
    order: null,
    // 倒计时
    countdown: '',
  },

  onLoad(options) {
    // 计算自定义导航栏高度，用于 scroll-view 高度
    try {
      const systemInfo = wx.getSystemInfoSync();
      const menuButton = wx.getMenuButtonBoundingClientRect();
      const navBarHeight = menuButton.height + (menuButton.top - systemInfo.statusBarHeight) * 2;
      const scrollHeight = systemInfo.windowHeight - systemInfo.statusBarHeight - navBarHeight;
      this.setData({ scrollHeight });
    } catch (e) {
      this.setData({ scrollHeight: 600 });
    }

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
    this._clearCountdown();
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
      showRefundBtn: [1, 2].includes(order.status),
      showContactBtn: [1, 2].includes(order.status),
      showBuyAgainBtn: [3, 4, 6, 7].includes(order.status),
      // 状态提示
      statusTip: this.getStatusTip(order.status),
      // 状态文本（API 未返回时兜底）
      statusText: order.statusText || '',
      // 状态横幅颜色类
      statusBannerClass: order.status === 0 ? 'orange' : [4, 7].includes(order.status) ? 'gray' : order.status === 3 ? 'green' : [5, 6].includes(order.status) ? 'blue' : 'primary',
      // 状态图标名
      statusIcon: order.status === 0 ? 'pending' : order.status === 1 || order.status === 2 ? 'confirmed' : order.status === 3 ? 'completed' : order.status === 4 || order.status === 7 ? 'cancelled' : 'refunding',
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
      1: '预订成功，请准时出行',
      2: '旅途愉快！',
      3: '感谢您的出行，期待下次相遇',
      4: '订单已取消',
      5: '退款申请中，请耐心等待',
      6: '退款已完成',
      7: '订单已关闭',
    };
    return tips[status] || '';
  },

  /**
   * 清除倒计时（内部方法）
   */
  _clearCountdown() {
    if (this._countdownTimer) {
      clearInterval(this._countdownTimer);
      this._countdownTimer = null;
    }
  },

  /**
   * 启动支付倒计时
   */
  startCountdown(expireAt) {
    // 先清除可能存在的旧计时器
    this._clearCountdown();

    const expireTime = new Date(expireAt).getTime();

    const updateCountdown = () => {
      const now = Date.now();
      const diff = expireTime - now;

      if (diff <= 0) {
        this._clearCountdown();
        this.setData({ countdown: '已超时' });
        // 刷新订单状态（超时后后端会关闭订单）
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
    this._countdownTimer = setInterval(updateCountdown, 1000);
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
