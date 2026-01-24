package com.xunjiqianxing.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * ID生成器
 * 使用雪花算法生成分布式唯一ID
 */
public class IdGenerator {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 生成ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成ID字符串
     */
    public static String nextIdStr() {
        return SNOWFLAKE.nextIdStr();
    }

    /**
     * 生成订单号
     * 格式: 年月日时分秒 + 6位序列号
     */
    public static String generateOrderNo() {
        return cn.hutool.core.date.DateUtil.format(new java.util.Date(), "yyyyMMddHHmmss")
                + String.format("%06d", SNOWFLAKE.nextId() % 1000000);
    }

    /**
     * 生成退款单号
     */
    public static String generateRefundNo() {
        return "R" + generateOrderNo();
    }

    /**
     * 生成支付流水号
     */
    public static String generatePaymentNo() {
        return "P" + generateOrderNo();
    }

    /**
     * 生成推广码
     */
    public static String generatePromoCode() {
        return IdUtil.nanoId(8).toUpperCase();
    }
}
