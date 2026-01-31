/**
 * order-card - 订单卡片组件
 */
const { ORDER_STATUS_TEXT, ORDER_STATUS_COLOR } = require('../../../config/constants');

Component({
  options: {
    addGlobalClass: true
  },

  properties: {
    // 订单数据
    order: {
      type: Object,
      value: {}
    },
    // 是否显示操作按钮
    showActions: {
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
    statusText: '',
    statusColor: ''
  },

  observers: {
    'order.status': function(status) {
      this.setData({
        statusText: ORDER_STATUS_TEXT[status] || '未知',
        statusColor: ORDER_STATUS_COLOR[status] || '#999999'
      });
    }
  },

  methods: {
    handleTap() {
      this.triggerEvent('tap', { order: this.data.order });
    },

    handlePay() {
      this.triggerEvent('pay', { order: this.data.order });
    },

    handleCancel() {
      this.triggerEvent('cancel', { order: this.data.order });
    },

    handleRefund() {
      this.triggerEvent('refund', { order: this.data.order });
    },

    handleReview() {
      this.triggerEvent('review', { order: this.data.order });
    }
  }
});
