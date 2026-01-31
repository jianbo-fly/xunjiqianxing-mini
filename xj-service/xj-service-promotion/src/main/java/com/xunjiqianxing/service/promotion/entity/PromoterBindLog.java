package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 推广绑定记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_bindlog")
public class PromoterBindLog extends BaseEntity {

    /**
     * 推广员ID
     */
    private Long promoterId;

    /**
     * 被绑定用户ID
     */
    private Long userId;

    /**
     * 来源: scan扫码
     */
    private String source;

    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;

    /**
     * 解绑原因
     */
    private String unbindReason;
}
