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
   * @param {number} params.packageId - 套餐ID
   * @param {string} params.date - 出行日期
   * @param {number} params.adultCount - 成人数量
   * @param {number} params.childCount - 儿童数量
   */
  getConfirmInfo(params) {
    return get(paths.order.confirm, params);
  },

  /**
   * 创建订单
   * @param {Object} params
   * @param {number} params.routeId - 线路ID
   * @param {number} params.packageId - 套餐ID
   * @param {string} params.date - 出行日期
   * @param {number} params.adultCount - 成人数量
   * @param {number} params.childCount - 儿童数量
   * @param {Array} params.adultTravelers - 成人出行人列表
   * @param {Array} params.childTravelers - 儿童出行人列表
   * @param {Object} params.contact - 联系人信息
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
   * @param {string} reason - 取消原因
   */
  cancel(id, reason) {
    const params = reason ? { reason } : {};
    return post(`${paths.order.detail}/${id}/cancel`, params);
  },

  /**
   * 申请退款
   * @param {number} id - 订单ID
   * @param {string} reason - 退款原因
   */
  refund(id, reason) {
    return post(`${paths.order.detail}/${id}/refund`, { reason });
  },
};

module.exports = orderApi;
