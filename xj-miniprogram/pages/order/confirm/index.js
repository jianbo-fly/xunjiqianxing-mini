/**
 * 订单确认页
 */
const orderApi = require('../../../services/order');
const travelerApi = require('../../../services/traveler');
const couponApi = require('../../../services/coupon');
const payApi = require('../../../services/pay');
const routeApi = require('../../../services/route');

Page({
  data: {
    loading: true,
    submitting: false,
    // 路由参数
    routeId: '',
    packageId: '',
    date: '',
    adultCount: 1,
    childCount: 0,
    adultPrice: 0,
    childPrice: 0,
    // 线路信息
    route: null,
    packageInfo: null,
    // 出行人列表（常用）
    travelerList: [],
    // 已选成人出行人
    adultTravelers: [],
    // 已选儿童出行人
    childTravelers: [],
    // 联系人信息
    contact: {
      name: '',
      phone: '',
    },
    useOtherContact: false,
    // 优惠券
    coupons: [],
    selectedCoupon: null,
    showCouponPopup: false,
    // 金额计算
    adultAmount: 0,
    childAmount: 0,
    totalAmount: 0,
    couponDiscount: 0,
    payAmount: 0,
    // 出行人选择弹窗
    showTravelerPopup: false,
    travelerType: 'adult', // adult | child
    travelerIndex: 0,
  },

  onLoad(options) {
    const { routeId, packageId, date, adultCount, childCount, adultPrice, childPrice } = options;

    this.setData({
      routeId,
      packageId,
      date,
      adultCount: parseInt(adultCount) || 1,
      childCount: parseInt(childCount) || 0,
      adultPrice: parseFloat(adultPrice) || 0,
      childPrice: parseFloat(childPrice) || 0,
    });

    this.loadData();
  },

  onShow() {
    // 从编辑页返回时刷新出行人列表
    if (this.needRefreshTravelers) {
      this.refreshTravelerList();
      this.needRefreshTravelers = false;
    }
  },

  /**
   * 刷新出行人列表（从编辑页返回后）
   */
  async refreshTravelerList() {
    try {
      const travelers = await travelerApi.getList();
      const { travelerType, travelerIndex, adultTravelers, childTravelers, travelerList } = this.data;

      // 找出新增的出行人（在新列表中但不在旧列表中）
      const oldIds = travelerList.map(t => t.id);
      const newTraveler = travelers.find(t => !oldIds.includes(t.id));

      // 如果有新出行人，自动填充到对应位置
      if (newTraveler) {
        if (travelerType === 'adult') {
          adultTravelers[travelerIndex] = newTraveler;
          this.setData({ adultTravelers, travelerList: travelers });
        } else {
          childTravelers[travelerIndex] = newTraveler;
          this.setData({ childTravelers, travelerList: travelers });
        }
      } else {
        this.setData({ travelerList: travelers });
      }
    } catch (err) {
      console.error('刷新出行人列表失败', err);
    }
  },

  /**
   * 加载页面数据
   */
  async loadData() {
    this.setData({ loading: true });

    try {
      // 并行加载数据
      const [route, travelers] = await Promise.all([
        routeApi.getDetail(this.data.routeId),
        travelerApi.getList().catch(() => []),
      ]);

      // 获取套餐信息
      let packageInfo = null;
      if (route.packages) {
        packageInfo = route.packages.find(p => String(p.id) === String(this.data.packageId));
      }
      if (!packageInfo) {
        const packages = await routeApi.getPackages(this.data.routeId).catch(() => []);
        packageInfo = packages.find(p => String(p.id) === String(this.data.packageId));
      }

      // 初始化出行人数组
      const { adultCount, childCount } = this.data;
      const adultTravelers = new Array(adultCount).fill(null);
      const childTravelers = new Array(childCount).fill(null);

      // 自动填充默认出行人
      const defaultTraveler = travelers.find(t => t.isDefault);
      if (defaultTraveler && adultTravelers.length > 0) {
        adultTravelers[0] = defaultTraveler;
      }

      // 初始化联系人（使用默认出行人）
      const contact = defaultTraveler
        ? { name: defaultTraveler.name, phone: defaultTraveler.phone }
        : { name: '', phone: '' };

      this.setData({
        route,
        packageInfo,
        travelerList: travelers,
        adultTravelers,
        childTravelers,
        contact,
        loading: false,
      });

      this.calcAmount();
      this.loadUsableCoupons();
    } catch (err) {
      console.error('加载数据失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
    }
  },

  /**
   * 加载可用优惠券
   */
  async loadUsableCoupons() {
    try {
      const { routeId, totalAmount } = this.data;
      const coupons = await couponApi.getUsable({
        routeId,
        amount: Math.round(totalAmount * 100), // 转分
      });
      this.setData({ coupons: coupons || [] });
    } catch (err) {
      console.error('加载优惠券失败', err);
    }
  },

  /**
   * 计算金额
   */
  calcAmount() {
    const { adultCount, childCount, adultPrice, childPrice, selectedCoupon } = this.data;

    const adultAmount = adultCount * adultPrice;
    const childAmount = childCount * childPrice;
    const totalAmount = adultAmount + childAmount;

    // 优惠券抵扣
    let couponDiscount = 0;
    if (selectedCoupon) {
      if (selectedCoupon.type === 1) {
        // 满减券
        if (totalAmount >= selectedCoupon.minAmount) {
          couponDiscount = selectedCoupon.discount;
        }
      } else if (selectedCoupon.type === 2) {
        // 折扣券
        couponDiscount = totalAmount * (1 - selectedCoupon.discount / 100);
      }
    }

    const payAmount = Math.max(0, totalAmount - couponDiscount);

    this.setData({
      adultAmount,
      childAmount,
      totalAmount,
      couponDiscount,
      payAmount,
    });
  },

  /**
   * 显示出行人选择弹窗
   */
  handleShowTravelerPopup(e) {
    const { type, index } = e.currentTarget.dataset;
    this.setData({
      showTravelerPopup: true,
      travelerType: type,
      travelerIndex: index,
    });
  },

  /**
   * 关闭出行人选择弹窗
   */
  handleCloseTravelerPopup() {
    this.setData({ showTravelerPopup: false });
  },

  /**
   * 选择出行人
   */
  handleSelectTraveler(e) {
    const { traveler } = e.currentTarget.dataset;
    const { travelerType, travelerIndex, adultTravelers, childTravelers } = this.data;

    // 检查是否已选
    const isSelectedInAdult = adultTravelers.some(t => t && t.id === traveler.id);
    const isSelectedInChild = childTravelers.some(t => t && t.id === traveler.id);

    if (isSelectedInAdult || isSelectedInChild) {
      wx.showToast({ title: '该出行人已添加', icon: 'none' });
      return;
    }

    if (travelerType === 'adult') {
      adultTravelers[travelerIndex] = traveler;
      this.setData({ adultTravelers, showTravelerPopup: false });
    } else {
      childTravelers[travelerIndex] = traveler;
      this.setData({ childTravelers, showTravelerPopup: false });
    }
  },

  /**
   * 删除出行人
   */
  handleRemoveTraveler(e) {
    const { type, index } = e.currentTarget.dataset;
    const { adultTravelers, childTravelers } = this.data;

    if (type === 'adult') {
      adultTravelers[index] = null;
      this.setData({ adultTravelers });
    } else {
      childTravelers[index] = null;
      this.setData({ childTravelers });
    }
  },

  /**
   * 跳转到添加出行人页面
   */
  handleShowAddTraveler() {
    // 关闭选择弹窗
    this.setData({ showTravelerPopup: false });
    // 标记需要刷新
    this.needRefreshTravelers = true;
    // 跳转到编辑页
    wx.navigateTo({
      url: '/pages/member/travelers/edit',
    });
  },

  /**
   * 切换使用其他联系人
   */
  handleToggleContact() {
    const { useOtherContact, adultTravelers } = this.data;

    if (useOtherContact) {
      // 切回使用出行人
      const firstTraveler = adultTravelers[0];
      this.setData({
        useOtherContact: false,
        contact: firstTraveler
          ? { name: firstTraveler.name, phone: firstTraveler.phone }
          : { name: '', phone: '' },
      });
    } else {
      this.setData({
        useOtherContact: true,
        contact: { name: '', phone: '' },
      });
    }
  },

  /**
   * 联系人输入
   */
  handleContactInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [`contact.${field}`]: e.detail.value,
    });
  },

  /**
   * 显示优惠券选择弹窗
   */
  handleShowCouponPopup() {
    this.setData({ showCouponPopup: true });
  },

  /**
   * 关闭优惠券弹窗
   */
  handleCloseCouponPopup() {
    this.setData({ showCouponPopup: false });
  },

  /**
   * 选择优惠券
   */
  handleSelectCoupon(e) {
    const { coupon } = e.currentTarget.dataset;
    const { selectedCoupon } = this.data;

    // 再次点击取消选择
    if (selectedCoupon && selectedCoupon.id === coupon.id) {
      this.setData({ selectedCoupon: null, showCouponPopup: false });
    } else {
      this.setData({ selectedCoupon: coupon, showCouponPopup: false });
    }

    this.calcAmount();
  },

  /**
   * 不使用优惠券
   */
  handleNoCoupon() {
    this.setData({ selectedCoupon: null, showCouponPopup: false });
    this.calcAmount();
  },

  /**
   * 提交订单
   */
  async handleSubmit() {
    const {
      routeId, packageId, date, adultCount, childCount,
      adultTravelers, childTravelers, contact, selectedCoupon,
      submitting,
    } = this.data;

    if (submitting) return;

    // 验证出行人
    const validAdultTravelers = adultTravelers.filter(t => t);
    const validChildTravelers = childTravelers.filter(t => t);

    if (validAdultTravelers.length < adultCount) {
      wx.showToast({ title: `请选择${adultCount}位成人出行人`, icon: 'none' });
      return;
    }
    if (validChildTravelers.length < childCount) {
      wx.showToast({ title: `请选择${childCount}位儿童出行人`, icon: 'none' });
      return;
    }

    // 验证联系人
    if (!contact.name.trim()) {
      wx.showToast({ title: '请填写联系人姓名', icon: 'none' });
      return;
    }
    if (!contact.phone.trim()) {
      wx.showToast({ title: '请填写联系人手机号', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });

    try {
      wx.showLoading({ title: '提交订单中...' });

      // 构建出行人列表（合并成人和儿童，按后端要求的格式）
      const travelers = [
        ...validAdultTravelers.map(t => ({
          name: t.name,
          idCard: t.idNo || t.idCard,
          phone: t.phone,
          travelerType: 1, // 成人
        })),
        ...validChildTravelers.map(t => ({
          name: t.name,
          idCard: t.idNo || t.idCard,
          phone: t.phone,
          travelerType: 2, // 儿童
        })),
      ];

      // 创建订单（按后端字段格式）
      const orderResult = await orderApi.create({
        skuId: packageId,
        startDate: date,
        adultCount,
        childCount,
        contactName: contact.name,
        contactPhone: contact.phone,
        travelers,
        couponId: selectedCoupon?.id,
      });

      const orderId = orderResult.orderId || orderResult.id;

      wx.hideLoading();

      // 发起支付
      await this.doPay(orderId);

    } catch (err) {
      wx.hideLoading();
      console.error('提交订单失败', err);
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
      this.setData({ submitting: false });
    }
  },

  /**
   * 发起支付
   */
  async doPay(orderId) {
    try {
      wx.showLoading({ title: '正在支付...' });

      // 创建支付
      const payParams = await payApi.createOrderPayment(orderId);
      wx.hideLoading();

      // 调起微信支付
      await payApi.wxPay(payParams);

      // 支付成功
      wx.showToast({ title: '支付成功', icon: 'success' });

      // 跳转订单详情
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/order/detail/index?id=${orderId}`,
        });
      }, 1500);

    } catch (err) {
      wx.hideLoading();
      this.setData({ submitting: false });

      if (err.code === -2) {
        // 用户取消支付
        wx.showModal({
          title: '提示',
          content: '订单已创建，您可以稍后在订单列表中继续支付',
          confirmText: '查看订单',
          cancelText: '继续购物',
          success: (res) => {
            if (res.confirm) {
              wx.redirectTo({ url: `/pages/order/detail/index?id=${orderId}` });
            } else {
              wx.navigateBack();
            }
          },
        });
      } else {
        wx.showToast({ title: err.message || '支付失败', icon: 'none' });
      }
    }
  },

  /**
   * 返回
   */
  handleBack() {
    wx.navigateBack();
  },
});
