/**
 * xj-empty - 空状态组件
 */
const assets = require('../../../assets/index');

Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 类型: default | order | favorite | network | search | message | coupon
    type: {
      type: String,
      value: 'default'
    },
    // 自定义图片
    image: {
      type: String,
      value: ''
    },
    // 主描述文字（标题）
    description: {
      type: String,
      value: ''
    },
    // 副标题
    subtitle: {
      type: String,
      value: ''
    },
    // 操作按钮文字（有值才显示按钮）
    buttonText: {
      type: String,
      value: ''
    },
    // 卡片模式（白色圆角卡片 + 右上角×徽章）
    cardMode: {
      type: Boolean,
      value: false
    },
    // 自定义类名
    customClass: {
      type: String,
      value: ''
    }
  },

  data: {
    emptyImage: '',
    emptyText: ''
  },

  observers: {
    'type, image, description': function(type, image, description) {
      const imageMap = {
        default: assets.empty.default,
        order: assets.empty.order,
        favorite: assets.empty.favorite,
        network: assets.empty.network,
        search: assets.empty.search,
        message: assets.empty.message,
        coupon: assets.empty.coupon,
        route: assets.empty.route,
      };

      const textMap = {
        default: '暂无数据',
        order: '暂无订单',
        favorite: '暂无收藏',
        network: '网络异常',
        search: '未找到相关内容',
        message: '暂无消息',
        coupon: '暂无优惠券',
        route: '暂无相关路线',
      };

      this.setData({
        emptyImage: image || imageMap[type] || assets.empty.default,
        emptyText: description || textMap[type] || '暂无数据'
      });
    }
  },

  methods: {
    handleAction() {
      this.triggerEvent('action');
    }
  }
});
