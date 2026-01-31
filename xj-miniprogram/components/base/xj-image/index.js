/**
 * xj-image - 图片组件
 */
Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 图片地址
    src: {
      type: String,
      value: ''
    },
    // 裁剪模式
    mode: {
      type: String,
      value: 'aspectFill'
    },
    // 宽度
    width: {
      type: String,
      value: '100%'
    },
    // 高度
    height: {
      type: String,
      value: '100%'
    },
    // 圆角
    radius: {
      type: String,
      value: '0'
    },
    // 懒加载
    lazyLoad: {
      type: Boolean,
      value: true
    },
    // 占位图
    placeholder: {
      type: String,
      value: '/assets/images/placeholder/image.png'
    },
    // 加载失败时显示的图片
    errorSrc: {
      type: String,
      value: '/assets/images/placeholder/image.png'
    },
    // 是否显示加载中
    showLoading: {
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
    loading: true,
    error: false,
    currentSrc: ''
  },

  observers: {
    'src': function(src) {
      this.setData({
        currentSrc: src || this.data.placeholder,
        loading: !!src,
        error: false
      });
    }
  },

  lifetimes: {
    attached() {
      this.setData({
        currentSrc: this.data.src || this.data.placeholder
      });
    }
  },

  methods: {
    handleLoad() {
      this.setData({ loading: false });
      this.triggerEvent('load');
    },

    handleError() {
      this.setData({
        loading: false,
        error: true,
        currentSrc: this.data.errorSrc
      });
      this.triggerEvent('error');
    },

    handleTap() {
      this.triggerEvent('tap');
    }
  }
});
