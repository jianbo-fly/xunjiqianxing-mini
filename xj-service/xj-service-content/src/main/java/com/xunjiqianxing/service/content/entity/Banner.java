package com.xunjiqianxing.service.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 轮播图实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_banner")
public class Banner extends BaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 跳转类型: 0无跳转 1线路详情 2外部链接 3小程序页面
     */
    private Integer linkType;

    /**
     * 跳转值
     */
    private String linkValue;

    /**
     * 状态: 0禁用 1启用
     */
    private Integer status;

    /**
     * 排序(越大越前)
     */
    private Integer sortOrder;

    /**
     * 开始展示时间
     */
    private LocalDateTime startTime;

    /**
     * 结束展示时间
     */
    private LocalDateTime endTime;
}
