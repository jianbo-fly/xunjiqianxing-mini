/**
 * Order Service - 订单相关API
 */

const { get, post, put } = require('./request');
const { paths } = require('../config/api');

const orderApi = {
  /**
   * 获取订单确认信息
   * @param {Object} params
   * @param {number} params.routeId - 线路ID
   * @param {number} params.skuId - SKU ID
   * @param {string} params.date - 出行日期
   * @param {number} params.quantity - 数量
   */
  getConfirmInfo(params) {
    return get(paths.order.confirm, params);
  },

  /**
   * 创建订单
   * @param {Object} params
   * @param {number} params.routeId - 线路ID
   * @param {number} params.skuId - SKU ID
   * @param {string} params.date - 出行日期
   * @param {number} params.quantity - 数量
   * @param {Array} params.travelers - 出行人信息
   * @param {number} params.couponId - 优惠券ID
   * @param {string} params.remark - 备注
   */
  create(params) {
    return post(paths.order.create, params);
  },

  /**
   * 获取订单列表
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   * @param {number} params.status - 订单状态
   */
  getList(params) {
    return get(paths.order.list, params);
  },

  /**
   * 获取订单详情
   * @param {number|string} id - 订单ID或订单号
   */
  getDetail(id) {
    return get(`${paths.order.detail}/${id}`);
  },

  /**
   * 取消订单
   * @param {number} id - 订单ID
   */
  cancel(id) {
    return post(`${paths.order.cancel}/${id}/cancel`);
  },

  /**
   * 申请退款
   * @param {number} id - 订单ID
   * @param {Object} params
   * @param {string} params.reason - 退款原因
   * @param {string} params.description - 退款说明
   */
  refund(id, params) {
    return post(`${paths.order.refund}/${id}/refund`, params);
  },
};

module.exports = orderApi;
