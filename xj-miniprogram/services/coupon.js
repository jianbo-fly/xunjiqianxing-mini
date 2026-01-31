/**
 * Coupon Service - 优惠券相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const couponApi = {
  /**
   * 获取可领取的优惠券
   */
  getAvailable() {
    return get(paths.coupon.available);
  },

  /**
   * 领取优惠券
   * @param {number} id - 优惠券ID
   */
  receive(id) {
    return post(`${paths.coupon.receive}/${id}`);
  },

  /**
   * 获取我的优惠券列表
   * @param {Object} params
   * @param {number} params.status - 状态：0未使用 1已使用 2已过期
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   */
  getMy(params) {
    return get(paths.coupon.my, params);
  },

  /**
   * 获取订单可用优惠券
   * @param {Object} params
   * @param {number} params.routeId - 线路ID
   * @param {number} params.amount - 订单金额（分）
   */
  getUsable(params) {
    return get(paths.coupon.usable, params);
  },
};

module.exports = couponApi;
