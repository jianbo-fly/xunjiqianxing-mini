/**
 * xj-card - 卡片组件
 */
Component({
  options: {
    multipleSlots: true,
    addGlobalClass: true
  },

  properties: {
    // 是否显示边框
    bordered: {
      type: Boolean,
      value: false
    },
    // 阴影: none | sm | md | lg
    shadow: {
      type: String,
      value: 'sm'
    },
    // 内边距: none | sm | md | lg
    padding: {
      type: String,
      value: 'md'
    },
    // 圆角: none | sm | md | lg
    radius: {
      type: String,
      value: 'md'
    },
    // 是否可点击
    clickable: {
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
    handleTap() {
      if (this.data.clickable) {
        this.triggerEvent('tap');
      }
    }
  }
});
