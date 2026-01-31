/**
 * Common Service - 公共API
 */

const { get } = require('./request');
const { upload } = require('./request');
const { paths } = require('../config/api');

const commonApi = {
  /**
   * 上传文件
   * @param {string} filePath - 文件路径
   */
  upload(filePath) {
    return upload(filePath);
  },

  /**
   * 获取系统配置
   */
  getConfig() {
    return get(paths.common.config);
  },

  /**
   * 获取地区列表
   */
  getRegions() {
    return get(paths.common.regions);
  },
};

module.exports = commonApi;
