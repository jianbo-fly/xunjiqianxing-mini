package com.xunjiqianxing.service.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 推广员扫码记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promoter_scan_record")
public class PromoterScanRecord extends BaseEntity {

    /**
     * 推广员ID
     */
    private Long promoterId;

    /**
     * 推广员用户ID
     */
    private Long promoterUserId;

    /**
     * 推广码
     */
    private String promoCode;

    /**
     * 扫码用户ID（未登录时为空）
     */
    private Long scanUserId;
}
