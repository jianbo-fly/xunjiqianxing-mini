package com.xunjiqianxing.service.custom.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 定制需求表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "custom_demand", autoResultMap = true)
public class CustomDemand extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 出行开始日期
     */
    private LocalDate travelDateStart;

    /**
     * 出行结束日期
     */
    private LocalDate travelDateEnd;

    /**
     * 出行时间类型
     */
    private String travelDateType;

    /**
     * 出行天数
     */
    private String travelDays;

    /**
     * 成人数
     */
    private Integer adultCount;

    /**
     * 儿童数
     */
    private Integer childCount;

    /**
     * 预算范围
     */
    private String budget;

    /**
     * 其他需求标签
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> requirements;

    /**
     * 补充需求
     */
    private String requirementsText;

    /**
     * 状态: 0待处理 1跟进中 2已完成 3已关闭
     */
    private Integer status;
}
