package com.xunjiqianxing.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(0, "success"),

    // 通用错误 400-499
    FAIL(400, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // 服务器错误 500-599
    SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 用户模块 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PHONE_BINDEXIST(1003, "手机号已被绑定"),
    TRAVELER_NOT_FOUND(1004, "出行人不存在"),
    TRAVELER_BINDEXIST(1005, "该身份证已存在"),

    // 商品模块 2001-2999
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_OFFLINE(2002, "商品已下架"),
    SKU_NOT_FOUND(2003, "套餐不存在"),
    STOCK_NOT_ENOUGH(2004, "库存不足"),
    DATE_NOT_AVAILABLE(2005, "该日期不可预订"),
    PRICE_NOT_CONFIG(2006, "该日期价格未配置"),

    // 订单模块 3001-3999
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态异常"),
    ORDER_EXPIRED(3003, "订单已过期"),
    ORDER_CANNOT_CANCEL(3004, "订单无法取消"),
    ORDER_CANNOT_REFUND(3005, "订单无法退款"),
    REFUND_NOT_FOUND(3006, "退款单不存在"),
    TRAVELER_COUNT_ERROR(3007, "出行人数量不正确"),

    // 支付模块 4001-4999
    PAY_ERROR(4001, "支付失败"),
    PAY_SIGN_ERROR(4002, "支付签名验证失败"),
    PAY_ORDER_PAID(4003, "订单已支付"),
    REFUND_ERROR(4004, "退款失败"),

    // 会员模块 5001-5999
    MEMBER_ALREADY(5001, "已经是会员"),
    MEMBER_EXPIRED(5002, "会员已过期"),

    // 领队模块 6001-6999
    LEADER_ALREADY(6001, "已经是领队"),
    LEADER_APPLYING(6002, "领队申请审核中"),
    LEADER_APPLY_REJECTED(6003, "领队申请已被拒绝"),

    // 推广模块 7001-7999
    PROMOTER_ALREADY(7001, "已经是推广员"),
    PROMOTER_APPLYING(7002, "推广员申请审核中"),
    PROMO_CODE_INVALID(7003, "推广码无效"),

    // 优惠券模块 8001-8999
    COUPON_NOT_FOUND(8001, "优惠券不存在"),
    COUPON_USED(8002, "优惠券已使用"),
    COUPON_EXPIRED(8003, "优惠券已过期"),
    COUPON_NOT_AVAILABLE(8004, "优惠券不可用"),

    ;

    private final int code;
    private final String message;
}
