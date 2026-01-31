/**
 * xj-input - 输入框组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 输入值
    value: {
      type: String,
      value: ''
    },
    // 输入类型
    type: {
      type: String,
      value: 'text'
    },
    // 占位符
    placeholder: {
      type: String,
      value: '请输入'
    },
    // 最大长度
    maxlength: {
      type: Number,
      value: 140
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      value: false
    },
    // 是否聚焦
    focus: {
      type: Boolean,
      value: false
    },
    // 是否密码类型
    password: {
      type: Boolean,
      value: false
    },
    // 是否显示清除按钮
    clearable: {
      type: Boolean,
      value: false
    },
    // 标签
    label: {
      type: String,
      value: ''
    },
    // 是否必填
    required: {
      type: Boolean,
      value: false
    },
    // 错误提示
    error: {
      type: String,
      value: ''
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  },

  data: {
    inputValue: ''
  },

  observers: {
    'value': function(value) {
      this.setData({ inputValue: value });
    }
  },

  methods: {
    handleInput(e) {
      const value = e.detail.value;
      this.setData({ inputValue: value });
      this.triggerEvent('input', { value });
    },

    handleFocus(e) {
      this.triggerEvent('focus', e.detail);
    },

    handleBlur(e) {
      this.triggerEvent('blur', e.detail);
    },

    handleConfirm(e) {
      this.triggerEvent('confirm', e.detail);
    },

    handleClear() {
      this.setData({ inputValue: '' });
      this.triggerEvent('input', { value: '' });
      this.triggerEvent('clear');
    }
  }
});
