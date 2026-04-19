/**
 * 搜索页测试（#351 ~ #360）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../services/route', () => ({
  getList: jest.fn(),
}), { virtual: true });
jest.mock('../../../services/route', () => ({
  getList: jest.fn(),
}));
jest.mock('../../../utils/router', () => ({
  go: { routeDetail: jest.fn() },
  routes: {},
}));

const routeApi = require('../../../services/route');
const { go } = require('../../../utils/router');

describe('搜索页', () => {
  let pageConfig;
  let page;
  const storage = {};

  beforeAll(() => {
    pageConfig = loadPage('pages/search/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    Object.keys(storage).forEach(k => delete storage[k]);
    wx.getStorageSync.mockImplementation(k => storage[k]);
    wx.setStorageSync.mockImplementation((k, v) => { storage[k] = v; });
    wx.removeStorageSync.mockImplementation(k => { delete storage[k]; });
    page = createPageInstance(pageConfig);
  });

  // #351 搜索跳转
  test('#351 _doSearch → redirectTo 列表页', () => {
    page.setData({ departureCity: '上海' });
    page._doSearch('丽江');
    expect(wx.redirectTo).toHaveBeenCalledWith(
      expect.objectContaining({
        url: expect.stringContaining('keyword=' + encodeURIComponent('丽江')),
      })
    );
  });

  // #352 空关键词
  test('#352 空关键词 → 不跳转', () => {
    page.setData({ keyword: '' });
    page.handleSearch();
    expect(wx.redirectTo).not.toHaveBeenCalled();
  });

  // #353 搜索历史保存
  test('#353 _saveHistory 写入 storage', () => {
    page._saveHistory('丽江');
    expect(wx.setStorageSync).toHaveBeenCalled();
    expect(page.data.historyList).toContain('丽江');
  });

  // #354 历史去重
  test('#354 相同关键词不重复', () => {
    page.setData({ historyList: ['丽江', '大理'] });
    page._saveHistory('丽江');
    expect(page.data.historyList).toEqual(['丽江', '大理']);
  });

  // #355 删除单条历史
  test('#355 handleDeleteHistory 删除指定项', () => {
    page.setData({ historyList: ['丽江', '大理'] });
    page.handleDeleteHistory({ currentTarget: { dataset: { keyword: '丽江' } } });
    expect(page.data.historyList).toEqual(['大理']);
  });

  // #356 清空全部历史
  describe('#356 handleClearHistory', () => {
    test('确认后清空', () => {
      wx.showModal.mockImplementation(({ success }) => success({ confirm: true }));
      page.setData({ historyList: ['a', 'b'] });

      page.handleClearHistory();

      expect(wx.removeStorageSync).toHaveBeenCalledWith('search_history');
      expect(page.data.historyList).toEqual([]);
    });

    test('取消 → 不清空', () => {
      wx.showModal.mockImplementation(({ success }) => success({ confirm: false }));
      page.setData({ historyList: ['a'] });

      page.handleClearHistory();

      expect(page.data.historyList).toEqual(['a']);
    });
  });

  // #357 历史 JSON 解析失败
  test('#357 _loadHistory 解析失败 → 空数组', () => {
    storage['search_history'] = 'not-json';
    page._loadHistory();
    expect(page.data.historyList).toEqual([]);
  });

  // #358 排序切换
  test('#358 handleSort 切换排序并重新搜索', () => {
    routeApi.getList.mockResolvedValue({ list: [], total: 0 });
    page.setData({ keyword: '丽江', sortType: 'default' });

    page.handleSort({ currentTarget: { dataset: { type: 'price' } } });

    expect(page.data.sortType).toBe('price');
  });

  test('#358-2 点击当前排序 → 无操作', () => {
    page.setData({ sortType: 'default' });
    page.handleSort({ currentTarget: { dataset: { type: 'default' } } });
    expect(wx.redirectTo).not.toHaveBeenCalled();
  });

  // #359 热门搜索点击
  test('#359 handleTagTap 自动搜索', () => {
    page.setData({ departureCity: '上海' });
    page.handleTagTap({ currentTarget: { dataset: { keyword: '西藏' } } });
    expect(page.data.keyword).toBe('西藏');
    expect(wx.redirectTo).toHaveBeenCalled();
  });

  // #360 特殊字符
  test('#360 特殊字符不崩溃', () => {
    page.setData({ departureCity: '上海', keyword: '<script>' });
    expect(() => page.handleSearch()).not.toThrow();
  });

  // 其他辅助
  describe('辅助方法', () => {
    test('handleInput 清空时重置 resultList', () => {
      page.setData({ showResult: true, resultList: [1, 2] });
      page.handleInput({ detail: { value: '' } });
      expect(page.data.showResult).toBe(false);
      expect(page.data.resultList).toEqual([]);
    });

    test('handleClear 清空关键词和结果', () => {
      page.setData({ keyword: 'x', resultList: [1] });
      page.handleClear();
      expect(page.data.keyword).toBe('');
      expect(page.data.resultList).toEqual([]);
    });

    test('handleCancel 返回上一页', () => {
      page.handleCancel();
      expect(wx.navigateBack).toHaveBeenCalled();
    });

    test('handleRouteTap 跳转详情', () => {
      page.handleRouteTap({ currentTarget: { dataset: { id: 10 } } });
      expect(go.routeDetail).toHaveBeenCalledWith(10);
    });

    test('onLoad 使用 options.keyword 初始化', () => {
      page.onLoad({ keyword: encodeURIComponent('西藏'), departureCity: encodeURIComponent('北京') });
      expect(page.data.keyword).toBe('西藏');
      expect(page.data.departureCity).toBe('北京');
    });
  });
});
