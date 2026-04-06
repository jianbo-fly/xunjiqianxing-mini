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
    // fixed tabs header 的 top 值（精确对齐胶囊按钮底部）
    navBarBottom: 88,
    navBarContentHeight: 44,
    // 是否显示 fixed tabs header（滚动到 content-section 才显示）
    showStickyHeader: false,
    // content-section 距 scroll-view 顶部的距离（px），用于判断显隐
    contentSectionTop: 9999,
    // 导航栏是否变为不透明（滑过 banner 后）
    navOpaque: false,
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
    // 价格日历（横向日期条，当月有价数据）
    calendar: [],
    // 日期条 scroll-left（px，用于选中日期居中）
    dateStripScrollLeft: 0,
    // 选中的日期
    selectedDate: '',
    // 选中日期的价格
    selectedPrice: 0,
    // 收藏状态
    isFavorite: false,
    // 内容Tab
    contentTab: 0,
    contentTabs: ['行程介绍', '费用说明', '预订须知'],
    // 行程介绍 Day 导航
    itineraryNavIndex: 0,
    itineraryScrollIntoView: '',
    dayNavScrollLeft: 0,
    // 各 Day 卡片距离 scroll-view 顶部的距离（px），用于滚动联动
    dayCardTops: [],
    // 显示套餐选择弹窗
    showPackagePopup: false,
    // 显示日期选择弹窗
    showCalendarPopup: false,
    // 弹窗日历：多月数据（瀑布流）
    calendarMonths: [],
    loadedMonthCount: 0,
    calendarLoadingMore: false,
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
    // 获取状态栏高度，计算吸顶偏移
    const sysInfo = wx.getSystemInfoSync();
    const statusBarHeight = sysInfo.statusBarHeight || 44;
    // 用胶囊按钮的 bottom 作为导航栏底部精确值，兼容所有机型
    let navBarBottom = statusBarHeight + 44;
    try {
      const menuRect = wx.getMenuButtonBoundingClientRect();
      if (menuRect && menuRect.bottom) {
        navBarBottom = Math.ceil(menuRect.bottom);
      }
    } catch (e) { /* 降级使用默认值 */ }
    // nav-header 内容区高度 = navBarBottom - statusBarHeight
    const navBarContentHeight = navBarBottom - statusBarHeight;
    this.setData({
      statusBarHeight,
      navBarBottom,
      navBarContentHeight,
    });

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

      // 预处理行程图标
      if (route.itinerary) {
        route.itinerary = this._processItinerary(route.itinerary);
      }

      this.setData({
        route,
        packages,
        selectedPackage,
        bannerImages,
        loading: false,
      });

      // 检查收藏状态
      this.checkFavorite();

      // 测量各 Day 卡片位置，供滚动联动使用
      wx.nextTick(() => this._measureDayCardPositions());

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
   * 加载价格日历（当月，供横向日期条使用）
   */
  async loadCalendar(packageId) {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`;

    try {
      const rawCalendar = await routeApi.getPriceCalendar(packageId, startDate, endDate);
      const calendar = this.processCalendar(rawCalendar || []);
      this.setData({ calendar });
    } catch (e) {
      console.error('加载价格日历失败', e);
      this.generateMockCalendar(year, month);
    }
  },

  /**
   * 加载弹窗日历（多月瀑布流，初始两个月）
   */
  async loadCalendarPopup(packageId) {
    this.setData({ calendarMonths: [], loadedMonthCount: 0, calendarLoadingMore: false });
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;

    await this._appendMonthToPopup(packageId, year, month);
    let m2 = month + 1, y2 = year;
    if (m2 > 12) { m2 = 1; y2++; }
    await this._appendMonthToPopup(packageId, y2, m2);
    this.setData({ loadedMonthCount: 2 });
  },

  /**
   * 追加一个月到弹窗日历
   */
  async _appendMonthToPopup(packageId, year, month) {
    const pad = n => String(n).padStart(2, '0');
    const startDate = `${year}-${pad(month)}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${pad(month)}-${pad(lastDay)}`;

    let rawItems = [];
    try {
      rawItems = await routeApi.getPriceCalendar(packageId, startDate, endDate) || [];
    } catch (e) {
      // 接口失败 → 所有日期标记为实时计价
    }

    const monthData = this._buildMonthCells(year, month, rawItems);
    this.setData({ calendarMonths: [...this.data.calendarMonths, monthData] });
  },

  /**
   * 构建单月格子数据（7列，周一为首列，含价格分级）
   */
  _buildMonthCells(year, month, rawItems) {
    const pad = n => String(n).padStart(2, '0');
    const now = new Date();
    const todayStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    // 将后端数据转为 dateStr→item 映射
    const configMap = {};
    (rawItems || []).forEach(item => {
      let dateStr = item.date;
      if (Array.isArray(item.date)) {
        const [y, m, d] = item.date;
        dateStr = `${y}-${pad(m)}-${pad(d)}`;
      } else if (item.date && typeof item.date === 'object') {
        dateStr = `${item.date.year}-${pad(item.date.month)}-${pad(item.date.day)}`;
      }
      configMap[dateStr] = item;
    });

    const lastDay = new Date(year, month, 0).getDate();
    const firstWeekday = new Date(year, month - 1, 1).getDay(); // 0=周日
    const leadingCount = (firstWeekday + 6) % 7; // 转换为周一首列

    const dayCells = [];
    for (let d = 1; d <= lastDay; d++) {
      const dateStr = `${year}-${pad(month)}-${pad(d)}`;
      const dateObj = new Date(year, month - 1, d);
      const isPast = dateObj < today;
      const cfg = configMap[dateStr];

      dayCells.push({
        date: dateStr,
        day: dateStr.slice(5),
        dayNum: String(d),
        isToday: dateStr === todayStr,
        isPast,
        isWeekend: dateObj.getDay() === 0 || dateObj.getDay() === 6,
        price: cfg ? (cfg.price || null) : null,
        stock: isPast ? 0 : (cfg ? (cfg.stock !== undefined ? cfg.stock : 10) : -1),
        isRealtime: !cfg && !isPast,
      });
    }

    // 价格分级（基于当月均价）
    const prices = dayCells.filter(c => c.price != null && c.stock > 0).map(c => c.price);
    if (prices.length > 0) {
      const avg = prices.reduce((a, b) => a + b, 0) / prices.length;
      dayCells.forEach(cell => {
        if (cell.price != null && cell.stock > 0) {
          cell.priceLevel = cell.price < avg * 0.85 ? 'low' : cell.price > avg * 1.15 ? 'high' : 'normal';
        }
      });
    }

    return {
      year,
      month,
      label: `${year}年${month}月`,
      cells: [...Array(leadingCount).fill(null), ...dayCells],
    };
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
  /**
   * 预处理行程图标：给每个 activity 添加 actIconPath / actDotClass
   */
  _processItinerary(itinerary) {
    const iconMap = [
      { keys: ['plane', 'flight', 'bus', 'transport', '交通', 'car', 'train'], type: 'transport', cls: 'transport' },
      { keys: ['food', 'meal', 'fork', 'restaurant', 'lunch', 'dinner', 'breakfast', '餐', '早', '午', '晚'], type: 'meal', cls: 'meal' },
      { keys: ['camera', 'scenic', 'attraction', 'mountain', '景', 'sight', 'photo'], type: 'scenic', cls: 'scenic' },
      { keys: ['hotel', 'bed', 'accommodation', '住', 'sleep', 'lodge'], type: 'hotel', cls: 'hotel' },
    ];
    return itinerary.map(day => ({
      ...day,
      activities: (day.activities || []).map(act => {
        const iconStr = (act.icon || act.type || act.content || '').toLowerCase();
        const matched = iconMap.find(m => m.keys.some(k => iconStr.includes(k)));
        return {
          ...act,
          actIconPath: `/assets/icons/itinerary/act-${matched ? matched.type : 'transport'}.png`,
          actDotClass: matched ? matched.cls : 'transport',
        };
      }),
    }));
  },

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
    const index = parseInt(e.currentTarget.dataset.index);
    this.setData({ contentTab: index });
  },

  /**
   * 行程介绍 Day 导航点击 → 滚动到对应卡片
   */
  handleItineraryNavTap(e) {
    const { index } = e.currentTarget.dataset;
    this._setActiveDay(index);
    this.setData({ itineraryScrollIntoView: `itin-day-${index}` });
  },

  /**
   * 主滚动监听 → 联动 Day nav 高亮
   */
  handleMainScroll(e) {
    const scrollTop = e.detail.scrollTop;
    const { contentTab, dayCardTops, contentSectionTop, navBarBottom, showStickyHeader } = this.data;

    // banner 高度约 288px（576rpx），滑过后 nav 变不透明
    const navOpaque = scrollTop >= 200;
    if (navOpaque !== this.data.navOpaque) {
      this.setData({ navOpaque });
    }

    // 滚动到 content-section 才显示 fixed header
    const shouldShow = scrollTop + navBarBottom >= contentSectionTop;
    if (shouldShow !== showStickyHeader) {
      this.setData({ showStickyHeader: shouldShow });
    }

    // 滚动联动 Day nav 高亮（仅行程 tab）
    if (contentTab !== 0 || !dayCardTops.length) return;
    const threshold = navBarBottom + 94; // fixed header 总高度
    let activeIndex = 0;
    for (let i = 0; i < dayCardTops.length; i++) {
      if (scrollTop + threshold >= dayCardTops[i]) {
        activeIndex = i;
      }
    }
    if (activeIndex !== this.data.itineraryNavIndex) {
      this._setActiveDay(activeIndex);
    }
  },

  /**
   * 设置当前激活的 Day，并让 Day nav 对应项居中
   */
  _setActiveDay(index) {
    this.setData({ itineraryNavIndex: index });
    // 让激活的 tab 居中（粗略估算：每个 tab 约 120rpx）
    const TAB_WIDTH_PX = 60; // rpx/2 ≈ px（750rpx = 屏幕宽）
    const screenWidth = wx.getSystemInfoSync().windowWidth;
    const tabWidth = screenWidth * (120 / 750);
    const scrollLeft = Math.max(0, index * tabWidth - screenWidth / 2 + tabWidth / 2);
    this.setData({ dayNavScrollLeft: scrollLeft });
  },

  /**
   * 测量各 Day 卡片距 scroll-view 顶部的距离
   */
  _measureDayCardPositions() {
    const query = wx.createSelectorQuery().in(this);
    // scroll-view 基准
    query.select('.main-scroll').boundingClientRect();
    // content-section 位置
    query.select('#content-section').boundingClientRect();
    // 各 Day 卡片位置
    const itinerary = (this.data.route && this.data.route.itinerary) || [];
    itinerary.forEach((_, i) => {
      query.select(`#itin-day-${i}`).boundingClientRect();
    });
    query.exec(rects => {
      const scrollRect = rects[0];
      const sectionRect = rects[1];
      if (!scrollRect) return;
      const contentSectionTop = sectionRect ? sectionRect.top - scrollRect.top : 9999;
      const dayCardTops = rects.slice(2).map(r => r ? r.top - scrollRect.top : 0);
      this.setData({ contentSectionTop, dayCardTops });
    });
  },

  /**
   * 预览活动图片
   */
  handlePreviewActImage(e) {
    const { urls, current } = e.currentTarget.dataset;
    wx.previewImage({ urls, current });
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
   * 显示日期选择弹窗，并初始化瀑布流日历
   */
  handleShowCalendar() {
    if (!this.data.selectedPackage) {
      wx.showToast({ title: '请先选择套餐', icon: 'none' });
      return;
    }
    this.setData({ showCalendarPopup: true });
    this.loadCalendarPopup(this.data.selectedPackage?.id);
  },

  /**
   * 上滑到底部：追加下一个月
   */
  async handleLoadMoreMonth() {
    const { calendarMonths, loadedMonthCount, calendarLoadingMore } = this.data;
    if (calendarLoadingMore || loadedMonthCount >= 12) return;

    this.setData({ calendarLoadingMore: true });
    const last = calendarMonths[calendarMonths.length - 1];
    let nextYear = last.year;
    let nextMonth = last.month + 1;
    if (nextMonth > 12) { nextMonth = 1; nextYear++; }

    await this._appendMonthToPopup(this.data.selectedPackage?.id, nextYear, nextMonth);
    this.setData({ loadedMonthCount: loadedMonthCount + 1, calendarLoadingMore: false });
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
    if (!item || item.isPast) return;
    if (item.stock === 0) {
      wx.showToast({ title: '该日期已售罄', icon: 'none' });
      return;
    }

    this.setData({
      selectedDate: item.date,
      selectedPrice: item.price || 0,
      selectedChildPrice: item.childPrice || (item.price ? Math.floor(item.price * 0.7) : 0),
      selectedStock: item.stock,
      adultCount: 1,
      childCount: 0,
      showCalendarPopup: false,
    });

    // 计算合计金额
    this.calcTotalAmount();

    // 日期条滚动居中
    this._scrollDateStripToCenter(item.date);
  },

  /**
   * 滚动日期条，使指定日期居中
   */
  _scrollDateStripToCenter(date) {
    const index = this.data.calendar.findIndex(item => item.date === date);
    if (index < 0) return;

    wx.createSelectorQuery()
      .select('.date-strip-scroll')
      .boundingClientRect(rect => {
        if (!rect) return;
        const { windowWidth } = wx.getSystemInfoSync();
        const rpxRatio = windowWidth / 750;
        const itemWidth = 144 * rpxRatio;
        const gap = 16 * rpxRatio;
        const paddingLeft = 32 * rpxRatio;
        // 让选中卡片的中心对齐容器中心
        const scrollLeft = paddingLeft + index * (itemWidth + gap) - (rect.width - itemWidth) / 2;
        this.setData({ dateStripScrollLeft: Math.max(0, scrollLeft) });
      })
      .exec();
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
