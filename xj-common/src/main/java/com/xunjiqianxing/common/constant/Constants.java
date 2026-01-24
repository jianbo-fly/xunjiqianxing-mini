package com.xunjiqianxing.common.constant;

/**
 * 系统常量
 */
public interface Constants {

    /**
     * 默认支付超时时间（分钟）
     */
    int DEFAULT_PAY_TIMEOUT_MINUTES = 30;

    /**
     * 默认分页大小
     */
    int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    int MAX_PAGE_SIZE = 100;

    /**
     * 儿童年龄上限
     */
    int CHILD_AGE_LIMIT = 12;

    /**
     * 会员年费（元）
     */
    int MEMBER_PRICE = 99;

    /**
     * 领队认证费（元）
     */
    int LEADER_PRICE = 2000;

    /**
     * Redis Key 前缀
     */
    interface RedisKey {
        String PREFIX = "xj:";
        String USER_TOKEN = PREFIX + "user:token:";
        String ADMIN_TOKEN = PREFIX + "admin:token:";
        String ORDER_LOCK = PREFIX + "order:lock:";
        String STOCK_LOCK = PREFIX + "stock:lock:";
        String SMS_CODE = PREFIX + "sms:code:";
        String RATE_LIMIT = PREFIX + "rate:limit:";
    }

    /**
     * 登录类型
     */
    interface LoginType {
        String APP = "app";
        String ADMIN = "admin";
        String SUPPLIER = "supplier";
    }
}
