package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.route.*;
import com.xunjiqianxing.admin.mapper.SystemSupplierMapper;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.product.entity.ProductMain;
import com.xunjiqianxing.service.product.entity.ProductPriceStock;
import com.xunjiqianxing.service.product.entity.ProductRoute;
import com.xunjiqianxing.service.product.entity.ProductSku;
import com.xunjiqianxing.service.product.mapper.ProductMainMapper;
import com.xunjiqianxing.service.product.mapper.ProductPriceStockMapper;
import com.xunjiqianxing.service.product.mapper.ProductRouteMapper;
import com.xunjiqianxing.service.product.mapper.ProductSkuMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminRouteService 单元测试（#289 ~ #291）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminRouteServiceTest {

    @InjectMocks
    private AdminRouteService adminRouteService;

    @Mock
    private ProductMainMapper productMainMapper;
    @Mock
    private ProductRouteMapper productRouteMapper;
    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private ProductPriceStockMapper productPriceStockMapper;
    @Mock
    private SystemSupplierMapper systemSupplierMapper;

    private MockedStatic<StpUtil> stpMock;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration cfg = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(cfg, "");
        TableInfoHelper.initTableInfo(assistant, ProductMain.class);
        TableInfoHelper.initTableInfo(assistant, ProductPriceStock.class);
        TableInfoHelper.initTableInfo(assistant, ProductSku.class);
    }

    @BeforeEach
    void setUp() {
        stpMock = Mockito.mockStatic(StpUtil.class);
        SaSession session = new SaSession("test-session");
        session.set("roleType", "admin");
        stpMock.when(StpUtil::getSession).thenReturn(session);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    private ProductMain buildProduct(Long id, int status, int auditStatus) {
        ProductMain p = new ProductMain();
        p.setId(id);
        p.setBizType("route");
        p.setName("丽江三日游");
        p.setCoverImage("cover.jpg");
        p.setStatus(status);
        p.setAuditStatus(auditStatus);
        p.setIsDeleted(0);
        p.setSupplierId(1L);
        return p;
    }

    // #289 路线列表（分页+筛选）
    @Test
    @DisplayName("#289 路线列表分页查询")
    void testPageList() {
        Page<ProductMain> page = new Page<>(1, 10);
        page.setRecords(List.of(buildProduct(1L, 1, 1)));
        page.setTotal(1);

        when(productMainMapper.selectPage(any(), any())).thenReturn(page);
        when(productRouteMapper.selectById(anyLong())).thenReturn(null);
        when(productSkuMapper.selectCount(any())).thenReturn(2L);
        when(systemSupplierMapper.selectById(anyLong())).thenReturn(null);

        RouteQueryRequest req = new RouteQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        PageResult<RouteListVO> result = adminRouteService.pageList(req);

        assertEquals(1, result.getList().size());
        assertEquals("丽江三日游", result.getList().get(0).getName());
    }

    @Test
    @DisplayName("#289-2 按关键词筛选")
    void testPageListByKeyword() {
        Page<ProductMain> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(productMainMapper.selectPage(any(), any())).thenReturn(page);

        RouteQueryRequest req = new RouteQueryRequest();
        req.setPage(1);
        req.setPageSize(10);
        req.setKeyword("丽江");

        PageResult<RouteListVO> result = adminRouteService.pageList(req);

        assertEquals(0, result.getList().size());
    }

    // #290 路线上架/下架
    @Test
    @DisplayName("#290 路线上架（审核通过后）")
    void testUpdateStatusOnline() {
        ProductMain product = buildProduct(1L, 0, 1);
        when(productMainMapper.selectById(1L)).thenReturn(product);
        when(productMainMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> adminRouteService.updateStatus(1L, 1));
        verify(productMainMapper).updateById(argThat(p -> ((ProductMain) p).getStatus() == 1));
    }

    @Test
    @DisplayName("#290-2 路线下架")
    void testUpdateStatusOffline() {
        ProductMain product = buildProduct(1L, 1, 1);
        when(productMainMapper.selectById(1L)).thenReturn(product);
        when(productMainMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> adminRouteService.updateStatus(1L, 0));
    }

    @Test
    @DisplayName("#290-3 未审核通过 → 无法上架")
    void testUpdateStatusNotAudited() {
        ProductMain product = buildProduct(1L, 0, 0);
        when(productMainMapper.selectById(1L)).thenReturn(product);

        BizException ex = assertThrows(BizException.class,
                () -> adminRouteService.updateStatus(1L, 1));
        assertTrue(ex.getMessage().contains("未审核通过"));
    }

    @Test
    @DisplayName("#290-4 路线不存在")
    void testUpdateStatusNotFound() {
        when(productMainMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> adminRouteService.updateStatus(999L, 1));
    }

    // #291 路线库存管理
    @Test
    @DisplayName("#291 设置价格日历 → 新增价格库存记录")
    void testSetPriceCalendarInsert() {
        ProductSku sku = new ProductSku();
        sku.setId(10L);
        sku.setProductId(1L);
        sku.setBasePrice(BigDecimal.valueOf(299));
        when(productSkuMapper.selectById(10L)).thenReturn(sku);

        ProductMain product = buildProduct(1L, 1, 1);
        when(productMainMapper.selectById(1L)).thenReturn(product);
        when(productPriceStockMapper.selectOne(any())).thenReturn(null);
        when(productPriceStockMapper.insert(any())).thenReturn(1);
        when(productSkuMapper.selectList(any())).thenReturn(List.of(sku));
        when(productMainMapper.updateById(any())).thenReturn(1);

        PriceCalendarRequest req = new PriceCalendarRequest();
        req.setSkuId(10L);
        PriceCalendarRequest.PriceItem item = new PriceCalendarRequest.PriceItem();
        item.setDate(LocalDate.of(2026, 5, 1));
        item.setPrice(BigDecimal.valueOf(399));
        item.setStock(20);
        item.setStatus(1);
        req.setPrices(List.of(item));

        assertDoesNotThrow(() -> adminRouteService.setPriceCalendar(req));
        verify(productPriceStockMapper).insert(any());
    }

    @Test
    @DisplayName("#291-2 设置价格日历 → 更新已有记录")
    void testSetPriceCalendarUpdate() {
        ProductSku sku = new ProductSku();
        sku.setId(10L);
        sku.setProductId(1L);
        sku.setBasePrice(BigDecimal.valueOf(299));
        when(productSkuMapper.selectById(10L)).thenReturn(sku);

        ProductMain product = buildProduct(1L, 1, 1);
        when(productMainMapper.selectById(1L)).thenReturn(product);

        ProductPriceStock existing = new ProductPriceStock();
        existing.setId(100L);
        existing.setSkuId(10L);
        existing.setDate(LocalDate.of(2026, 5, 1));
        existing.setPrice(BigDecimal.valueOf(299));
        existing.setStock(10);
        when(productPriceStockMapper.selectOne(any())).thenReturn(existing);
        when(productPriceStockMapper.updateById(any())).thenReturn(1);
        when(productSkuMapper.selectList(any())).thenReturn(List.of(sku));
        when(productMainMapper.updateById(any())).thenReturn(1);

        PriceCalendarRequest req = new PriceCalendarRequest();
        req.setSkuId(10L);
        PriceCalendarRequest.PriceItem item = new PriceCalendarRequest.PriceItem();
        item.setDate(LocalDate.of(2026, 5, 1));
        item.setPrice(BigDecimal.valueOf(399));
        item.setStock(20);
        item.setStatus(1);
        req.setPrices(List.of(item));

        assertDoesNotThrow(() -> adminRouteService.setPriceCalendar(req));
        verify(productPriceStockMapper).updateById(any());
    }

    @Test
    @DisplayName("#291-3 获取价格日历")
    void testGetPriceCalendar() {
        ProductSku sku = new ProductSku();
        sku.setId(10L);
        sku.setProductId(1L);
        when(productSkuMapper.selectById(10L)).thenReturn(sku);

        ProductMain product = buildProduct(1L, 1, 1);
        when(productMainMapper.selectById(1L)).thenReturn(product);

        ProductPriceStock ps = new ProductPriceStock();
        ps.setDate(LocalDate.of(2026, 5, 1));
        ps.setPrice(BigDecimal.valueOf(399));
        ps.setStock(20);
        when(productPriceStockMapper.selectList(any())).thenReturn(List.of(ps));

        List<ProductPriceStock> result = adminRouteService.getPriceCalendar(
                10L, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(399), result.get(0).getPrice());
    }
}
