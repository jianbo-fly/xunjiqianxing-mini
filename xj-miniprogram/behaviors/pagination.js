/**
 * pagination - 分页行为
 *
 * 用于列表页面的分页加载
 */

module.exports = Behavior({
  data: {
    // 列表数据
    list: [],
    // 分页参数
    page: 1,
    pageSize: 10,
    total: 0,
    // 加载状态
    loading: false,
    loadingMore: false,
    noMore: false,
    // 是否初始化加载
    initialized: false,
  },

  methods: {
    /**
     * 初始化加载
     */
    async initLoad() {
      this.setData({
        page: 1,
        list: [],
        noMore: false,
        initialized: false,
      });
      await this.loadData();
      this.setData({ initialized: true });
    },

    /**
     * 加载更多
     */
    async loadMore() {
      if (this.data.loading || this.data.loadingMore || this.data.noMore) {
        return;
      }
      this.setData({ page: this.data.page + 1 });
      await this.loadData(true);
    },

    /**
     * 下拉刷新
     */
    async onPullDownRefresh() {
      await this.initLoad();
      wx.stopPullDownRefresh();
    },

    /**
     * 上拉加载更多
     */
    onReachBottom() {
      this.loadMore();
    },

    /**
     * 加载数据
     * @param {boolean} isLoadMore - 是否加载更多
     */
    async loadData(isLoadMore = false) {
      if (isLoadMore) {
        this.setData({ loadingMore: true });
      } else {
        this.setData({ loading: true });
      }

      try {
        // 子类需要实现 fetchData 方法
        const result = await this.fetchData({
          page: this.data.page,
          pageSize: this.data.pageSize,
        });

        const { list = [], total = 0 } = result || {};

        this.setData({
          list: isLoadMore ? [...this.data.list, ...list] : list,
          total,
          noMore: this.data.list.length + list.length >= total,
        });
      } catch (e) {
        console.error('加载数据失败', e);
        // 加载失败回退页码
        if (isLoadMore) {
          this.setData({ page: this.data.page - 1 });
        }
      } finally {
        this.setData({
          loading: false,
          loadingMore: false,
        });
      }
    },

    /**
     * 获取数据（子类需要实现）
     */
    async fetchData(params) {
      console.warn('请在页面中实现 fetchData 方法');
      return { list: [], total: 0 };
    },
  },
});
