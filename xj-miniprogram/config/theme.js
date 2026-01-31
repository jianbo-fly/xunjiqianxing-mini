/**
 * Theme Manager - 主题管理器
 *
 * 支持主题切换功能
 */

const designTokens = require('./design-tokens');

// 预定义主题
const themes = {
  // 默认主题（蓝色系）
  default: {
    primary: '#1890FF',
    primaryLight: '#E6F7FF',
    primaryDark: '#096DD9',
  },

  // 自然主题（绿色系 - 户外旅行风格）
  nature: {
    primary: '#52C41A',
    primaryLight: '#F6FFED',
    primaryDark: '#389E0D',
  },

  // 暖色主题（橙色系 - 活力风格）
  warm: {
    primary: '#FA8C16',
    primaryLight: '#FFF7E6',
    primaryDark: '#D46B08',
  },

  // 深色主题
  dark: {
    primary: '#177DDC',
    primaryLight: '#111B26',
    primaryDark: '#3C9AE8',
    textPrimary: '#FFFFFF',
    textSecondary: '#CCCCCC',
    textTertiary: '#999999',
    bgPage: '#1A1A1A',
    bgCard: '#2D2D2D',
    border: '#404040',
  },
};

// 当前主题
let currentTheme = 'default';

/**
 * 主题管理器
 */
const ThemeManager = {
  /**
   * 获取当前主题名称
   */
  getCurrentTheme() {
    return currentTheme;
  },

  /**
   * 获取所有主题
   */
  getAllThemes() {
    return Object.keys(themes);
  },

  /**
   * 切换主题
   * @param {string} themeName - 主题名称
   */
  setTheme(themeName) {
    if (!themes[themeName]) {
      console.warn(`Theme "${themeName}" not found, using default.`);
      themeName = 'default';
    }

    currentTheme = themeName;
    this.applyTheme(themeName);

    // 持久化存储
    wx.setStorageSync('theme', themeName);
  },

  /**
   * 应用主题到页面
   */
  applyTheme(themeName) {
    const theme = themes[themeName];
    const pages = getCurrentPages();

    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      if (currentPage && currentPage.setData) {
        currentPage.setData({
          themeVars: this.generateCSSVars(theme)
        });
      }
    }
  },

  /**
   * 生成CSS变量字符串
   */
  generateCSSVars(theme) {
    let cssVars = '';
    for (const [key, value] of Object.entries(theme)) {
      const cssKey = this.camelToKebab(key);
      cssVars += `--color-${cssKey}: ${value}; `;
    }
    return cssVars;
  },

  /**
   * 驼峰转连字符
   */
  camelToKebab(str) {
    return str.replace(/([A-Z])/g, '-$1').toLowerCase();
  },

  /**
   * 初始化主题（在app.js中调用）
   */
  init() {
    const savedTheme = wx.getStorageSync('theme') || 'default';
    currentTheme = savedTheme;
    return savedTheme;
  },

  /**
   * 获取主题配置
   */
  getThemeConfig(themeName) {
    return themes[themeName] || themes.default;
  },
};

module.exports = ThemeManager;
