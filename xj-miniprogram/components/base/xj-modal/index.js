/**
 * xj-modal - 弹窗组件
 */
Component({
  options: {
    multipleSlots: true,
    addGlobalClass: true
  },

  properties: {
    // 是否显示
    visible: {
      type: Boolean,
      value: false
    },
    // 标题
    title: {
      type: String,
      value: ''
    },
    // 内容
    content: {
      type: String,
      value: ''
    },
    // 是否显示取消按钮
    showCancel: {
      type: Boolean,
      value: true
    },
    // 取消按钮文字
    cancelText: {
      type: String,
      value: '取消'
    },
    // 确认按钮文字
    confirmText: {
      type: String,
      value: '确定'
    },
    // 确认按钮颜色
    confirmColor: {
      type: String,
      value: ''
    },
    // 点击遮罩是否关闭
    closeOnClickOverlay: {
      type: Boolean,
      value: false
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleOverlayTap() {
      if (this.data.closeOnClickOverlay) {
        this.handleCancel();
      }
    },

    handleCancel() {
      this.triggerEvent('cancel');
      this.triggerEvent('close');
    },

    handleConfirm() {
      this.triggerEvent('confirm');
    },

    // 阻止冒泡
    noop() {}
  }
});
