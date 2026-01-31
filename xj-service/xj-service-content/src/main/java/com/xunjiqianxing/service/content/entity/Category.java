package com.xunjiqianxing.service.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_category")
public class Category extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 关联业务类型
     */
    private String bizType;

    /**
     * 状态: 0禁用 1启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
