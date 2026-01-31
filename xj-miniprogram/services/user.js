/**
 * User Service - 用户认证相关API
 */

const { get, post, put } = require('./request');
const { paths } = require('../config/api');

const userApi = {
  /**
   * 微信登录
   * @param {Object} params
   * @param {string} params.code - wx.login获取的code
   * @param {string} params.promoterCode - 推广员邀请码（可选）
   */
  wxLogin(params) {
    return post(paths.user.wxLogin, params);
  },

  /**
   * 获取当前用户信息
   */
  getInfo() {
    return get(paths.user.info);
  },

  /**
   * 更新用户信息
   * @param {Object} params
   * @param {string} params.nickname - 昵称
   * @param {string} params.avatar - 头像
   * @param {number} params.gender - 性别
   */
  updateInfo(params) {
    return put(paths.user.update, params);
  },

  /**
   * 退出登录
   */
  logout() {
    return post(paths.user.logout);
  },

  /**
   * 微信一键绑定手机号
   * @param {Object} params
   * @param {string} params.code - getPhoneNumber返回的code
   */
  bindPhoneByWx(params) {
    return post(paths.user.bindPhoneByWx, params);
  },

  /**
   * 验证码绑定手机号
   * @param {Object} params
   * @param {string} params.phone - 手机号
   * @param {string} params.code - 验证码
   */
  bindPhone(params) {
    return post(paths.user.bindPhone, params);
  },

  /**
   * 发送验证码
   * @param {Object} params
   * @param {string} params.phone - 手机号
   */
  sendVerifyCode(params) {
    return post(paths.user.sendCode, params);
  },

  /**
   * 获取会员信息
   */
  getMemberInfo() {
    return get(paths.member.info);
  },

  /**
   * 购买会员
   * @param {Object} params
   */
  buyMember(params) {
    return post(paths.member.buy, params);
  },

  /**
   * 获取会员权益
   */
  getMemberBenefits() {
    return get(paths.member.benefits);
  },
};

module.exports = userApi;
