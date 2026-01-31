/**
 * Design Tokens - 设计令牌
 *
 * 所有视觉属性的配置中心
 * 后期UI美化时，主要修改此文件
 */

module.exports = {
  // ========================
  // 颜色系统 (Colors)
  // ========================
  colors: {
    // 品牌色
    primary: '#1890FF',           // 主色调
    primaryLight: '#E6F7FF',      // 主色浅
    primaryDark: '#096DD9',       // 主色深

    // 功能色
    success: '#52C41A',           // 成功
    successLight: '#F6FFED',
    warning: '#FAAD14',           // 警告
    warningLight: '#FFFBE6',
    error: '#FF4D4F',             // 错误
    errorLight: '#FFF2F0',
    info: '#1890FF',              // 信息

    // 中性色 - 文字
    textPrimary: '#333333',       // 主要文字
    textSecondary: '#666666',     // 次要文字
    textTertiary: '#999999',      // 辅助文字
    textPlaceholder: '#CCCCCC',   // 占位符
    textInverse: '#FFFFFF',       // 反色文字

    // 中性色 - 背景
    bgPage: '#F5F5F5',            // 页面背景
    bgCard: '#FFFFFF',            // 卡片背景
    bgGray: '#FAFAFA',            // 灰色背景
    bgMask: 'rgba(0,0,0,0.5)',    // 遮罩层

    // 中性色 - 边框分割线
    border: '#E8E8E8',            // 边框
    borderLight: '#F0F0F0',       // 浅边框
    divider: '#EEEEEE',           // 分割线
  },

  // ========================
  // 字体系统 (Typography)
  // ========================
  fonts: {
    // 字号
    sizeXs: '20rpx',              // 极小 - 辅助文字
    sizeSm: '24rpx',              // 小 - 次要文字
    sizeMd: '28rpx',              // 中 - 正文（默认）
    sizeLg: '32rpx',              // 大 - 小标题
    sizeXl: '36rpx',              // 特大 - 标题
    sizeXxl: '44rpx',             // 超大 - 大标题
    sizePrice: '40rpx',           // 价格专用

    // 字重
    weightNormal: '400',          // 常规
    weightMedium: '500',          // 中等
    weightSemibold: '600',        // 半粗
    weightBold: '700',            // 粗体

    // 行高
    lineHeightTight: '1.2',       // 紧凑
    lineHeightNormal: '1.5',      // 常规
    lineHeightLoose: '1.8',       // 宽松
  },

  // ========================
  // 间距系统 (Spacing)
  // ========================
  spacing: {
    xs: '8rpx',                   // 4px - 极小
    sm: '16rpx',                  // 8px - 小
    md: '24rpx',                  // 12px - 中
    lg: '32rpx',                  // 16px - 大
    xl: '48rpx',                  // 24px - 特大
    xxl: '64rpx',                 // 32px - 超大

    // 页面边距
    pagePadding: '32rpx',         // 页面左右边距
    cardPadding: '24rpx',         // 卡片内边距
    sectionGap: '24rpx',          // 区块间距
  },

  // ========================
  // 圆角系统 (Radius)
  // ========================
  radius: {
    none: '0',
    xs: '4rpx',                   // 极小圆角
    sm: '8rpx',                   // 小圆角
    md: '16rpx',                  // 中圆角（默认）
    lg: '24rpx',                  // 大圆角
    xl: '32rpx',                  // 特大圆角
    full: '9999rpx',              // 全圆（胶囊）
  },

  // ========================
  // 阴影系统 (Shadows)
  // ========================
  shadows: {
    none: 'none',
    sm: '0 2rpx 8rpx rgba(0, 0, 0, 0.08)',      // 轻微阴影
    md: '0 4rpx 16rpx rgba(0, 0, 0, 0.12)',     // 中等阴影
    lg: '0 8rpx 32rpx rgba(0, 0, 0, 0.16)',     // 较深阴影
    xl: '0 16rpx 48rpx rgba(0, 0, 0, 0.20)',    // 深阴影
  },

  // ========================
  // 动画系统 (Animation)
  // ========================
  animation: {
    durationFast: '150ms',
    durationNormal: '300ms',
    durationSlow: '500ms',

    easingDefault: 'ease',
    easingIn: 'ease-in',
    easingOut: 'ease-out',
    easingInOut: 'ease-in-out',
  },

  // ========================
  // 层级系统 (Z-Index)
  // ========================
  zIndex: {
    default: 1,
    dropdown: 100,
    sticky: 200,
    fixed: 300,
    modalBackdrop: 400,
    modal: 500,
    popover: 600,
    tooltip: 700,
    toast: 800,
  },

  // ========================
  // 组件尺寸 (Component Size)
  // ========================
  components: {
    // 按钮
    button: {
      heightSm: '56rpx',
      heightMd: '72rpx',
      heightLg: '88rpx',
    },

    // 输入框
    input: {
      height: '88rpx',
    },

    // 头像
    avatar: {
      sizeSm: '48rpx',
      sizeMd: '64rpx',
      sizeLg: '96rpx',
      sizeXl: '128rpx',
    },

    // 图标
    icon: {
      sizeSm: '32rpx',
      sizeMd: '40rpx',
      sizeLg: '48rpx',
    },

    // 导航栏
    navBar: {
      height: '88rpx',
    },

    // 底部栏
    tabBar: {
      height: '100rpx',
    },
  },
};
