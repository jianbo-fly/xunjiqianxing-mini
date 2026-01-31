/**
 * Services - API服务统一导出
 */

const userApi = require('./user');
const homeApi = require('./home');
const routeApi = require('./route');
const orderApi = require('./order');
const payApi = require('./pay');
const favoriteApi = require('./favorite');
const travelerApi = require('./traveler');
const couponApi = require('./coupon');
const companionApi = require('./companion');
const promoterApi = require('./promoter');
const messageApi = require('./message');
const commonApi = require('./common');

module.exports = {
  userApi,
  homeApi,
  routeApi,
  orderApi,
  payApi,
  favoriteApi,
  travelerApi,
  couponApi,
  companionApi,
  promoterApi,
  messageApi,
  commonApi,
};
