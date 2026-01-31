/**
 * xj-icon - 图标组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 图标名称（对应assets中的图标）
    name: {
      type: String,
      value: ''
    },
    // 图标尺寸: sm | md | lg 或自定义rpx值
    size: {
      type: String,
      value: 'md'
    },
    // 图标颜色（仅对SVG有效，PNG无效）
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

  data: {
    iconSize: '40rpx'
  },

  observers: {
    'size': function(size) {
      const sizeMap = {
        sm: 'var(--icon-size-sm)',
        md: 'var(--icon-size-md)',
        lg: 'var(--icon-size-lg)'
      };
      this.setData({
        iconSize: sizeMap[size] || size
      });
    }
  },

  methods: {
    handleTap() {
      this.triggerEvent('tap');
    }
  }
});
