/**
 * Home Service - 首页相关API
 */

const { get } = require('./request');
const { paths } = require('../config/api');

const homeApi = {
  /**
   * 获取首页数据（轮播图+推荐线路）
   */
  getData() {
    return get(paths.home.data);
  },

  /**
   * 获取Banner列表
   */
  getBanners() {
    return get(paths.home.banners);
  },

  /**
   * 获取推荐线路
   */
  getRecommend() {
    return get(paths.home.recommend);
  },
};

module.exports = homeApi;
