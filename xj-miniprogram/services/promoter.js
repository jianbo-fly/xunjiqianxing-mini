/**
 * Promoter Service - 推广员相关API
 */

const { get, post } = require('./request');
const { baseUrl, paths } = require('../config/api');
const { getToken } = require('../utils/auth');

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
   * 上报扫码事件（无需登录）
   * @param {string} promoCode
   */
  recordScan(promoCode) {
    return post(paths.promoter.scan, { promoCode }, { showError: false });
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

  /**
   * 获取扫码记录
   * @param {Object} params
   * @param {number} params.page
   * @param {number} params.pageSize
   */
  getScanList(params) {
    return get(paths.promoter.scanList, params);
  },

  /**
   * 获取推广下单记录
   * @param {Object} params
   * @param {number} params.page
   * @param {number} params.pageSize
   */
  getOrderList(params) {
    return get(paths.promoter.orderList, params);
  },

  /**
   * 下载推广员专属小程序码（返回 base64 data URI，可直接用于 <image> src）
   * 同时将原始 ArrayBuffer 写入本地，供 handleSaveImage 保存相册使用。
   */
  downloadQrCode() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: baseUrl + paths.promoter.qrcode,
        method: 'GET',
        header: { Authorization: getToken(), 'ngrok-skip-browser-warning': 'true' },
        responseType: 'arraybuffer',
        success(res) {
          if (res.statusCode === 200) {
            // 转 base64 data URI，直接给 <image> 渲染，不依赖文件系统路径
            const base64 = wx.arrayBufferToBase64(res.data);
            const dataUri = `data:image/png;base64,${base64}`;
            // 后台异步写文件，供保存到相册时使用（失败不影响显示）
            const filePath = `${wx.env.USER_DATA_PATH}/promoter_qr.png`;
            wx.getFileSystemManager().writeFile({
              filePath,
              data: res.data,
              fail() {},
            });
            resolve({ dataUri, filePath });
          } else {
            reject(new Error('二维码获取失败: ' + res.statusCode));
          }
        },
        fail(err) { reject(err); },
      });
    });
  },
};

module.exports = promoterApi;
