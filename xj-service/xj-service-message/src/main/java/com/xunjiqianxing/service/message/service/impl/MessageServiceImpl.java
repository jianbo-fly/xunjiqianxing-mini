package com.xunjiqianxing.service.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunjiqianxing.service.message.entity.MessageRecord;
import com.xunjiqianxing.service.message.mapper.MessageRecordMapper;
import com.xunjiqianxing.service.message.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageRecordMapper, MessageRecord>
        implements MessageService {

    @Override
    public Page<MessageRecord> listMessages(Long userId, String category, int page, int pageSize) {
        Page<MessageRecord> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<MessageRecord> wrapper = new LambdaQueryWrapper<MessageRecord>()
                .eq(MessageRecord::getUserId, userId)
                .eq(StringUtils.hasText(category), MessageRecord::getCategory, category)
                .orderByDesc(MessageRecord::getCreatedAt);
        return baseMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public void markRead(Long userId, Long messageId) {
        LambdaUpdateWrapper<MessageRecord> wrapper = new LambdaUpdateWrapper<MessageRecord>()
                .eq(MessageRecord::getId, messageId)
                .eq(MessageRecord::getUserId, userId)
                .eq(MessageRecord::getIsRead, 0)
                .set(MessageRecord::getIsRead, 1)
                .set(MessageRecord::getReadTime, LocalDateTime.now());
        baseMapper.update(null, wrapper);
    }

    @Override
    public void markAllRead(Long userId) {
        LambdaUpdateWrapper<MessageRecord> wrapper = new LambdaUpdateWrapper<MessageRecord>()
                .eq(MessageRecord::getUserId, userId)
                .eq(MessageRecord::getIsRead, 0)
                .set(MessageRecord::getIsRead, 1)
                .set(MessageRecord::getReadTime, LocalDateTime.now());
        baseMapper.update(null, wrapper);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return Math.toIntExact(baseMapper.selectCount(
                new LambdaQueryWrapper<MessageRecord>()
                        .eq(MessageRecord::getUserId, userId)
                        .eq(MessageRecord::getIsRead, 0)
        ));
    }

    @Override
    public void sendMessage(Long userId, String type, String category, String title, String content,
                            String bizType, Long bizId, String bizNo, String jumpUrl) {
        MessageRecord record = new MessageRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setCategory(category);
        record.setTitle(title);
        record.setContent(content);
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setBizNo(bizNo);
        record.setJumpUrl(jumpUrl);
        record.setIsRead(0);
        save(record);
    }
}
