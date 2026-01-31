/**
 * Pay Service - 支付相关API
 */

const { post, get } = require('./request');
const { paths } = require('../config/api');

const payApi = {
  /**
   * 创建订单支付
   * @param {number} orderId - 订单ID
   */
  createOrderPayment(orderId) {
    return post(`${paths.payment.orderPay}/${orderId}`);
  },

  /**
   * 创建会员支付
   * @param {number} orderId - 会员订单ID
   */
  createMemberPayment(orderId) {
    return post(`${paths.payment.memberPay}/${orderId}`);
  },

  /**
   * 查询支付状态
   * @param {string} paymentNo - 支付单号
   */
  queryStatus(paymentNo) {
    return get(`${paths.payment.status}/${paymentNo}`);
  },

  /**
   * 调起微信支付
   * @param {Object} payParams - 支付参数（从create接口返回）
   */
  wxPay(payParams) {
    return new Promise((resolve, reject) => {
      wx.requestPayment({
        timeStamp: payParams.timeStamp,
        nonceStr: payParams.nonceStr,
        package: payParams.package,
        signType: payParams.signType || 'MD5',
        paySign: payParams.paySign,
        success: (res) => {
          resolve(res);
        },
        fail: (err) => {
          if (err.errMsg.includes('cancel')) {
            reject({ code: -2, message: '用户取消支付' });
          } else {
            reject({ code: -1, message: err.errMsg || '支付失败' });
          }
        }
      });
    });
  },
};

module.exports = payApi;
