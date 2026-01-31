/**
 * xj-popup - 弹出层组件
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
    // 弹出位置: center | top | bottom | left | right
    position: {
      type: String,
      value: 'bottom'
    },
    // 是否显示圆角
    round: {
      type: Boolean,
      value: true
    },
    // 是否显示关闭按钮
    closable: {
      type: Boolean,
      value: false
    },
    // 标题
    title: {
      type: String,
      value: ''
    },
    // 点击遮罩是否关闭
    closeOnClickOverlay: {
      type: Boolean,
      value: true
    },
    // 是否显示遮罩
    overlay: {
      type: Boolean,
      value: true
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
        this.handleClose();
      }
    },

    handleClose() {
      this.triggerEvent('close');
    },

    // 阻止冒泡
    noop() {}
  }
});
