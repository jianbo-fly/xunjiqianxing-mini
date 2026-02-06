-- =============================================
-- 寻迹千行 数据库初始化脚本
-- 版本: V1.0
-- 日期: 2026-01
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS xunjiqianxing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xunjiqianxing;

-- =============================================
-- 用户域
-- =============================================

-- 用户表
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    openid VARCHAR(64) NOT NULL COMMENT '微信openid',
    unionid VARCHAR(64) COMMENT '微信unionid',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0未知 1男 2女',

    is_member TINYINT DEFAULT 0 COMMENT '是否会员: 0否 1是',
    member_expire_at DATETIME COMMENT '会员到期时间',
    is_leader TINYINT DEFAULT 0 COMMENT '是否领队: 0否 1是',
    is_promoter TINYINT DEFAULT 0 COMMENT '是否推广员: 0否 1是',

    points INT DEFAULT 0 COMMENT '积分余额',

    bindpromoter_id BIGINT COMMENT '绑定的推广员ID',
    bindpromoter_at DATETIME COMMENT '绑定时间',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    last_login_at DATETIME COMMENT '最后登录时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_bindpromoter (bindpromoter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 出行人表
DROP TABLE IF EXISTS user_traveler;
CREATE TABLE user_traveler (
    id BIGINT PRIMARY KEY COMMENT '出行人ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    id_type TINYINT DEFAULT 1 COMMENT '证件类型: 1身份证 2护照 3港澳通行证 4台湾通行证',
    id_no VARCHAR(200) NOT NULL COMMENT '证件号码(加密存储)',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    gender TINYINT COMMENT '性别: 1男 2女',
    birthday DATE COMMENT '出生日期',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认',
    emergency_name VARCHAR(50) COMMENT '紧急联系人姓名',
    emergency_phone VARCHAR(20) COMMENT '紧急联系人电话',
    remark VARCHAR(500) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',

    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出行人表';

-- =============================================
-- 商品域
-- =============================================

-- 商品主表
DROP TABLE IF EXISTS product_main;
CREATE TABLE product_main (
    id BIGINT PRIMARY KEY COMMENT '商品ID',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型: route/car/hotel/ticket/transfer/food/rental/insurance',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',

    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    subtitle VARCHAR(500) COMMENT '副标题',
    cover_image VARCHAR(500) NOT NULL COMMENT '封面图',
    images JSON COMMENT '轮播图',
    tags JSON COMMENT '标签',

    city_code VARCHAR(20) COMMENT '城市编码',
    city_name VARCHAR(50) COMMENT '城市名称',

    min_price DECIMAL(10,2) COMMENT '最低价',
    original_price DECIMAL(10,2) COMMENT '原价(划线价)',

    sales_count INT DEFAULT 0 COMMENT '销量',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    score DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',

    status TINYINT DEFAULT 0 COMMENT '状态: 0下架 1上架',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2驳回',
    audit_remark VARCHAR(500) COMMENT '审核备注',

    sort_order INT DEFAULT 0 COMMENT '排序(越大越前)',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_biz_type (biz_type),
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status, is_deleted),
    INDEX idx_city (city_code),
    INDEX idx_recommend (is_recommend, sort_order DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表';

-- 跟团游扩展表
DROP TABLE IF EXISTS product_route;
CREATE TABLE product_route (
    product_id BIGINT PRIMARY KEY COMMENT '商品ID',
    category VARCHAR(20) NOT NULL COMMENT '分类: domestic国内游 overseas出境游',
    departure_city VARCHAR(50) NOT NULL COMMENT '出发城市',
    destination VARCHAR(100) NOT NULL COMMENT '目的地',
    cost_exclude TEXT COMMENT '费用不含(富文本)',
    booking_notice TEXT COMMENT '预订须知(富文本)',
    tips TEXT COMMENT '温馨提示(富文本)',

    INDEX idx_category (category),
    INDEX idx_departure (departure_city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟团游扩展表';

-- SKU表
DROP TABLE IF EXISTS product_sku;
CREATE TABLE product_sku (
    id BIGINT PRIMARY KEY COMMENT 'SKU ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',

    name VARCHAR(200) NOT NULL COMMENT 'SKU名称',
    tags JSON COMMENT '标签',
    attrs JSON NOT NULL COMMENT '业务属性',

    base_price DECIMAL(10,2) COMMENT '基准价',
    child_price DECIMAL(10,2) COMMENT '儿童基准价',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    sort_order INT DEFAULT 0 COMMENT '排序',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_product (product_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU表';

-- 价格库存表
DROP TABLE IF EXISTS product_price_stock;
CREATE TABLE product_price_stock (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    date DATE NOT NULL COMMENT '日期',

    price DECIMAL(10,2) NOT NULL COMMENT '成人价格',
    child_price DECIMAL(10,2) COMMENT '儿童价格',

    stock INT NOT NULL DEFAULT 0 COMMENT '总库存',
    sold INT DEFAULT 0 COMMENT '已售数量',
    locked INT DEFAULT 0 COMMENT '锁定数量',

    status TINYINT DEFAULT 1 COMMENT '状态: 0不可售 1可售',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_sku_date (sku_id, date),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格库存表';

-- =============================================
-- 订单域
-- =============================================

-- 订单主表
DROP TABLE IF EXISTS order_main;
CREATE TABLE order_main (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',

    user_id BIGINT NOT NULL COMMENT '用户ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',

    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(500) COMMENT '商品图片',
    sku_name VARCHAR(200) NOT NULL COMMENT 'SKU名称',

    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    days INT COMMENT '天数',
    adult_count INT DEFAULT 0 COMMENT '成人数',
    child_count INT DEFAULT 0 COMMENT '儿童数',
    quantity INT DEFAULT 1 COMMENT '数量',

    adult_price DECIMAL(10,2) COMMENT '成人单价',
    child_price DECIMAL(10,2) COMMENT '儿童单价',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    refund_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已退金额',

    coupon_id BIGINT COMMENT '优惠券ID',
    coupon_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠券金额',

    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系人电话',

    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0待支付 1待确认 2已确认 3出行中 4已完成 5已取消 6退款申请中 7已退款 8已关闭',

    pay_status TINYINT DEFAULT 0 COMMENT '支付状态: 0未支付 1已支付',
    pay_time DATETIME COMMENT '支付时间',
    pay_trade_no VARCHAR(64) COMMENT '支付流水号',

    confirm_time DATETIME COMMENT '确认时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',

    cancel_time DATETIME COMMENT '取消时间',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    cancel_type TINYINT COMMENT '取消类型: 1用户取消 2超时取消 3商家拒绝',

    complete_time DATETIME COMMENT '完成时间',

    ext_data JSON COMMENT '扩展数据',

    promoter_id BIGINT COMMENT '推广员ID',

    remark VARCHAR(500) COMMENT '用户备注',
    admin_remark VARCHAR(500) COMMENT '管理员备注',

    expire_at DATETIME COMMENT '支付超时时间',

    is_deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user (user_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status),
    INDEX idx_start_date (start_date),
    INDEX idx_created (created_at),
    INDEX idx_promoter (promoter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单出行人表
DROP TABLE IF EXISTS order_traveler;
CREATE TABLE order_traveler (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    id_card VARCHAR(200) NOT NULL COMMENT '身份证号(加密)',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    traveler_type TINYINT NOT NULL COMMENT '类型: 1成人 2儿童',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单出行人表';

-- 退款表
DROP TABLE IF EXISTS order_refund;
CREATE TABLE order_refund (
    id BIGINT PRIMARY KEY,
    refund_no VARCHAR(32) NOT NULL COMMENT '退款编号',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',

    refund_amount DECIMAL(10,2) NOT NULL COMMENT '申请退款金额',
    actual_amount DECIMAL(10,2) COMMENT '实际退款金额',
    refund_ratio INT COMMENT '退款比例(%)',

    reason VARCHAR(500) COMMENT '退款原因',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已驳回',

    audit_time DATETIME COMMENT '审核时间',
    audit_by BIGINT COMMENT '审核人',
    audit_remark VARCHAR(500) COMMENT '审核备注',

    refund_time DATETIME COMMENT '退款到账时间',
    refund_trade_no VARCHAR(64) COMMENT '退款流水号',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_refund_no (refund_no),
    INDEX idx_order (order_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款表';

-- 订单日志表
DROP TABLE IF EXISTS order_log;
CREATE TABLE order_log (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',

    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    from_status TINYINT COMMENT '原状态',
    to_status TINYINT COMMENT '新状态',

    content VARCHAR(500) COMMENT '操作内容',
    operator_type TINYINT COMMENT '操作人类型: 1用户 2供应商 3管理员 4系统',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人名称',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单日志表';

-- =============================================
-- 支付域
-- =============================================

-- 支付记录表
DROP TABLE IF EXISTS payment_record;
CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY,
    payment_no VARCHAR(32) NOT NULL COMMENT '支付流水号',

    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型: order/member/leader',
    biz_id BIGINT NOT NULL COMMENT '业务ID',
    biz_no VARCHAR(32) NOT NULL COMMENT '业务编号',

    user_id BIGINT NOT NULL COMMENT '用户ID',

    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',

    pay_channel VARCHAR(20) NOT NULL DEFAULT 'wechat' COMMENT '支付渠道',
    pay_type VARCHAR(20) COMMENT '支付方式: jsapi/native/app',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待支付 1已支付 2已关闭',

    prepay_id VARCHAR(64) COMMENT '预支付ID',
    transaction_id VARCHAR(64) COMMENT '微信支付订单号',
    pay_time DATETIME COMMENT '支付成功时间',

    notify_data TEXT COMMENT '回调原始数据',

    expire_at DATETIME COMMENT '超时时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_payment_no (payment_no),
    INDEX idx_biz (biz_type, biz_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- =============================================
-- 会员域
-- =============================================

-- 会员订单表
DROP TABLE IF EXISTS member_order;
CREATE TABLE member_order (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',

    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',

    start_date DATE NOT NULL COMMENT '会员开始日期',
    end_date DATE NOT NULL COMMENT '会员结束日期',
    duration_months INT NOT NULL COMMENT '购买月数',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待支付 1已支付 2已取消',
    pay_time DATETIME COMMENT '支付时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单表';

-- =============================================
-- 领队域
-- =============================================

-- 领队信息表
DROP TABLE IF EXISTS leader_info;
CREATE TABLE leader_info (
    id BIGINT PRIMARY KEY COMMENT '领队ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',

    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像',
    expertise JSON COMMENT '擅长领域',
    intro TEXT COMMENT '个人简介',
    certificate_images JSON COMMENT '资质证书图片',

    trip_count INT DEFAULT 0 COMMENT '带队次数',
    total_commission DECIMAL(10,2) DEFAULT 0 COMMENT '累计佣金',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领队信息表';

-- 领队申请表
DROP TABLE IF EXISTS leader_apply;
CREATE TABLE leader_apply (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',

    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    expertise JSON COMMENT '擅长领域',
    intro TEXT COMMENT '个人简介',
    certificate_images JSON COMMENT '资质证书图片',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1已通过 2已驳回',

    audit_time DATETIME COMMENT '审核时间',
    audit_by BIGINT COMMENT '审核人',
    audit_remark VARCHAR(500) COMMENT '审核备注',

    pay_status TINYINT DEFAULT 0 COMMENT '支付状态: 0未支付 1已支付',
    pay_amount DECIMAL(10,2) COMMENT '支付金额',
    pay_time DATETIME COMMENT '支付时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领队申请表';

-- =============================================
-- 推广域
-- =============================================

-- 推广员表
DROP TABLE IF EXISTS promotion_user;
CREATE TABLE promotion_user (
    id BIGINT PRIMARY KEY COMMENT '推广员ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    promo_code VARCHAR(32) NOT NULL COMMENT '推广码',

    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',

    level TINYINT DEFAULT 1 COMMENT '等级: 1普通 2银牌 3金牌',
    commission_rate DECIMAL(5,4) DEFAULT 0.0500 COMMENT '佣金比例',

    total_commission DECIMAL(12,2) DEFAULT 0 COMMENT '累计佣金',
    available_commission DECIMAL(12,2) DEFAULT 0 COMMENT '可提现佣金',
    withdrawn_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已提现金额',

    promoted_count INT DEFAULT 0 COMMENT '推广人数',
    order_count INT DEFAULT 0 COMMENT '成交订单数',
    order_amount DECIMAL(12,2) DEFAULT 0 COMMENT '成交金额',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1正常 2禁用',
    audit_at DATETIME COMMENT '审核时间',
    remark VARCHAR(200) COMMENT '备注',

    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user (user_id),
    UNIQUE KEY uk_code (promo_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广员表';

-- 推广绑定记录表
DROP TABLE IF EXISTS promotion_bindlog;
CREATE TABLE promotion_bindlog (
    id BIGINT PRIMARY KEY,
    promoter_id BIGINT NOT NULL COMMENT '推广员ID',
    user_id BIGINT NOT NULL COMMENT '被绑定用户ID',

    source VARCHAR(50) COMMENT '来源: scan扫码',

    unbind_time DATETIME COMMENT '解绑时间',
    unbind_reason VARCHAR(100) COMMENT '解绑原因',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_promoter (promoter_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广绑定记录表';

-- 积分记录表
DROP TABLE IF EXISTS promotion_points;
CREATE TABLE promotion_points (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',

    points INT NOT NULL COMMENT '积分数',
    balance INT NOT NULL COMMENT '变动后余额',

    source_type VARCHAR(32) NOT NULL COMMENT '来源类型',
    source_id BIGINT COMMENT '来源ID',
    source_no VARCHAR(32) COMMENT '来源编号',

    remark VARCHAR(200) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user (user_id),
    INDEX idx_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 推广员佣金记录表
DROP TABLE IF EXISTS promotion_commission;
CREATE TABLE promotion_commission (
    id BIGINT PRIMARY KEY,
    promoter_id BIGINT NOT NULL COMMENT '推广员ID',
    promoter_user_id BIGINT NOT NULL COMMENT '推广员用户ID',
    from_user_id BIGINT NOT NULL COMMENT '被推广用户ID',

    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    commission_rate DECIMAL(5,4) NOT NULL COMMENT '佣金比例',
    commission_amount DECIMAL(10,2) NOT NULL COMMENT '佣金金额',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待结算 1已结算 2已取消',
    settle_at DATETIME COMMENT '结算时间',
    remark VARCHAR(200) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_promoter (promoter_id),
    INDEX idx_order (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广员佣金记录表';

-- 推广员提现记录表
DROP TABLE IF EXISTS promotion_withdraw;
CREATE TABLE promotion_withdraw (
    id BIGINT PRIMARY KEY,
    promoter_id BIGINT NOT NULL COMMENT '推广员ID',
    promoter_user_id BIGINT NOT NULL COMMENT '推广员用户ID',

    amount DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    withdraw_type TINYINT NOT NULL COMMENT '提现方式: 1微信 2支付宝 3银行卡',
    account VARCHAR(100) NOT NULL COMMENT '收款账号',
    account_name VARCHAR(50) NOT NULL COMMENT '收款人姓名',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待审核 1审核通过 2已打款 3已拒绝',
    audit_at DATETIME COMMENT '审核时间',
    pay_at DATETIME COMMENT '打款时间',
    reject_reason VARCHAR(200) COMMENT '拒绝原因',
    remark VARCHAR(200) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_promoter (promoter_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广员提现记录表';

-- =============================================
-- 定制域
-- =============================================

-- 定制需求表
DROP TABLE IF EXISTS custom_demand;
CREATE TABLE custom_demand (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',

    destination VARCHAR(100) NOT NULL COMMENT '目的地',
    travel_date_start DATE COMMENT '出行开始日期',
    travel_date_end DATE COMMENT '出行结束日期',
    travel_date_type VARCHAR(20) COMMENT '出行时间类型',
    travel_days VARCHAR(20) COMMENT '出行天数',
    adult_count INT DEFAULT 1 COMMENT '成人数',
    child_count INT DEFAULT 0 COMMENT '儿童数',
    budget VARCHAR(20) COMMENT '预算范围',
    requirements JSON COMMENT '其他需求标签',
    requirements_text VARCHAR(500) COMMENT '补充需求',

    status TINYINT DEFAULT 0 COMMENT '状态: 0待处理 1跟进中 2已完成 3已关闭',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制需求表';

-- 定制跟进记录表
DROP TABLE IF EXISTS custom_followup;
CREATE TABLE custom_followup (
    id BIGINT PRIMARY KEY,
    demand_id BIGINT NOT NULL COMMENT '需求ID',

    content TEXT NOT NULL COMMENT '跟进内容',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_demand (demand_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定制跟进记录表';

-- =============================================
-- 消息域
-- =============================================

-- 消息记录表
DROP TABLE IF EXISTS message_record;
CREATE TABLE message_record (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',

    type VARCHAR(32) NOT NULL COMMENT '消息类型',
    category VARCHAR(20) NOT NULL DEFAULT 'order' COMMENT '消息分类: order/system',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',

    biz_type VARCHAR(32) COMMENT '业务类型',
    biz_id BIGINT COMMENT '业务ID',
    biz_no VARCHAR(32) COMMENT '业务编号',

    jump_url VARCHAR(255) COMMENT '跳转路径',

    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    read_time DATETIME COMMENT '阅读时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_created (user_id, created_at DESC),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

-- =============================================
-- 内容域
-- =============================================

-- 轮播图表
DROP TABLE IF EXISTS content_banner;
CREATE TABLE content_banner (
    id BIGINT PRIMARY KEY,

    title VARCHAR(100) COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',

    link_type TINYINT DEFAULT 0 COMMENT '跳转类型: 0无跳转 1线路详情 2外部链接 3小程序页面',
    link_value VARCHAR(500) COMMENT '跳转值',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    sort_order INT DEFAULT 0 COMMENT '排序(越大越前)',

    start_time DATETIME COMMENT '开始展示时间',
    end_time DATETIME COMMENT '结束展示时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_status_sort (status, sort_order DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 分类表
DROP TABLE IF EXISTS content_category;
CREATE TABLE content_category (
    id BIGINT PRIMARY KEY,

    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(500) COMMENT '图标URL',

    biz_type VARCHAR(32) COMMENT '关联业务类型',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    sort_order INT DEFAULT 0 COMMENT '排序',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_status_sort (status, sort_order DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- =============================================
-- 系统域
-- =============================================

-- 供应商表
DROP TABLE IF EXISTS system_supplier;
CREATE TABLE system_supplier (
    id BIGINT PRIMARY KEY,

    name VARCHAR(100) NOT NULL COMMENT '商家名称',
    logo VARCHAR(500) COMMENT 'Logo',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    intro TEXT COMMENT '简介',
    license_images JSON COMMENT '资质证书图片',

    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码(加密)',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',

    last_login_at DATETIME COMMENT '最后登录时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 管理员表
DROP TABLE IF EXISTS system_admin;
CREATE TABLE system_admin (
    id BIGINT PRIMARY KEY,

    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码(加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像',
    phone VARCHAR(20) COMMENT '手机号',

    role VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色: super_admin/admin',

    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    last_login_at DATETIME COMMENT '最后登录时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 系统配置表
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY,

    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'string' COMMENT '类型: string/number/json/boolean',

    remark VARCHAR(200) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 优惠券域 (P1)
-- =============================================

-- 优惠券模板表
DROP TABLE IF EXISTS coupon_template;
CREATE TABLE coupon_template (
    id BIGINT PRIMARY KEY,

    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type TINYINT NOT NULL COMMENT '类型: 1满减券 2折扣券 3无门槛券',

    threshold DECIMAL(10,2) DEFAULT 0 COMMENT '满减门槛金额',
    amount DECIMAL(10,2) COMMENT '优惠金额(满减券/无门槛券)',
    discount DECIMAL(5,2) COMMENT '折扣率(如0.9表示9折)',
    max_amount DECIMAL(10,2) COMMENT '最高减免金额(折扣券用)',

    scope TINYINT DEFAULT 0 COMMENT '适用范围: 0全场 1指定线路 2指定分类',
    scope_ids VARCHAR(500) COMMENT '适用线路/分类ID列表，逗号分隔',

    valid_type TINYINT NOT NULL COMMENT '有效期类型: 1固定日期 2领取后N天',
    valid_start DATETIME COMMENT '有效期开始(固定日期)',
    valid_end DATETIME COMMENT '有效期结束(固定日期)',
    valid_days INT COMMENT '有效天数(领取后)',

    total_count INT COMMENT '发放总量(-1不限)',
    received_count INT DEFAULT 0 COMMENT '已领取数量',
    per_limit INT DEFAULT 1 COMMENT '每人限领',

    status TINYINT DEFAULT 1 COMMENT '状态: 0停用 1启用',
    member_only TINYINT DEFAULT 0 COMMENT '是否仅会员可用: 0否 1是',
    description VARCHAR(500) COMMENT '描述',

    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 用户优惠券表
DROP TABLE IF EXISTS coupon_user;
CREATE TABLE coupon_user (
    id BIGINT PRIMARY KEY,

    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券模板ID',

    coupon_name VARCHAR(100) NOT NULL COMMENT '券名称(冗余)',
    coupon_type TINYINT NOT NULL COMMENT '券类型: 1满减券 2折扣券 3无门槛券',
    threshold DECIMAL(10,2) COMMENT '满减门槛',
    amount DECIMAL(10,2) COMMENT '优惠金额',
    discount DECIMAL(5,2) COMMENT '折扣率',
    max_amount DECIMAL(10,2) COMMENT '最高减免金额',

    valid_start DATETIME NOT NULL COMMENT '有效期开始',
    valid_end DATETIME NOT NULL COMMENT '有效期结束',

    status TINYINT DEFAULT 0 COMMENT '状态: 0未使用 1已使用 2已过期',

    used_at DATETIME COMMENT '使用时间',
    used_order_no VARCHAR(32) COMMENT '使用订单号',

    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_status (user_id, status),
    INDEX idx_valid_end (valid_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化系统配置
INSERT INTO system_config (id, config_key, config_value, config_type, remark) VALUES
(1, 'service_phone', '400-xxx-xxxx', 'string', '客服电话'),
(2, 'wework_customer_url', '', 'string', '企业微信客服链接'),
(3, 'pay_timeout_minutes', '30', 'number', '支付超时时间(分钟)'),
(4, 'member_price', '99', 'number', '会员年费(元)'),
(5, 'leader_price', '2000', 'number', '领队认证费(元)'),
(6, 'child_age_limit', '12', 'number', '儿童年龄上限'),
(7, 'refund_rules', '[{"days":7,"ratio":100},{"days":3,"ratio":70},{"days":1,"ratio":50},{"days":0,"ratio":0}]', 'json', '退款规则'),
(8, 'points_rule_order', '1', 'number', '下单积分比例(每消费1元得x积分)'),
(9, 'points_rule_promote', '10', 'number', '推广积分比例(每成交1单得x积分)');

-- 初始化超级管理员 (密码: admin123, 使用BCrypt加密)
-- 如果密码不正确，可通过 POST /admin/auth/initAdmin 重置
INSERT INTO system_admin (id, username, password, nickname, role, status) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 'super_admin', 1);

-- =============================================
-- 完成
-- =============================================
SELECT '数据库初始化完成!' AS result;


-- 添加行程安排和费用包含字段到 product_route 表
ALTER TABLE product_route
    ADD COLUMN itinerary JSON COMMENT '行程安排(JSON数组)',
  ADD COLUMN cost_include TEXT COMMENT '费用包含(富文本)';
