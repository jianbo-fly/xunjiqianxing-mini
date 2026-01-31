/**
 * form-validate - 表单验证行为
 */

const { validate } = require('../utils/validate');

module.exports = Behavior({
  data: {
    // 表单数据
    formData: {},
    // 错误信息
    formErrors: {},
  },

  methods: {
    /**
     * 设置表单字段值
     */
    setFieldValue(field, value) {
      this.setData({
        [`formData.${field}`]: value,
        [`formErrors.${field}`]: '', // 清除错误
      });
    },

    /**
     * 设置多个字段值
     */
    setFieldsValue(values) {
      const data = {};
      for (const key in values) {
        data[`formData.${key}`] = values[key];
        data[`formErrors.${key}`] = '';
      }
      this.setData(data);
    },

    /**
     * 获取表单数据
     */
    getFieldsValue() {
      return { ...this.data.formData };
    },

    /**
     * 验证表单
     * @param {Object} rules - 验证规则
     * @returns {Object} { isValid, errors }
     */
    validateForm(rules) {
      const result = validate(this.data.formData, rules);

      if (!result.isValid) {
        // 设置错误信息
        const formErrors = {};
        let index = 0;
        for (const field in rules) {
          if (result.errors[index]) {
            formErrors[field] = result.errors[index];
          }
          index++;
        }
        this.setData({ formErrors });

        // 显示第一个错误
        wx.showToast({
          title: result.error,
          icon: 'none'
        });
      }

      return result;
    },

    /**
     * 重置表单
     */
    resetForm() {
      this.setData({
        formData: {},
        formErrors: {},
      });
    },

    /**
     * 输入事件处理
     */
    onInput(e) {
      const { field } = e.currentTarget.dataset;
      const { value } = e.detail;
      if (field) {
        this.setFieldValue(field, value);
      }
    },
  },
});
