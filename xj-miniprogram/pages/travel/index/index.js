/**
 * 旅游页 - 跟团游 & 定制游
 */
const routeApi = require('../../../services/route');
const customApi = require('../../../services/custom');
const { go } = require('../../../utils/router');

Page({
  data: {
    // 状态栏高度
    statusBarHeight: 44,
    // 顶部Tab
    currentTab: 0, // 0=跟团游, 1=定制游
    tabs: ['跟团游', '定制游'],

    // ========== 跟团游相关 ==========
    // 二级分类
    categoryIndex: 0,
    categories: ['国内游', '出境游'],

    // 筛选条件
    showFilter: false,
    filters: {
      departure: '',
      days: '',
      priceMin: '',
      priceMax: '',
    },
    departures: ['不限', '北京', '上海', '广州', '深圳', '成都', '杭州', '南京', '武汉'],
    daysOptions: ['不限', '1-3天', '4-6天', '7-9天', '10天+'],

    // 线路列表
    routeList: [],
    routeLoading: false,
    routeFinished: false,
    routePage: 1,
    routePageSize: 10,

    // ========== 定制游相关 ==========
    // 目的地
    destinations: ['云南', '三亚', '日本', '新疆', '西藏', '其他'],
    selectedDest: '',
    customDest: '',

    // 出行时间
    timeOptions: ['本周末', '下周', '本月', '选日期'],
    selectedTime: '',
    customDate: '',
    showDatePicker: false,

    // 出行天数
    daysChoices: ['3-5天', '6-8天', '9天+', '不确定'],
    selectedDays: '',

    // 出行人数
    adultCount: 2,
    childCount: 0,

    // 预算范围
    budgetOptions: ['3k以下', '3-5k', '5-8k', '8k-1w', '1w+'],
    selectedBudget: '',

    // 其他需求
    needTags: [
      { id: 'elderly', name: '有老人', selected: false },
      { id: 'child', name: '有小孩', selected: false },
      { id: 'honeymoon', name: '蜜月', selected: false },
      { id: 'noShopping', name: '不购物', selected: false },
      { id: 'relaxed', name: '轻松不赶', selected: false },
      { id: 'deep', name: '深度体验', selected: false },
      { id: 'hotspot', name: '网红打卡', selected: false },
    ],
    extraNote: '',

    // 联系方式
    phone: '',

    // 提交状态
    submitting: false,
    showSuccess: false,
  },

  onLoad() {
    // 获取状态栏高度
    const sysInfo = wx.getSystemInfoSync();
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 44 });

    this.loadRouteList(true);
    this.loadUserPhone();
  },

  /**
   * 返回
   */
  handleBack() {
    wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/index/index' }) });
  },

  /**
   * 搜索
   */
  handleSearch() {
    go.routeList();
  },

  /**
   * 切换顶部Tab
   */
  handleTabChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentTab: index });
  },

  // ==================== 跟团游 ====================

  /**
   * 切换二级分类
   */
  handleCategoryChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ categoryIndex: index, routePage: 1, routeList: [], routeFinished: false });
    this.loadRouteList(true);
  },

  /**
   * 显示筛选
   */
  handleShowFilter() {
    this.setData({ showFilter: true });
  },

  /**
   * 关闭筛选
   */
  handleCloseFilter() {
    this.setData({ showFilter: false });
  },

  /**
   * 选择出发地
   */
  handleDepartureChange(e) {
    const departure = this.data.departures[e.detail.value];
    this.setData({ 'filters.departure': departure === '不限' ? '' : departure });
  },

  /**
   * 选择天数
   */
  handleDaysChange(e) {
    const days = this.data.daysOptions[e.detail.value];
    this.setData({ 'filters.days': days === '不限' ? '' : days });
  },

  /**
   * 重置筛选
   */
  handleResetFilter() {
    this.setData({
      filters: { departure: '', days: '', priceMin: '', priceMax: '' }
    });
  },

  /**
   * 确认筛选
   */
  handleConfirmFilter() {
    this.setData({ showFilter: false, routePage: 1, routeList: [], routeFinished: false });
    this.loadRouteList(true);
  },

  /**
   * 加载线路列表
   */
  async loadRouteList(refresh = false) {
    if (this.data.routeLoading || this.data.routeFinished) return;

    this.setData({ routeLoading: true });

    try {
      const params = {
        page: this.data.routePage,
        pageSize: this.data.routePageSize,
        category: this.data.categoryIndex === 0 ? 'domestic' : 'overseas',
        ...this.data.filters,
      };

      const res = await routeApi.getList(params);
      const list = res.list || res.records || [];

      this.setData({
        routeList: refresh ? list : [...this.data.routeList, ...list],
        routePage: this.data.routePage + 1,
        routeFinished: list.length < this.data.routePageSize,
      });
    } catch (e) {
      console.error('加载线路失败', e);
    } finally {
      this.setData({ routeLoading: false });
    }
  },

  /**
   * 线路点击
   */
  handleRouteTap(e) {
    const { route } = e.detail;
    if (route && route.id) {
      go.routeDetail(route.id);
    }
  },

  /**
   * 触底加载更多
   */
  onReachBottom() {
    if (this.data.currentTab === 0) {
      this.loadRouteList();
    }
  },

  // ==================== 定制游 ====================

  /**
   * 获取用户手机号
   */
  loadUserPhone() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    this.setData({ phone: userInfo.phone || '' });
  },

  /**
   * 选择目的地
   */
  handleDestSelect(e) {
    const dest = e.currentTarget.dataset.dest;
    this.setData({ selectedDest: dest, customDest: dest === '其他' ? '' : '' });
  },

  /**
   * 输入自定义目的地
   */
  handleCustomDestInput(e) {
    this.setData({ customDest: e.detail.value });
  },

  /**
   * 选择出行时间
   */
  handleTimeSelect(e) {
    const time = e.currentTarget.dataset.time;
    if (time === '选日期') {
      this.setData({ showDatePicker: true, selectedTime: time });
    } else {
      this.setData({ selectedTime: time, customDate: '' });
    }
  },

  /**
   * 日期选择
   */
  handleDateChange(e) {
    this.setData({ customDate: e.detail.value, showDatePicker: false });
  },

  /**
   * 选择出行天数
   */
  handleDaysSelect(e) {
    const days = e.currentTarget.dataset.days;
    this.setData({ selectedDays: days });
  },

  /**
   * 成人数量变更
   */
  handleAdultChange(e) {
    const type = e.currentTarget.dataset.type;
    let count = this.data.adultCount;
    if (type === 'minus' && count > 1) count--;
    if (type === 'plus' && count < 99) count++;
    this.setData({ adultCount: count });
  },

  /**
   * 儿童数量变更
   */
  handleChildChange(e) {
    const type = e.currentTarget.dataset.type;
    let count = this.data.childCount;
    if (type === 'minus' && count > 0) count--;
    if (type === 'plus' && count < 99) count++;
    this.setData({ childCount: count });
  },

  /**
   * 选择预算
   */
  handleBudgetSelect(e) {
    const budget = e.currentTarget.dataset.budget;
    this.setData({ selectedBudget: budget });
  },

  /**
   * 切换需求标签
   */
  handleNeedTagTap(e) {
    const index = e.currentTarget.dataset.index;
    const key = `needTags[${index}].selected`;
    this.setData({ [key]: !this.data.needTags[index].selected });
  },

  /**
   * 输入额外备注
   */
  handleExtraNoteInput(e) {
    this.setData({ extraNote: e.detail.value });
  },

  /**
   * 输入手机号
   */
  handlePhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  /**
   * 提交定制需求
   */
  async handleSubmitCustom() {
    // 表单验证
    if (!this.data.selectedDest) {
      wx.showToast({ title: '请选择目的地', icon: 'none' });
      return;
    }
    if (this.data.selectedDest === '其他' && !this.data.customDest) {
      wx.showToast({ title: '请输入目的地', icon: 'none' });
      return;
    }
    if (!this.data.selectedTime) {
      wx.showToast({ title: '请选择出行时间', icon: 'none' });
      return;
    }
    if (!this.data.selectedDays) {
      wx.showToast({ title: '请选择出行天数', icon: 'none' });
      return;
    }
    if (!this.data.selectedBudget) {
      wx.showToast({ title: '请选择预算范围', icon: 'none' });
      return;
    }
    if (!this.data.phone) {
      wx.showToast({ title: '请输入联系方式', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      const selectedNeeds = this.data.needTags.filter(t => t.selected).map(t => t.name);

      await customApi.submit({
        destination: this.data.selectedDest === '其他' ? this.data.customDest : this.data.selectedDest,
        travelTime: this.data.selectedTime === '选日期' ? this.data.customDate : this.data.selectedTime,
        travelDays: this.data.selectedDays,
        adultCount: this.data.adultCount,
        childCount: this.data.childCount,
        budget: this.data.selectedBudget,
        needs: selectedNeeds.join(','),
        extraNote: this.data.extraNote,
        phone: this.data.phone,
      });

      this.setData({ showSuccess: true });
    } catch (e) {
      console.error('提交定制需求失败', e);
      wx.showToast({ title: '提交失败，请重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  },

  /**
   * 返回首页
   */
  handleBackHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  /**
   * 查看我的定制
   */
  handleViewMyCustom() {
    wx.navigateTo({ url: '/pages/custom/list/index' });
  },

  /**
   * 关闭成功弹窗
   */
  handleCloseSuccess() {
    this.setData({ showSuccess: false });
    // 重置表单
    this.resetCustomForm();
  },

  /**
   * 重置定制表单
   */
  resetCustomForm() {
    this.setData({
      selectedDest: '',
      customDest: '',
      selectedTime: '',
      customDate: '',
      selectedDays: '',
      adultCount: 2,
      childCount: 0,
      selectedBudget: '',
      needTags: this.data.needTags.map(t => ({ ...t, selected: false })),
      extraNote: '',
    });
  },
});
