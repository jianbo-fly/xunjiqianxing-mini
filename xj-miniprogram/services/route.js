/**
 * Route Service - 线路相关API
 */

const { get } = require('./request');
const { paths } = require('../config/api');

const routeApi = {
  /**
   * 获取线路列表
   * @param {Object} params
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页数量
   * @param {string} params.keyword - 搜索关键词
   * @param {number} params.categoryId - 分类ID
   * @param {string} params.sortBy - 排序方式
   */
  getList(params) {
    return get(paths.route.list, params);
  },

  /**
   * 获取线路详情
   * @param {number} id - 线路ID
   */
  getDetail(id) {
    return get(`${paths.route.detail}/${id}`);
  },

  /**
   * 获取线路套餐列表
   * @param {number} routeId - 线路ID
   */
  getPackages(routeId) {
    return get(`${paths.route.packages}/${routeId}/packages`);
  },

  /**
   * 获取套餐详情
   * @param {number} packageId - 套餐ID
   */
  getPackageDetail(packageId) {
    return get(`${paths.route.packageDetail}/${packageId}`);
  },

  /**
   * 获取套餐价格日历
   * @param {number} packageId - 套餐ID
   * @param {string} startDate - 开始日期 yyyy-MM-dd
   * @param {string} endDate - 结束日期 yyyy-MM-dd
   */
  getPriceCalendar(packageId, startDate, endDate) {
    return get(`${paths.route.calendar}/${packageId}/calendar`, { startDate, endDate });
  },
};

module.exports = routeApi;
