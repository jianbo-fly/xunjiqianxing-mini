package com.xunjiqianxing.service.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunjiqianxing.service.message.entity.MessageRecord;

public interface MessageService {

    /**
     * 获取用户消息列表
     * @param userId 用户ID
     * @param category 分类: null=全部, order=订单通知, system=系统消息
     * @param page 页码
     * @param pageSize 每页数量
     */
    Page<MessageRecord> listMessages(Long userId, String category, int page, int pageSize);

    /**
     * 标记单条消息已读
     */
    void markRead(Long userId, Long messageId);

    /**
     * 标记全部已读
     */
    void markAllRead(Long userId);

    /**
     * 获取未读数量
     */
    int getUnreadCount(Long userId);

    /**
     * 发送/保存消息
     *
     * @param userId   接收用户ID
     * @param type     消息子类型: order_confirm / payment_success / travel_reminder / refund_success / refund_reject / system
     * @param category 分类: order / system
     * @param title    标题
     * @param content  内容
     * @param bizType  业务类型 (order / member / ...)
     * @param bizId    业务主键ID
     * @param bizNo    业务单号
     * @param jumpUrl  跳转路径
     */
    void sendMessage(Long userId, String type, String category, String title, String content,
                     String bizType, Long bizId, String bizNo, String jumpUrl);
}
