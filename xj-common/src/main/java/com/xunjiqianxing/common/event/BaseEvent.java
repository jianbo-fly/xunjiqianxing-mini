package com.xunjiqianxing.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 事件基类
 */
@Getter
public abstract class BaseEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 事件发生时间
     */
    private final LocalDateTime eventTime;

    /**
     * 事件ID
     */
    private final String eventId;

    public BaseEvent(Object source) {
        super(source);
        this.eventTime = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取事件类型
     */
    public abstract String getEventType();
}
