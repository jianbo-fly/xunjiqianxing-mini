package com.xunjiqianxing.service.custom.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定制需求跟进记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("custom_follow_record")
public class CustomFollowRecord extends BaseEntity {

    /**
     * 定制需求ID
     */
    private Long demandId;

    /**
     * 跟进内容
     */
    private String content;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;
}
