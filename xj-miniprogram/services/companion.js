/**
 * Companion Service - 搭子相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const companionApi = {
  /**
   * 获取搭子列表
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   * @param {number} params.routeId - 线路ID（可选）
   * @param {string} params.date - 出行日期（可选）
   */
  getList(params) {
    return get(paths.companion.list, params);
  },

  /**
   * 获取搭子详情
   * @param {number} id - 搭子ID
   */
  getDetail(id) {
    return get(`${paths.companion.detail}/${id}`);
  },

  /**
   * 加入搭子
   * @param {number} id - 搭子ID
   * @param {string} message - 留言
   */
  join(id, message) {
    return post(paths.companion.join, { id, message });
  },

  /**
   * 发起搭子
   * @param {Object} params
   * @param {number} params.routeId - 线路ID
   * @param {string} params.date - 出行日期
   * @param {number} params.targetCount - 期望人数
   * @param {string} params.description - 描述
   */
  create(params) {
    return post(paths.companion.create, params);
  },
};

module.exports = companionApi;
