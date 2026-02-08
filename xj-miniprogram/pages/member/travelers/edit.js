/**
 * 添加/编辑出行人页面
 */
const travelerApi = require('../../../services/traveler');

Page({
  data: {
    isEdit: false,
    travelerId: '',
    loading: false,
    submitting: false,

    // 表单数据
    form: {
      name: '',
      idType: 1,
      idNo: '',
      phone: '',
      isDefault: false,
    },

    // 证件类型选项
    idTypeOptions: [
      { value: 1, label: '身份证' },
      { value: 2, label: '护照' },
      { value: 3, label: '港澳通行证' },
      { value: 4, label: '台湾通行证' },
    ],
    idTypeIndex: 0,
  },

  onLoad(options) {
    if (options.id) {
      this.setData({
        isEdit: true,
        travelerId: options.id,
      });
      wx.setNavigationBarTitle({ title: '编辑出行人' });
      this.loadTravelerDetail();
    } else {
      wx.setNavigationBarTitle({ title: '添加出行人' });
    }
  },

  /**
   * 加载出行人详情
   */
  async loadTravelerDetail() {
    this.setData({ loading: true });

    try {
      const detail = await travelerApi.getDetail(this.data.travelerId);
      const idTypeIndex = this.data.idTypeOptions.findIndex(
        (opt) => opt.value === detail.idType
      );

      this.setData({
        form: {
          name: detail.name || '',
          idType: detail.idType || 1,
          idNo: detail.idNo || '',
          phone: detail.phone || '',
          isDefault: detail.isDefault || false,
        },
        idTypeIndex: idTypeIndex >= 0 ? idTypeIndex : 0,
        loading: false,
      });
    } catch (err) {
      console.error('加载出行人详情失败', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      this.setData({ loading: false });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  /**
   * 输入处理
   */
  handleInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [`form.${field}`]: e.detail.value,
    });
  },

  /**
   * 证件类型选择
   */
  handleIdTypeChange(e) {
    const index = parseInt(e.detail.value);
    const idType = this.data.idTypeOptions[index].value;
    this.setData({
      idTypeIndex: index,
      'form.idType': idType,
    });
  },

  /**
   * 默认开关
   */
  handleDefaultChange(e) {
    this.setData({
      'form.isDefault': e.detail.value,
    });
  },

  /**
   * 表单验证
   */
  validateForm() {
    const { name, idNo, phone } = this.data.form;

    if (!name.trim()) {
      wx.showToast({ title: '请输入姓名', icon: 'none' });
      return false;
    }

    if (!idNo.trim()) {
      wx.showToast({ title: '请输入证件号码', icon: 'none' });
      return false;
    }

    // 简单验证身份证格式
    if (this.data.form.idType === 1) {
      const idCardReg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
      if (!idCardReg.test(idNo)) {
        wx.showToast({ title: '请输入正确的身份证号', icon: 'none' });
        return false;
      }
    }

    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return false;
    }

    const phoneReg = /^1[3-9]\d{9}$/;
    if (!phoneReg.test(phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return false;
    }

    return true;
  },

  /**
   * 提交保存
   */
  async handleSubmit() {
    if (!this.validateForm()) return;
    if (this.data.submitting) return;

    this.setData({ submitting: true });

    try {
      wx.showLoading({ title: '保存中...' });

      const { form, isEdit, travelerId } = this.data;

      if (isEdit) {
        await travelerApi.update(travelerId, form);
      } else {
        await travelerApi.add(form);
      }

      wx.hideLoading();
      wx.showToast({ title: '保存成功', icon: 'success' });

      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      console.error('保存出行人失败', err);
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
      this.setData({ submitting: false });
    }
  },
});
