Component({
  data: {
    selected: 0,
    color: '#9CA3AF',
    selectedColor: '#EC3713',
    list: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        iconPath: '/assets/icons/tabbar/home.png',
        selectedIconPath: '/assets/icons/tabbar/home-active.png'
      },
      {
        pagePath: '/pages/companion/list/index',
        text: '搭子',
        iconPath: '/assets/icons/tabbar/companion.png',
        selectedIconPath: '/assets/icons/tabbar/companion-active.png'
      },
      {
        pagePath: '/pages/trip/list/index',
        text: '行程',
        iconPath: '/assets/icons/tabbar/trip.png',
        selectedIconPath: '/assets/icons/tabbar/trip-active.png'
      },
      {
        pagePath: '/pages/member/index/index',
        text: '我的',
        iconPath: '/assets/icons/tabbar/member.png',
        selectedIconPath: '/assets/icons/tabbar/member-active.png'
      }
    ]
  },

  methods: {
    switchTab(e) {
      const { path } = e.currentTarget.dataset;
      wx.switchTab({ url: path });
    }
  }
});
