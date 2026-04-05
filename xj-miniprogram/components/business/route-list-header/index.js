/**
 * 线路列表顶部导航组件
 * 包含：主 Tab（跟团游/定制游）+ 分类标签行 + 筛选栏
 */
Component({
  properties: {
    /** 主 Tab 列表，格式：[{ key: 'group', label: '跟团游' }, ...] */
    tabs: {
      type: Array,
      value: [],
    },
    /** 当前激活的主 Tab key */
    activeTab: {
      type: String,
      value: '',
    },
    /** 分类标签列表，格式：[{ id: '', name: '全部' }, ...] */
    categories: {
      type: Array,
      value: [],
    },
    /** 当前激活的分类 id */
    activeCategory: {
      type: String,
      value: '',
    },
    /** 筛选条件当前值，格式：{ departure, days, budget, hasFilter } */
    filters: {
      type: Object,
      value: {},
    },
    /** 固定导航栏高度（px），用于 sticky 定位时避免被 nav-bar 遮挡 */
    navTop: {
      type: Number,
      value: 0,
    },
  },

  methods: {
    _handleTabTap(e) {
      const { key } = e.currentTarget.dataset;
      if (key === this.properties.activeTab) return;
      this.triggerEvent('tabchange', { key });
    },

    _handleCategoryTap(e) {
      const { id } = e.currentTarget.dataset;
      if (id === this.properties.activeCategory) return;
      this.triggerEvent('categorychange', { id });
    },

    _handleFilterTap(e) {
      const { type } = e.currentTarget.dataset;
      this.triggerEvent('filtertap', { type });
    },

    _handleMoreFilter() {
      this.triggerEvent('morefilter');
    },

    _handleSearchTap() {
      this.triggerEvent('searchtap');
    },
  },
});
