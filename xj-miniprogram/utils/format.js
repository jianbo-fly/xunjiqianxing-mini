/**
 * Format - 格式化工具
 */

/**
 * 格式化日期
 * @param {Date|string|number} date - 日期
 * @param {string} format - 格式，默认 YYYY-MM-DD
 */
const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return '';

  const d = new Date(date);
  if (isNaN(d.getTime())) return '';

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
};

/**
 * 格式化时间为相对时间
 */
const formatRelativeTime = (date) => {
  if (!date) return '';

  const d = new Date(date);
  const now = new Date();
  const diff = now - d;

  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  const week = 7 * day;
  const month = 30 * day;

  if (diff < minute) return '刚刚';
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
  if (diff < day) return `${Math.floor(diff / hour)}小时前`;
  if (diff < week) return `${Math.floor(diff / day)}天前`;
  if (diff < month) return `${Math.floor(diff / week)}周前`;

  return formatDate(date, 'YYYY-MM-DD');
};

/**
 * 格式化价格
 * @param {number} price - 价格（分）
 * @param {boolean} showSymbol - 是否显示符号
 */
const formatPrice = (price, showSymbol = true) => {
  if (price === null || price === undefined) return '';

  // 分转元
  const yuan = (price / 100).toFixed(2);

  // 去掉末尾的0
  const formatted = parseFloat(yuan).toString();

  return showSymbol ? `¥${formatted}` : formatted;
};

/**
 * 格式化价格（元）
 */
const formatPriceYuan = (price, showSymbol = true) => {
  if (price === null || price === undefined) return '';

  const formatted = parseFloat(price).toFixed(2);

  return showSymbol ? `¥${formatted}` : formatted;
};

/**
 * 格式化手机号（隐藏中间4位）
 */
const formatPhone = (phone) => {
  if (!phone || phone.length !== 11) return phone || '';
  return `${phone.slice(0, 3)}****${phone.slice(7)}`;
};

/**
 * 格式化身份证（隐藏中间）
 */
const formatIdCard = (idCard) => {
  if (!idCard || idCard.length < 8) return idCard || '';
  return `${idCard.slice(0, 4)}****${idCard.slice(-4)}`;
};

/**
 * 格式化数字（千分位）
 */
const formatNumber = (num) => {
  if (num === null || num === undefined) return '';
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

/**
 * 格式化数字（简写：万、亿）
 */
const formatNumberShort = (num) => {
  if (num === null || num === undefined) return '';

  if (num >= 100000000) {
    return (num / 100000000).toFixed(1).replace(/\.0$/, '') + '亿';
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1).replace(/\.0$/, '') + '万';
  }
  return num.toString();
};

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';

  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i];
};

/**
 * 格式化距离
 */
const formatDistance = (meters) => {
  if (meters === null || meters === undefined) return '';

  if (meters < 1000) {
    return `${Math.round(meters)}m`;
  }
  return `${(meters / 1000).toFixed(1)}km`;
};

/**
 * 格式化倒计时
 */
const formatCountdown = (seconds) => {
  if (seconds <= 0) return '00:00:00';

  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;

  return [h, m, s].map(v => String(v).padStart(2, '0')).join(':');
};

/**
 * 格式化订单倒计时（分:秒）
 */
const formatOrderCountdown = (seconds) => {
  if (seconds <= 0) return '00:00';

  const m = Math.floor(seconds / 60);
  const s = seconds % 60;

  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

module.exports = {
  formatDate,
  formatRelativeTime,
  formatPrice,
  formatPriceYuan,
  formatPhone,
  formatIdCard,
  formatNumber,
  formatNumberShort,
  formatFileSize,
  formatDistance,
  formatCountdown,
  formatOrderCountdown,
};
