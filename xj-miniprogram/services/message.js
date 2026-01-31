/**
 * Message Service - 消息相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const messageApi = {
  /**
   * 获取消息列表
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   * @param {number} params.type - 消息类型（可选）
   */
  getList(params) {
    return get(paths.message.list, params);
  },

  /**
   * 标记消息已读
   * @param {number} id - 消息ID
   */
  read(id) {
    return post(paths.message.read, { id });
  },

  /**
   * 获取未读消息数量
   */
  getUnreadCount() {
    return get(paths.message.unreadCount);
  },
};

module.exports = messageApi;
