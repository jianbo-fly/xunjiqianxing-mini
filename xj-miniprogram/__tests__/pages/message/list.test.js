/**
 * 消息列表页测试（#389 ~ #396）
 */

const { loadPage, createPageInstance } = require('../../helpers');

jest.mock('../../../services/message', () => ({
  getList: jest.fn(),
  markRead: jest.fn(),
}));

const messageApi = require('../../../services/message');

describe('消息列表页', () => {
  let pageConfig;
  let page;

  beforeAll(() => {
    pageConfig = loadPage('pages/message/list/index');
  });

  beforeEach(() => {
    jest.clearAllMocks();
    page = createPageInstance(pageConfig);
  });

  const makeMsg = (overrides = {}) => ({
    id: 1,
    type: 'order_confirm',
    title: '订单确认',
    content: '您的订单已确认',
    read: false,
    ...overrides,
  });

  // #389 按分类筛选
  describe('#389 handleTabChange', () => {
    test('切换 tab → 重置列表并传入 category', async () => {
      messageApi.getList.mockResolvedValue({ list: [] });

      page.handleTabChange({ currentTarget: { dataset: { tab: 1 } } });
      await new Promise(setImmediate);

      expect(page.data.activeTab).toBe(1);
      expect(page.data.list).toEqual([]);
      expect(messageApi.getList).toHaveBeenCalledWith(
        expect.objectContaining({ category: 'order' })
      );
    });

    test('全部 tab（activeTab=0）不传 category', async () => {
      messageApi.getList.mockResolvedValue({ list: [] });
      page.setData({ activeTab: 1 });

      page.handleTabChange({ currentTarget: { dataset: { tab: 0 } } });
      await new Promise(setImmediate);

      const arg = messageApi.getList.mock.calls[0][0];
      expect(arg.category).toBeUndefined();
    });

    test('点击当前 tab → 不重新加载', () => {
      page.setData({ activeTab: 0 });
      page.handleTabChange({ currentTarget: { dataset: { tab: 0 } } });
      expect(messageApi.getList).not.toHaveBeenCalled();
    });
  });

  // #390 乐观标记已读
  test('#390 handleMsgTap 乐观标记已读', async () => {
    messageApi.markRead.mockResolvedValue({});
    page.setData({ list: [makeMsg({ id: 1, read: false })] });

    await page.handleMsgTap({ currentTarget: { dataset: { id: 1 } } });

    expect(page.data['list[0].read']).toBe(true);
    expect(messageApi.markRead).toHaveBeenCalledWith(1);
  });

  test('#390-2 已读消息不再调用 API', async () => {
    page.setData({ list: [makeMsg({ id: 1, read: true })] });

    await page.handleMsgTap({ currentTarget: { dataset: { id: 1 } } });

    expect(messageApi.markRead).not.toHaveBeenCalled();
  });

  // #391 跳转 link
  test('#391 handleMsgTap 带 link → navigateTo', async () => {
    page.setData({ list: [] });
    await page.handleMsgTap({ currentTarget: { dataset: { id: 999, link: '/pages/x/x' } } });
    expect(wx.navigateTo).toHaveBeenCalledWith(
      expect.objectContaining({ url: '/pages/x/x' })
    );
  });

  // #392 全部标记已读
  describe('#392 handleMarkAllRead', () => {
    test('成功 → 所有消息标记已读', async () => {
      messageApi.markRead.mockResolvedValue({});
      page.setData({
        list: [makeMsg({ id: 1 }), makeMsg({ id: 2 })],
      });

      await page.handleMarkAllRead();

      expect(messageApi.markRead).toHaveBeenCalledWith('all');
      expect(page.data.list.every(m => m.read)).toBe(true);
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '已全部已读' })
      );
    });

    test('失败 → toast 操作失败', async () => {
      messageApi.markRead.mockRejectedValue(new Error('fail'));
      await page.handleMarkAllRead();
      expect(wx.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: '操作失败' })
      );
    });
  });

  // #393 分页加载
  describe('#393 loadList 分页', () => {
    test('首次加载（refresh=true）', async () => {
      messageApi.getList.mockResolvedValue({
        list: [makeMsg({ id: 1 }), makeMsg({ id: 2 })],
      });

      await page.loadList(true);

      expect(page.data.list).toHaveLength(2);
      expect(page.data.page).toBe(2);
    });

    test('追加加载', async () => {
      page.setData({ list: [makeMsg({ id: 1 })], page: 2, finished: false });
      messageApi.getList.mockResolvedValue({
        list: [makeMsg({ id: 2 })],
      });

      await page.loadList(false);

      expect(page.data.list).toHaveLength(2);
    });

    test('少于 pageSize → finished=true', async () => {
      messageApi.getList.mockResolvedValue({ list: [makeMsg()] });

      await page.loadList(true);

      expect(page.data.finished).toBe(true);
    });

    test('loading 中不重复加载', async () => {
      page.setData({ loading: true });
      await page.loadList(true);
      expect(messageApi.getList).not.toHaveBeenCalled();
    });
  });

  // #394 无消息空态
  test('#394 无消息 → list=[]', async () => {
    messageApi.getList.mockResolvedValue({ list: [] });
    await page.loadList(true);
    expect(page.data.list).toEqual([]);
  });

  // #395 图标映射
  test('#395 type → iconType 映射', async () => {
    messageApi.getList.mockResolvedValue({
      list: [
        makeMsg({ id: 1, type: 'order_confirm' }),
        makeMsg({ id: 2, type: 'refund_success' }),
        makeMsg({ id: 3, type: 'travel_reminder' }),
        makeMsg({ id: 4, type: 'unknown', category: 'order' }),
        makeMsg({ id: 5, type: 'unknown' }),
      ],
    });

    await page.loadList(true);

    expect(page.data.list[0].iconType).toBe('order');
    expect(page.data.list[1].iconType).toBe('refund');
    expect(page.data.list[2].iconType).toBe('travel');
    expect(page.data.list[3].iconType).toBe('order');
    expect(page.data.list[4].iconType).toBe('system');
    expect(page.data.list[0].iconPath).toContain('msg-order');
  });

  // #396 API 失败
  test('#396 API 失败 → 不崩溃', async () => {
    messageApi.getList.mockRejectedValue(new Error('fail'));

    await page.loadList(true);

    expect(page.data.loading).toBe(false);
  });
});
