/**
 * Traveler Service - 出行人相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const travelerApi = {
  /**
   * 获取出行人列表
   */
  getList() {
    return get(paths.traveler.list);
  },

  /**
   * 获取出行人详情
   * @param {number} id - 出行人ID
   */
  getDetail(id) {
    return get(`${paths.traveler.detail}/${id}`);
  },

  /**
   * 添加出行人
   * @param {Object} params
   * @param {string} params.name - 姓名
   * @param {number} params.idType - 证件类型
   * @param {string} params.idNo - 证件号码
   * @param {string} params.phone - 手机号
   * @param {boolean} params.isDefault - 是否默认
   */
  add(params) {
    return post(paths.traveler.add, params);
  },

  /**
   * 更新出行人
   * @param {number} id - 出行人ID
   * @param {Object} params
   */
  update(id, params) {
    return post(`${paths.traveler.update}/${id}/update`, params);
  },

  /**
   * 删除出行人
   * @param {number} id - 出行人ID
   */
  delete(id) {
    return post(`${paths.traveler.delete}/${id}/delete`);
  },

  /**
   * 设为默认出行人
   * @param {number} id - 出行人ID
   */
  setDefault(id) {
    return post(`${paths.traveler.setDefault}/${id}/setDefault`);
  },
};

module.exports = travelerApi;
