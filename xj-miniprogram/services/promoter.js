/**
 * Promoter Service - 推广员相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const promoterApi = {
  /**
   * 获取推广员信息
   */
  getInfo() {
    return get(paths.promoter.info);
  },

  /**
   * 申请成为推广员
   * @param {Object} params
   * @param {string} params.realName - 真实姓名
   * @param {string} params.phone - 手机号
   * @param {string} params.reason - 申请理由
   */
  apply(params) {
    return post(paths.promoter.apply, params);
  },

  /**
   * 绑定推广员
   * @param {Object} params
   * @param {string} params.promoterCode - 推广员邀请码
   */
  bind(params) {
    return post(paths.promoter.bind, params);
  },

  /**
   * 获取佣金记录
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   */
  getCommissions(params) {
    return get(paths.promoter.commissions, params);
  },

  /**
   * 申请提现
   * @param {Object} params
   * @param {number} params.amount - 提现金额（分）
   */
  withdraw(params) {
    return post(paths.promoter.withdraw, params);
  },

  /**
   * 获取提现记录
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   */
  getWithdraws(params) {
    return get(paths.promoter.withdraws, params);
  },

  /**
   * 获取推广统计
   */
  getStatistics() {
    return get(paths.promoter.statistics);
  },
};

module.exports = promoterApi;
