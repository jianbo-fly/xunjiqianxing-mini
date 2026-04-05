package com.xunjiqianxing.service.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("message_record")
public class MessageRecord implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String type;

    private String category;

    private String title;

    private String content;

    private String bizType;

    private Long bizId;

    private String bizNo;

    private String jumpUrl;

    @TableField("is_read")
    private Integer isRead;

    private LocalDateTime readTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
