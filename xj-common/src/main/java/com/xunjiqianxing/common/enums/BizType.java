package com.xunjiqianxing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum BizType {

    /**
     * 跟团游
     */
    ROUTE("route", "跟团游"),

    /**
     * 租车自驾
     */
    CAR("car", "租车自驾"),

    /**
     * 民宿酒店
     */
    HOTEL("hotel", "民宿酒店"),

    /**
     * 景点门票
     */
    TICKET("ticket", "景点门票"),

    /**
     * 接送包车
     */
    TRANSFER("transfer", "接送包车"),

    /**
     * 美食玩乐
     */
    FOOD("food", "美食玩乐"),

    /**
     * 租赁服务
     */
    RENTAL("rental", "租赁服务"),

    /**
     * 旅游保险
     */
    INSURANCE("insurance", "旅游保险"),

    ;

    private final String code;
    private final String name;

    /**
     * 根据code获取枚举
     */
    public static BizType of(String code) {
        for (BizType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
