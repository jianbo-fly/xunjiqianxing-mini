/**
 * 行程列表页 (TabBar页面)
 * 区块：待出行订单 + 为你推荐线路
 */
const orderApi = require('../../../services/order');
const routeApi = require('../../../services/route');
const { go } = require('../../../utils/router');
const { checkLogin } = require('../../../utils/auth');

Page({
  data: {
    loading: true,
    tripList: [],
    tripEmpty: false,
    recommendList: [],
  },

  onShow() {
    // 设置TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }

    // 检查登录状态
    if (!checkLogin()) return;

    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 加载所有数据
   */
  async loadData() {
    this.setData({ loading: true });

    await Promise.all([
      this.loadTripList(),
      this.loadRecommendList(),
    ]);

    this.setData({ loading: false });
  },

  /**
   * 加载待出行订单
   */
  async loadTripList() {
    try {
      const result = await orderApi.getList({ status: 2, page: 1, pageSize: 20 });
      const list = (result.list || [])
        .map(item => this.formatTrip(item))
        .sort((a, b) => a.countdown - b.countdown);

      this.setData({
        tripList: list,
        tripEmpty: list.length === 0,
      });
    } catch (err) {
      console.error('加载待出行列表失败', err);
      this.setData({ tripList: [], tripEmpty: true });
    }
  },

  /**
   * 加载推荐线路
   */
  async loadRecommendList() {
    try {
      const result = await routeApi.getList({ page: 1, pageSize: 6 });
      this.setData({
        recommendList: result.list || [],
      });
    } catch (err) {
      console.error('加载推荐线路失败', err);
      this.setData({ recommendList: [] });
    }
  },

  /**
   * 格式化行程数据
   */
  formatTrip(order) {
    const countdown = this.calcCountdown(order.startDate);
    let countdownText = '';
    if (countdown > 0) {
      countdownText = `还有${countdown}天`;
    } else if (countdown === 0) {
      countdownText = '今天出发';
    } else {
      countdownText = '出行中';
    }

    return {
      ...order,
      startDateText: this.formatDate(order.startDate),
      countdown,
      countdownText,
      peopleText: this.getPeopleText(order.adultCount, order.childCount),
    };
  },

  /**
   * 格式化日期 → MM月DD日
   */
  formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}月${day}日`;
  },

  /**
   * 计算倒计时天数
   */
  calcCountdown(dateStr) {
    if (!dateStr) return -1;
    const target = new Date(dateStr);
    target.setHours(0, 0, 0, 0);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return Math.ceil((target - today) / (1000 * 60 * 60 * 24));
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
   * 点击行程卡片 → 订单详情
   */
  handleTripTap(e) {
    const { id } = e.currentTarget.dataset;
    go.orderDetail(id);
  },

  /**
   * 点击推荐线路 → 线路详情
   */
  handleRouteTap(e) {
    const { route } = e.detail;
    if (route && route.id) {
      go.routeDetail(route.id);
    }
  },

  /**
   * 查看全部行程 → 订单列表(已确认tab)
   */
  handleViewAllTrips() {
    go.orderList('2');
  },

  /**
   * 空状态 → 去逛逛
   */
  handleExplore() {
    go.home();
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: '我的旅行行程',
      path: '/pages/trip/list/index',
    };
  },
});
