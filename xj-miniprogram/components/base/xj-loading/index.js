/**
 * xj-loading - 加载组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 类型: spinner | dots
    type: {
      type: String,
      value: 'spinner'
    },
    // 尺寸: sm | md | lg
    size: {
      type: String,
      value: 'md'
    },
    // 颜色
    color: {
      type: String,
      value: ''
    },
    // 文字
    text: {
      type: String,
      value: ''
    },
    // 是否垂直布局
    vertical: {
      type: Boolean,
      value: false
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  }
});
