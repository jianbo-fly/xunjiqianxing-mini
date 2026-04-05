/**
 * Message Service - 消息相关API
 */
const { get, post } = require('./request');
const { paths } = require('../config/api');

const messageApi = {
  /**
   * 获取消息列表
   * @param {Object} params
   * @param {number} params.page
   * @param {number} params.pageSize
   */
  getList(params) {
    return get(paths.message.list, params);
  },

  /**
   * 标记已读
   * @param {number|string} id - 消息ID，传 'all' 表示全部已读
   */
  markRead(id) {
    return post(`${paths.message.read}/${id}`);
  },

  /**
   * 获取未读数量
   */
  getUnreadCount() {
    return get(paths.message.unreadCount);
  },
};

module.exports = messageApi;
