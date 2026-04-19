package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.exception.GlobalExceptionHandler;
import com.xunjiqianxing.service.message.entity.MessageRecord;
import com.xunjiqianxing.service.message.service.MessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MessageController 单元测试（#76 ~ #82）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    private MockMvc mockMvc;
    private MockedStatic<StpUtil> stpMock;
    private static final Long USER_ID = 10001L;

    @BeforeEach
    void setUp() {
        MessageController controller = new MessageController(messageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        stpMock = Mockito.mockStatic(StpUtil.class);
        stpMock.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
    }

    @AfterEach
    void tearDown() {
        stpMock.close();
    }

    private MessageRecord buildMsg(Long id, String type, String category, int isRead) {
        MessageRecord msg = new MessageRecord();
        msg.setId(id);
        msg.setUserId(USER_ID);
        msg.setType(type);
        msg.setCategory(category);
        msg.setTitle("测试消息");
        msg.setContent("消息内容");
        msg.setIsRead(isRead);
        msg.setCreatedAt(LocalDateTime.of(2026, 4, 10, 10, 30));
        return msg;
    }

    // #76 获取全部消息列表（分页）
    @Test
    @DisplayName("#76 获取全部消息列表（分页）")
    void testListAll() throws Exception {
        Page<MessageRecord> pageResult = new Page<>(1, 20);
        pageResult.setRecords(List.of(
                buildMsg(1L, "order_confirm", "order", 0),
                buildMsg(2L, "system", "system", 1)
        ));
        pageResult.setTotal(2);

        when(messageService.listMessages(eq(USER_ID), isNull(), eq(1), eq(20)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/message/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    // #77 按 category 筛选消息
    @Test
    @DisplayName("#77 按 category 筛选消息")
    void testListByCategory() throws Exception {
        Page<MessageRecord> pageResult = new Page<>(1, 20);
        pageResult.setRecords(List.of(buildMsg(1L, "order_confirm", "order", 0)));
        pageResult.setTotal(1);

        when(messageService.listMessages(eq(USER_ID), eq("order"), eq(1), eq(20)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/message/list").param("category", "order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].category").value("order"));

        verify(messageService).listMessages(USER_ID, "order", 1, 20);
    }

    // #78 无消息时返回空列表
    @Test
    @DisplayName("#78 无消息时返回空列表")
    void testListEmpty() throws Exception {
        Page<MessageRecord> pageResult = new Page<>(1, 20);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);

        when(messageService.listMessages(eq(USER_ID), isNull(), eq(1), eq(20)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/message/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // #79 标记单条消息已读
    @Test
    @DisplayName("#79 标记单条消息已读")
    void testMarkReadSingle() throws Exception {
        mockMvc.perform(post("/api/message/read/123"))
                .andExpect(status().isOk());

        verify(messageService).markRead(USER_ID, 123L);
        verify(messageService, never()).markAllRead(anyLong());
    }

    // #80 标记全部消息已读（id=all）
    @Test
    @DisplayName("#80 标记全部消息已读")
    void testMarkReadAll() throws Exception {
        mockMvc.perform(post("/api/message/read/all"))
                .andExpect(status().isOk());

        verify(messageService).markAllRead(USER_ID);
        verify(messageService, never()).markRead(anyLong(), anyLong());
    }

    // #81 消息 ID 非法（非数字且非 "all"）
    @Test
    @DisplayName("#81 消息 ID 非法")
    void testMarkReadInvalidId() throws Exception {
        mockMvc.perform(post("/api/message/read/abc"))
                .andExpect(status().is5xxServerError());
    }

    // #82 获取未读消息数
    @Test
    @DisplayName("#82 获取未读消息数")
    void testUnreadCount() throws Exception {
        when(messageService.getUnreadCount(USER_ID)).thenReturn(5);

        mockMvc.perform(get("/api/message/unreadCount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(5));
    }
}
