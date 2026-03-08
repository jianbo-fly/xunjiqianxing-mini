/**
 * 线路详情页
 */
const routeApi = require('../../../services/route');
const favoriteApi = require('../../../services/favorite');
const { go } = require('../../../utils/router');
const appConfig = require('../../../config/app.config');

Page({
  data: {
    // 状态栏高度
    statusBarHeight: 44,
    // 页面状态
    loading: true,
    // 线路ID
    routeId: '',
    // 线路详情
    route: null,
    // 当前轮播索引
    swiperIndex: 0,
    // 轮播图列表
    bannerImages: [],
    // 套餐列表
    packages: [],
    // 选中的套餐
    selectedPackage: null,
    // 价格日历
    calendar: [],
    // 选中的日期
    selectedDate: '',
    // 选中日期的价格
    selectedPrice: 0,
    // 收藏状态
    isFavorite: false,
    // 内容Tab
    contentTab: 0,
    contentTabs: ['行程介绍', '费用说明', '预订须知'],
    // 显示套餐选择弹窗
    showPackagePopup: false,
    // 显示日期选择弹窗
    showCalendarPopup: false,
    // 当前月份显示文本
    currentMonth: '',
    // 日历年份
    calendarYear: 0,
    // 日历月份
    calendarMonth: 0,
    // 是否可以切换到上个月
    canGoPrevMonth: false,
    // 成人数量
    adultCount: 1,
    // 儿童数量
    childCount: 0,
    // 选中日期的儿童价
    selectedChildPrice: 0,
    // 当日剩余库存
    selectedStock: 0,
    // 合计金额
    totalAmount: 0,
  },

  onLoad(options) {
    // 获取状态栏高度
    const sysInfo = wx.getSystemInfoSync();
    this.setData({ statusBarHeight: sysInfo.statusBarHeight || 44 });

    if (options.id) {
      this.setData({ routeId: options.id });
      this.loadRouteDetail();
    }
  },

  /**
   * 返回
   */
  handleBack() {
    wx.navigateBack({ fail: () => wx.switchTab({ url: '/pages/index/index' }) });
  },

  /**
   * 加载线路详情
   */
  async loadRouteDetail() {
    this.setData({ loading: true });

    try {
      // 加载详情
      const route = await routeApi.getDetail(this.data.routeId);

      // 加载套餐
      let packages = [];
      try {
        packages = await routeApi.getPackages(this.data.routeId);
      } catch (e) {
        packages = route.packages || [];
      }

      // 处理套餐属性，转换为可显示格式
      packages = this.processPackages(packages);

      // 默认选中第一个套餐
      const selectedPackage = packages.length > 0 ? packages[0] : null;

      // 处理轮播图
      const bannerImages = route.images && route.images.length > 0 ? route.images : [route.coverImage];

      this.setData({
        route,
        packages,
        selectedPackage,
        bannerImages,
        loading: false,
      });

      // 检查收藏状态
      this.checkFavorite();

      // 加载价格日历
      if (selectedPackage) {
        this.loadCalendar(selectedPackage.id);
      }
    } catch (e) {
      console.error('加载线路详情失败', e);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 加载价格日历
   */
  async loadCalendar(packageId, year, month) {
    const now = new Date();

    // 如果没有指定年月，使用当前月
    if (!year || !month) {
      year = now.getFullYear();
      month = now.getMonth() + 1;
    }

    // 计算该月的开始和结束日期
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`;

    // 判断是否可以切换到上个月（不能早于当前月）
    const canGoPrevMonth = year > now.getFullYear() ||
      (year === now.getFullYear() && month > now.getMonth() + 1);

    this.setData({
      calendarYear: year,
      calendarMonth: month,
      currentMonth: `${year}年${month}月`,
      canGoPrevMonth,
    });

    try {
      const rawCalendar = await routeApi.getPriceCalendar(packageId, startDate, endDate);

      // 处理日历数据，确保日期格式正确
      const calendar = this.processCalendar(rawCalendar || []);

      this.setData({ calendar });
    } catch (e) {
      console.error('加载价格日历失败', e);
      // 生成模拟日历数据
      this.generateMockCalendar(year, month);
    }
  },

  /**
   * 上一个月
   */
  handlePrevMonth() {
    if (!this.data.canGoPrevMonth) return;

    let { calendarYear, calendarMonth } = this.data;
    calendarMonth--;
    if (calendarMonth < 1) {
      calendarMonth = 12;
      calendarYear--;
    }

    this.loadCalendar(this.data.selectedPackage?.id, calendarYear, calendarMonth);
  },

  /**
   * 下一个月
   */
  handleNextMonth() {
    let { calendarYear, calendarMonth } = this.data;
    calendarMonth++;
    if (calendarMonth > 12) {
      calendarMonth = 1;
      calendarYear++;
    }

    this.loadCalendar(this.data.selectedPackage?.id, calendarYear, calendarMonth);
  },

  /**
   * 处理日历数据
   */
  processCalendar(rawCalendar) {
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const now = new Date();
    const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;

    return rawCalendar.map(item => {
      let dateStr = item.date;

      // 处理不同的日期格式
      if (Array.isArray(item.date)) {
        // 数组格式 [2026, 1, 29]
        const [y, m, d] = item.date;
        dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      } else if (typeof item.date === 'string') {
        // 字符串格式 "2026-01-29"
        dateStr = item.date;
      } else if (item.date && typeof item.date === 'object') {
        // 对象格式 {year: 2026, month: 1, day: 29}
        dateStr = `${item.date.year}-${String(item.date.month).padStart(2, '0')}-${String(item.date.day).padStart(2, '0')}`;
      }

      // MM-DD 格式（Figma 日期条显示格式）
      const mmdd = dateStr.slice(5);

      // 周几（今天 / 周一~周日）
      const dateObj = new Date(dateStr);
      const isWeekend = dateObj.getDay() === 0 || dateObj.getDay() === 6;
      const week = dateStr === todayStr ? '今天' : weekDays[dateObj.getDay()];

      return {
        ...item,
        date: dateStr,
        day: mmdd,           // "MM-DD" 用于日期条
        dayNum: String(parseInt(dateStr.split('-')[2], 10)), // 纯数字天 用于日历弹窗
        week,
        isWeekend,
      };
    });
  },

  /**
   * 格式化日期为 yyyy-MM-dd
   */
  formatDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  },

  /**
   * 处理套餐数据，将attrs转换为可显示格式
   */
  processPackages(packages) {
    // 属性名称映射
    const attrLabels = {
      days: '天',
      nights: '晚',
      hotel: '🏨',
      meals: '🍽️',
      shopping: '🛍️',
      groupSize: '👥',
      transport: '🚌',
      guide: '👨‍💼',
    };

    return packages.map(pkg => {
      const attrsDisplay = [];

      if (pkg.attrs) {
        // 行程天数
        if (pkg.attrs.days || pkg.attrs.nights) {
          const days = pkg.attrs.days || 0;
          const nights = pkg.attrs.nights || 0;
          attrsDisplay.push(`${days}天${nights}晚`);
        }

        // 其他属性按配置显示
        Object.keys(pkg.attrs).forEach(key => {
          if (key === 'days' || key === 'nights') return; // 已处理

          const value = pkg.attrs[key];
          if (value !== null && value !== undefined && value !== '') {
            const label = attrLabels[key] || '';
            attrsDisplay.push(`${label}${value}`);
          }
        });
      }

      return {
        ...pkg,
        attrsDisplay,
      };
    });
  },

  /**
   * 生成模拟日历数据
   */
  generateMockCalendar(year, month) {
    const now = new Date();
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;

    // 如果没有指定年月，使用当前月
    if (!year || !month) {
      year = this.data.calendarYear || now.getFullYear();
      month = this.data.calendarMonth || (now.getMonth() + 1);
    }

    const calendar = [];
    const basePrice = this.data.selectedPackage?.basePrice || 2999;
    const lastDay = new Date(year, month, 0).getDate();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    for (let d = 1; d <= lastDay; d++) {
      const date = new Date(year, month - 1, d);
      const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      const mmdd = `${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;

      // 过去的日期不显示库存
      const isPast = date < today;

      calendar.push({
        date: dateStr,
        day: mmdd,           // "MM-DD" 用于日期条
        dayNum: String(d),   // 纯数字天 用于日历弹窗
        week: dateStr === todayStr ? '今天' : weekDays[date.getDay()],
        price: basePrice + Math.floor(Math.random() * 500),
        stock: isPast ? 0 : Math.floor(Math.random() * 20),
        isWeekend: date.getDay() === 0 || date.getDay() === 6,
      });
    }

    this.setData({ calendar });
  },

  /**
   * 检查收藏状态
   */
  async checkFavorite() {
    try {
      const res = await favoriteApi.check({ routeId: this.data.routeId });
      this.setData({ isFavorite: res.isFavorite || false });
    } catch (e) {
      // 忽略错误
    }
  },

  /**
   * 轮播图变化
   */
  handleSwiperChange(e) {
    this.setData({ swiperIndex: e.detail.current });
  },

  /**
   * 预览图片
   */
  handlePreviewImage(e) {
    const { index } = e.currentTarget.dataset;
    const images = this.data.bannerImages;
    wx.previewImage({
      urls: images,
      current: images[index],
    });
  },

  /**
   * 切换内容Tab
   */
  handleContentTabChange(e) {
    const { index } = e.currentTarget.dataset;
    this.setData({ contentTab: index });
  },

  /**
   * 显示套餐选择
   */
  handleShowPackage() {
    this.setData({ showPackagePopup: true });
  },

  /**
   * 关闭套餐选择
   */
  handleClosePackage() {
    this.setData({ showPackagePopup: false });
  },

  /**
   * 选择套餐
   */
  handlePackageSelect(e) {
    const { index } = e.currentTarget.dataset;
    const selectedPackage = this.data.packages[index];
    this.setData({
      selectedPackage,
      selectedDate: '',
      selectedPrice: 0,
    });
    this.loadCalendar(selectedPackage.id);
  },

  /**
   * 显示日期选择
   */
  handleShowCalendar() {
    if (!this.data.selectedPackage) {
      wx.showToast({ title: '请先选择套餐', icon: 'none' });
      return;
    }
    this.setData({ showCalendarPopup: true });
  },

  /**
   * 关闭日期选择
   */
  handleCloseCalendar() {
    this.setData({ showCalendarPopup: false });
  },

  /**
   * 选择日期
   */
  handleDateSelect(e) {
    const { item } = e.currentTarget.dataset;
    if (item.stock <= 0) {
      wx.showToast({ title: '该日期已售罄', icon: 'none' });
      return;
    }

    // 重置人数
    const adultCount = 1;
    const childCount = 0;

    this.setData({
      selectedDate: item.date,
      selectedPrice: item.price,
      selectedChildPrice: item.childPrice || Math.floor(item.price * 0.7), // 儿童价，默认成人价的70%
      selectedStock: item.stock,
      adultCount,
      childCount,
      showCalendarPopup: false,
    });

    // 计算合计金额
    this.calcTotalAmount();
  },

  /**
   * 计算合计金额
   */
  calcTotalAmount() {
    const { selectedPrice, selectedChildPrice, adultCount, childCount } = this.data;
    const totalAmount = (selectedPrice * adultCount) + (selectedChildPrice * childCount);
    this.setData({ totalAmount });
  },

  /**
   * 成人数量减少
   */
  handleAdultMinus() {
    const { adultCount, childCount } = this.data;
    if (adultCount > 1) {
      // 成人减少时，如果儿童超过成人数量，也要减少儿童
      const newAdultCount = adultCount - 1;
      const newChildCount = Math.min(childCount, newAdultCount);
      this.setData({
        adultCount: newAdultCount,
        childCount: newChildCount,
      });
      this.calcTotalAmount();
    }
  },

  /**
   * 成人数量增加
   */
  handleAdultPlus() {
    const { adultCount, childCount, selectedStock } = this.data;
    const totalCount = adultCount + childCount;
    if (totalCount < selectedStock) {
      this.setData({ adultCount: adultCount + 1 });
      this.calcTotalAmount();
    } else {
      wx.showToast({ title: '库存不足', icon: 'none' });
    }
  },

  /**
   * 儿童数量减少
   */
  handleChildMinus() {
    const { childCount } = this.data;
    if (childCount > 0) {
      this.setData({ childCount: childCount - 1 });
      this.calcTotalAmount();
    }
  },

  /**
   * 儿童数量增加
   */
  handleChildPlus() {
    const { adultCount, childCount, selectedStock } = this.data;
    const totalCount = adultCount + childCount;

    // 儿童数量不能超过成人数量
    if (childCount >= adultCount) {
      wx.showToast({ title: '儿童数量不能超过成人', icon: 'none' });
      return;
    }

    // 检查库存
    if (totalCount >= selectedStock) {
      wx.showToast({ title: '库存不足', icon: 'none' });
      return;
    }

    this.setData({ childCount: childCount + 1 });
    this.calcTotalAmount();
  },

  /**
   * 收藏/取消收藏
   */
  async handleFavorite() {
    try {
      if (this.data.isFavorite) {
        await favoriteApi.remove({ routeId: this.data.routeId });
        this.setData({ isFavorite: false });
        wx.showToast({ title: '已取消收藏', icon: 'none' });
      } else {
        await favoriteApi.add({ routeId: this.data.routeId });
        this.setData({ isFavorite: true });
        wx.showToast({ title: '收藏成功', icon: 'success' });
      }
    } catch (e) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  /**
   * 联系客服
   */
  handleService() {
    if (appConfig.features.customerService.type === 'weixin') {
      wx.openCustomerServiceChat({
        extInfo: { url: '' },
        corpId: appConfig.features.customerService.corpId || '',
        fail: () => {
          wx.makePhoneCall({
            phoneNumber: appConfig.features.customerService.phone || '',
          });
        }
      });
    } else {
      wx.makePhoneCall({
        phoneNumber: appConfig.features.customerService.phone || '',
      });
    }
  },

  /**
   * 立即预订
   */
  handleBook() {
    if (!this.data.selectedPackage) {
      wx.showToast({ title: '请选择套餐', icon: 'none' });
      this.setData({ showPackagePopup: true });
      return;
    }
    if (!this.data.selectedDate) {
      wx.showToast({ title: '请选择出行日期', icon: 'none' });
      this.setData({ showCalendarPopup: true });
      return;
    }
    if (this.data.adultCount < 1) {
      wx.showToast({ title: '至少选择1位成人', icon: 'none' });
      return;
    }

    const { routeId, selectedPackage, selectedDate, adultCount, childCount, selectedPrice, selectedChildPrice } = this.data;

    // 跳转订单确认页
    wx.navigateTo({
      url: `/pages/order/confirm/index?routeId=${routeId}&packageId=${selectedPackage.id}&date=${selectedDate}&adultCount=${adultCount}&childCount=${childCount}&adultPrice=${selectedPrice}&childPrice=${selectedChildPrice}`,
    });
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    const { route } = this.data;
    return {
      title: route?.name || '精彩线路推荐',
      path: `/pages/route/detail/index?id=${this.data.routeId}`,
      imageUrl: route?.coverImage,
    };
  },
});
