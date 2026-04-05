/**
 * nav-bar - 自定义导航栏组件
 */
Component({
  options: {
    multipleSlots: true,
    addGlobalClass: true
  },

  properties: {
    // 标题
    title: {
      type: String,
      value: ''
    },
    // 背景色
    background: {
      type: String,
      value: '#FFFFFF'
    },
    // 文字颜色主题: light | dark
    theme: {
      type: String,
      value: 'dark'
    },
    // 是否显示返回按钮
    showBack: {
      type: Boolean,
      value: true
    },
    // 是否显示首页按钮
    showHome: {
      type: Boolean,
      value: false
    },
    // 是否固定在顶部
    fixed: {
      type: Boolean,
      value: true
    },
    // 是否显示底部边框
    border: {
      type: Boolean,
      value: true
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  },

  data: {
    statusBarHeight: 0,
    navBarHeight: 44
  },

  lifetimes: {
    attached() {
      const windowInfo = wx.getWindowInfo();

      let navBarHeight = 44;
      try {
        const menuButton = wx.getMenuButtonBoundingClientRect();
        navBarHeight = menuButton.height + (menuButton.top - windowInfo.statusBarHeight) * 2;
      } catch (e) {
        console.warn('获取胶囊按钮信息失败');
      }

      this.setData({
        statusBarHeight: windowInfo.statusBarHeight,
        navBarHeight: navBarHeight
      });
    }
  },

  methods: {
    handleBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
      } else {
        wx.switchTab({ url: '/pages/index/index' });
      }
      this.triggerEvent('back');
    },

    handleHome() {
      wx.switchTab({ url: '/pages/index/index' });
      this.triggerEvent('home');
    }
  }
});
