/**
 * xj-tag - 标签组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 类型: primary | success | warning | error | default
    type: {
      type: String,
      value: 'default'
    },
    // 尺寸: sm | md
    size: {
      type: String,
      value: 'md'
    },
    // 是否镂空
    plain: {
      type: Boolean,
      value: false
    },
    // 是否可关闭
    closable: {
      type: Boolean,
      value: false
    },
    // 是否圆角
    round: {
      type: Boolean,
      value: false
    },
    // 自定义颜色
    color: {
      type: String,
      value: ''
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  },

  methods: {
    handleClose() {
      this.triggerEvent('close');
    },

    handleTap() {
      this.triggerEvent('tap');
    }
  }
});
