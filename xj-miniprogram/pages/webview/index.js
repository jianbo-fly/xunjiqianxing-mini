/**
 * 网页容器页
 * 用于加载外部H5页面
 */

Page({
  data: {
    // 网页URL
    url: '',
    // 页面标题
    title: '',
    // 加载状态
    loading: true,
  },

  onLoad(options) {
    // 获取URL参数
    if (options.url) {
      const url = decodeURIComponent(options.url);
      this.setData({ url });
    }
    // 获取标题参数
    if (options.title) {
      const title = decodeURIComponent(options.title);
      this.setData({ title });
      wx.setNavigationBarTitle({ title });
    }
  },

  /**
   * 加载完成
   */
  handleLoad() {
    this.setData({ loading: false });
  },

  /**
   * 加载失败
   */
  handleError(e) {
    console.error('WebView加载失败', e);
    this.setData({ loading: false });
    wx.showToast({
      title: '页面加载失败',
      icon: 'none',
    });
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: this.data.title || '寻迹千行',
      path: `/pages/webview/index?url=${encodeURIComponent(this.data.url)}`,
    };
  },
});
