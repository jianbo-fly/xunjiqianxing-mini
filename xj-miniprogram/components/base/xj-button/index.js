/**
 * xj-button - 按钮组件
 */
Component({
  options: {
    multipleSlots: true,
    addGlobalClass: true
  },

  properties: {
    // 按钮类型: primary | secondary | outline | text | danger
    type: {
      type: String,
      value: 'primary'
    },
    // 按钮尺寸: sm | md | lg
    size: {
      type: String,
      value: 'md'
    },
    // 是否块级按钮
    block: {
      type: Boolean,
      value: false
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      value: false
    },
    // 是否加载中
    loading: {
      type: Boolean,
      value: false
    },
    // 圆角类型: default | round | square
    shape: {
      type: String,
      value: 'default'
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    },
    // 开放能力
    openType: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleTap(e) {
      if (this.data.disabled || this.data.loading) return;
      this.triggerEvent('tap', e);
    },

    handleGetUserInfo(e) {
      this.triggerEvent('getuserinfo', e.detail);
    },

    handleContact(e) {
      this.triggerEvent('contact', e.detail);
    },

    handleGetPhoneNumber(e) {
      this.triggerEvent('getphonenumber', e.detail);
    },

    handleError(e) {
      this.triggerEvent('error', e.detail);
    },

    handleOpenSetting(e) {
      this.triggerEvent('opensetting', e.detail);
    },

    handleLaunchApp(e) {
      this.triggerEvent('launchapp', e.detail);
    }
  }
});
