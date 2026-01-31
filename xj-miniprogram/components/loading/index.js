/**
 * 加载组件
 */
Component({
  properties: {
    // 加载文字
    text: {
      type: String,
      value: '加载中...',
    },
    // 是否垂直排列
    vertical: {
      type: Boolean,
      value: false,
    },
    // 尺寸
    size: {
      type: String,
      value: 'default', // small, default, large
    },
  },
});
