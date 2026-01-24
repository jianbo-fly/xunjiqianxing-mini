package com.xunjiqianxing.service.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xunjiqianxing.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单出行人表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_traveler")
public class OrderTraveler extends BaseEntity {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 身份证号(加密)
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 类型: 1成人 2儿童
     */
    private Integer travelerType;
}
