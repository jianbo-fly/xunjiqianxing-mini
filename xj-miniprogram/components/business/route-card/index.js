/**
 * route-card - 线路卡片组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 线路数据
    route: {
      type: Object,
      value: {}
    },
    // 卡片模式: normal | compact | horizontal
    mode: {
      type: String,
      value: 'normal'
    },
    // 是否显示收藏按钮
    showFavorite: {
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
    handleTap() {
      this.triggerEvent('tap', { route: this.data.route });
    },

    handleFavorite(e) {
      this.triggerEvent('favorite', {
        route: this.data.route,
        isFavorite: !this.data.route.isFavorite
      });
    }
  }
});
