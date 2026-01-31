/**
 * Custom Service - 定制游相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const customApi = {
  /**
   * 提交定制需求
   * @param {Object} params
   */
  submit(params) {
    return post(paths.custom.submit, params);
  },

  /**
   * 获取定制需求列表
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   */
  getList(params) {
    return get(paths.custom.list, params);
  },

  /**
   * 获取定制需求详情
   * @param {number} id - 需求ID
   */
  getDetail(id) {
    return get(`${paths.custom.detail}/${id}`);
  },

  /**
   * 取消定制需求
   * @param {number} id - 需求ID
   */
  cancel(id) {
    return post(`${paths.custom.cancel}/${id}/cancel`);
  },
};

module.exports = customApi;
