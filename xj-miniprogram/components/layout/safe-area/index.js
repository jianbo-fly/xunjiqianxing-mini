/**
 * safe-area - 安全区域组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 位置: top | bottom
    position: {
      type: String,
      value: 'bottom'
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  }
});
