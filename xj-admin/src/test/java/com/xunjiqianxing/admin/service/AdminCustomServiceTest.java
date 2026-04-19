package com.xunjiqianxing.admin.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.admin.dto.custom.*;
import com.xunjiqianxing.common.exception.BizException;
import com.xunjiqianxing.common.result.PageResult;
import com.xunjiqianxing.service.custom.entity.CustomDemand;
import com.xunjiqianxing.service.custom.entity.CustomFollowRecord;
import com.xunjiqianxing.service.custom.mapper.CustomDemandMapper;
import com.xunjiqianxing.service.custom.mapper.CustomFollowRecordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AdminCustomService 单元测试（#286 ~ #288）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminCustomServiceTest {

    @InjectMocks
    private AdminCustomService adminCustomService;

    @Mock
    private CustomDemandMapper customDemandMapper;
    @Mock
    private CustomFollowRecordMapper customFollowRecordMapper;

    private MockedStatic<StpUtil> stpMock;

    @org.junit.jupiter.api.BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration cfg = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(cfg, "");
        TableInfoHelper.initTableInfo(assistant, CustomDemand.class);
    }

    @BeforeEach
    void setUp() {
        stpMock = Mockito.mockStatic(StpUtil.class);
        stpMock.when(StpUtil::getLoginId).thenReturn(100L);
        SaSession session = new SaSession("test-session");
        session.set("username", "admin");
        stpMock.when(StpUtil::getSession).thenReturn(session);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    private CustomDemand buildDemand(Long id, int status) {
        CustomDemand d = new CustomDemand();
        d.setId(id);
        d.setUserId(1L);
        d.setPhone("13800138000");
        d.setDestination("云南");
        d.setTravelDateStart(LocalDate.of(2026, 5, 1));
        d.setTravelDays("3-5天");
        d.setAdultCount(2);
        d.setChildCount(0);
        d.setBudget("3-5千");
        d.setStatus(status);
        d.setCreatedAt(LocalDateTime.of(2026, 4, 10, 10, 0));
        return d;
    }

    // #286 定制需求列表（按状态筛选）
    @Test
    @DisplayName("#286 定制需求列表（按状态筛选）")
    void testPageListByStatus() {
        Page<CustomDemand> page = new Page<>(1, 10);
        page.setRecords(List.of(buildDemand(1L, 0)));
        page.setTotal(1);

        when(customDemandMapper.selectPage(any(), any())).thenReturn(page);

        CustomQueryRequest req = new CustomQueryRequest();
        req.setPage(1);
        req.setPageSize(10);
        req.setStatus(0);

        PageResult<CustomListVO> result = adminCustomService.pageList(req);

        assertEquals(1, result.getList().size());
        assertEquals("待处理", result.getList().get(0).getStatusDesc());
    }

    @Test
    @DisplayName("#286-2 空列表")
    void testPageListEmpty() {
        Page<CustomDemand> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(customDemandMapper.selectPage(any(), any())).thenReturn(page);

        CustomQueryRequest req = new CustomQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        PageResult<CustomListVO> result = adminCustomService.pageList(req);

        assertEquals(0, result.getList().size());
    }

    // #287 定制需求跟进（状态变更+备注）
    @Test
    @DisplayName("#287 定制需求跟进 - 开始跟进")
    void testUpdateStatusToFollowing() {
        CustomDemand demand = buildDemand(1L, 0);
        when(customDemandMapper.selectById(1L)).thenReturn(demand);
        when(customDemandMapper.update(any(), any())).thenReturn(1);
        when(customFollowRecordMapper.insert(any())).thenReturn(1);

        CustomStatusRequest req = new CustomStatusRequest();
        req.setStatus(1);

        assertDoesNotThrow(() -> adminCustomService.updateStatus(1L, req));
        verify(customFollowRecordMapper).insert(argThat(r ->
                ((CustomFollowRecord) r).getContent().contains("开始跟进")
        ));
    }

    @Test
    @DisplayName("#287-2 定制需求完成")
    void testUpdateStatusToCompleted() {
        CustomDemand demand = buildDemand(1L, 1);
        when(customDemandMapper.selectById(1L)).thenReturn(demand);
        when(customDemandMapper.update(any(), any())).thenReturn(1);
        when(customFollowRecordMapper.insert(any())).thenReturn(1);

        CustomStatusRequest req = new CustomStatusRequest();
        req.setStatus(2);

        assertDoesNotThrow(() -> adminCustomService.updateStatus(1L, req));
        verify(customFollowRecordMapper).insert(argThat(r ->
                ((CustomFollowRecord) r).getContent().contains("已完成")
        ));
    }

    @Test
    @DisplayName("#287-3 关闭需求含备注")
    void testUpdateStatusToClosedWithRemark() {
        CustomDemand demand = buildDemand(1L, 1);
        when(customDemandMapper.selectById(1L)).thenReturn(demand);
        when(customDemandMapper.update(any(), any())).thenReturn(1);
        when(customFollowRecordMapper.insert(any())).thenReturn(1);

        CustomStatusRequest req = new CustomStatusRequest();
        req.setStatus(3);
        req.setRemark("客户已取消");

        assertDoesNotThrow(() -> adminCustomService.updateStatus(1L, req));
        verify(customFollowRecordMapper).insert(argThat(r ->
                ((CustomFollowRecord) r).getContent().contains("客户已取消")
        ));
    }

    @Test
    @DisplayName("#287-4 需求不存在")
    void testUpdateStatusNotFound() {
        when(customDemandMapper.selectById(999L)).thenReturn(null);

        CustomStatusRequest req = new CustomStatusRequest();
        req.setStatus(1);

        assertThrows(BizException.class, () -> adminCustomService.updateStatus(999L, req));
    }

    // #288 定制需求详情
    @Test
    @DisplayName("#288 定制需求详情")
    void testGetDetail() {
        CustomDemand demand = buildDemand(1L, 1);
        demand.setRequirementsText("带父母出游");
        when(customDemandMapper.selectById(1L)).thenReturn(demand);

        CustomFollowRecord record = new CustomFollowRecord();
        record.setId(10L);
        record.setDemandId(1L);
        record.setContent("已联系客户");
        record.setOperatorId(100L);
        record.setOperatorName("admin");
        record.setCreatedAt(LocalDateTime.now());
        when(customFollowRecordMapper.selectList(any())).thenReturn(List.of(record));

        CustomDetailVO detail = adminCustomService.getDetail(1L);

        assertNotNull(detail);
        assertEquals("云南", detail.getDestination());
        assertEquals("跟进中", detail.getStatusDesc());
        assertEquals(1, detail.getFollowRecords().size());
        assertEquals("已联系客户", detail.getFollowRecords().get(0).getContent());
    }

    @Test
    @DisplayName("#288-2 详情不存在")
    void testGetDetailNotFound() {
        when(customDemandMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> adminCustomService.getDetail(999L));
    }

    // 添加跟进记录
    @Test
    @DisplayName("添加跟进记录")
    void testAddFollowRecord() {
        CustomDemand demand = buildDemand(1L, 1);
        when(customDemandMapper.selectById(1L)).thenReturn(demand);
        when(customFollowRecordMapper.insert(any())).thenReturn(1);

        CustomFollowRequest req = new CustomFollowRequest();
        req.setContent("已与客户确认行程");

        assertDoesNotThrow(() -> adminCustomService.addFollowRecord(1L, req));
        verify(customFollowRecordMapper).insert(argThat(r -> {
            CustomFollowRecord cr = (CustomFollowRecord) r;
            return "已与客户确认行程".equals(cr.getContent())
                    && "admin".equals(cr.getOperatorName());
        }));
    }
}
