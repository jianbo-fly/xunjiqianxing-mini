/**
 * Favorite Service - 收藏相关API
 */

const { get, post } = require('./request');
const { paths } = require('../config/api');

const favoriteApi = {
  /**
   * 获取收藏列表
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   */
  getList(params) {
    return get(paths.favorite.list, params);
  },

  /**
   * 添加收藏
   * @param {number} routeId - 线路ID
   */
  add(routeId) {
    return post(paths.favorite.add, { routeId });
  },

  /**
   * 取消收藏
   * @param {number} routeId - 线路ID
   */
  remove(routeId) {
    return post(paths.favorite.remove, { routeId });
  },

  /**
   * 检查是否收藏
   * @param {number} routeId - 线路ID
   */
  check(routeId) {
    return get(paths.favorite.check, { routeId });
  },

  /**
   * 切换收藏状态
   * @param {number} routeId - 线路ID
   * @param {boolean} isFavorite - 当前是否已收藏
   */
  toggle(routeId, isFavorite) {
    return isFavorite ? this.remove(routeId) : this.add(routeId);
  },
};

module.exports = favoriteApi;
