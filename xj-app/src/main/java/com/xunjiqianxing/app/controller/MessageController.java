package com.xunjiqianxing.app.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.common.result.Result;
import com.xunjiqianxing.service.message.entity.MessageRecord;
import com.xunjiqianxing.service.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "消息接口", description = "消息通知相关接口")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/list")
    @Operation(summary = "消息列表", description = "获取用户消息列表，category: order=订单通知 system=系统消息 不传=全部")
    public Result<?> list(
            @Parameter(description = "分类: order/system，不传查全部")
            @RequestParam(required = false) String category,
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认20") @RequestParam(defaultValue = "20") int pageSize) {

        Long userId = StpUtil.getLoginIdAsLong();
        Page<MessageRecord> pageResult = messageService.listMessages(userId, category, page, pageSize);

        List<Map<String, Object>> list = pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @PostMapping("/read/{id}")
    @Operation(summary = "标记已读", description = "标记消息已读，id=all 时全部已读")
    public Result<Void> markRead(@PathVariable String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        if ("all".equals(id)) {
            messageService.markAllRead(userId);
        } else {
            messageService.markRead(userId, Long.parseLong(id));
        }
        return Result.success(null);
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "未读数量")
    public Result<Map<String, Integer>> unreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = messageService.getUnreadCount(userId);
        Map<String, Integer> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    private Map<String, Object> toVO(MessageRecord msg) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", String.valueOf(msg.getId()));
        vo.put("type", msg.getType());
        vo.put("category", msg.getCategory());
        vo.put("title", msg.getTitle());
        vo.put("content", msg.getContent());
        vo.put("read", msg.getIsRead() != null && msg.getIsRead() == 1);
        vo.put("link", msg.getJumpUrl());
        vo.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
        vo.put("timeText", formatTime(msg.getCreatedAt()));
        return vo;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(time, now);
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = ChronoUnit.HOURS.between(time, now);
        if (hours < 24) return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        long days = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate());
        if (days == 1) return "昨天";
        if (days < 7) {
            String[] weekdays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            return weekdays[time.getDayOfWeek().getValue()];
        }
        return time.format(DateTimeFormatter.ofPattern("MM-dd"));
    }
}
