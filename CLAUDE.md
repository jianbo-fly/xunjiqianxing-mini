# 寻迹千行 - 项目说明

## 项目概述
寻迹千行是一个综合性旅游服务平台，包含小程序端和后台管理系统。

## 技术栈
- Java 17 + Spring Boot 3.2.2
- MyBatis Plus 3.5.5
- Sa-Token 1.37.0 (认证)
- Redis + Redisson
- MySQL 8.0
- Knife4j 4.4.0 (API文档)
- Lombok edge-SNAPSHOT (兼容Java 25)

## 项目结构
```
xunjiqianxing/
├── xj-app/                    # 小程序后端（8080端口）
├── xj-admin/                  # 后台管理（8081端口）
├── xj-common/                 # 公共模块
├── xj-api/                    # API定义
└── xj-service/                # 业务服务层
    ├── xj-service-user/       # 用户服务
    ├── xj-service-product/    # 产品服务
    ├── xj-service-order/      # 订单服务
    ├── xj-service-custom/     # 定制游服务
    ├── xj-service-member/     # 会员服务
    ├── xj-service-promotion/  # 推广服务
    ├── xj-service-payment/    # 支付服务
    ├── xj-service-message/    # 消息服务
    └── xj-service-content/    # 内容服务
```

## 编码规范
- 每次完成功能后更新下方的API进度
- 使用Lombok注解简化代码
- 实体类继承BaseEntity（包含id, createdAt, updatedAt）
- 分页使用PageQuery（包含page, pageSize）
- 统一返回Result<T>

## 设计文档
小程序页面开发请参考设计文档：
- **页面交互文档**: `/Users/jianbo/project/real_travel/需求文档/寻迹千行页面交互文档_V4.md`
- 包含所有页面的UI设计、交互逻辑、字段说明
- 开发前必须先阅读对应页面的设计

---

# API开发进度

## ✅ 已完成的API模块

### 1. 用户登录模块 (`/api/auth`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/wx-login` | POST | 微信小程序登录 |
| `/api/auth/phone-login` | POST | 手机号登录 |
| `/api/auth/logout` | POST | 退出登录 |

### 2. 首页模块 (`/api/home`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/home/index` | GET | 首页数据 |
| `/api/home/banners` | GET | 轮播图列表 |
| `/api/home/navigations` | GET | 导航入口 |
| `/api/home/recommend-routes` | GET | 推荐线路 |

### 3. 线路模块 (`/api/route`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/route/list` | GET | 线路列表 |
| `/api/route/{id}` | GET | 线路详情 |
| `/api/route/{id}/packages` | GET | 线路套餐列表 |
| `/api/route/package/{id}` | GET | 套餐详情 |
| `/api/route/package/{id}/calendar` | GET | 价格日历 |

### 4. 订单模块 (`/api/order`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/order/create` | POST | 创建订单 |
| `/api/order/list` | GET | 订单列表 |
| `/api/order/{id}` | GET | 订单详情 |
| `/api/order/{id}/cancel` | POST | 取消订单 |
| `/api/order/{id}/refund` | POST | 申请退款 |

### 5. 定制游模块 (`/api/custom`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/custom/submit` | POST | 提交定制需求 |
| `/api/custom/list` | GET | 需求列表 |
| `/api/custom/{id}` | GET | 需求详情 |
| `/api/custom/{id}/cancel` | POST | 取消需求 |

### 6. 会员模块 (`/api/member`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/member/info` | GET | 会员信息 |
| `/api/member/buy` | POST | 购买会员 |
| `/api/member/benefits` | GET | 会员权益 |

### 7. 出行人管理模块 (`/api/traveler`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/traveler/list` | GET | 出行人列表 |
| `/api/traveler/{id}` | GET | 出行人详情 |
| `/api/traveler/add` | POST | 添加出行人 |
| `/api/traveler/{id}/update` | POST | 更新出行人 |
| `/api/traveler/{id}/delete` | POST | 删除出行人 |
| `/api/traveler/{id}/setDefault` | POST | 设为默认 |

### 8. 优惠券模块 (`/api/coupon`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/coupon/available` | GET | 可领取优惠券 |
| `/api/coupon/receive/{id}` | POST | 领取优惠券 |
| `/api/coupon/my` | GET | 我的优惠券 |
| `/api/coupon/usable` | GET | 订单可用优惠券 |

### 9. 推广员模块 (`/api/promoter`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/promoter/info` | GET | 推广员信息 |
| `/api/promoter/apply` | POST | 申请推广员 |
| `/api/promoter/bind` | POST | 绑定推广员 |
| `/api/promoter/commissions` | GET | 佣金记录 |
| `/api/promoter/withdraw` | POST | 申请提现 |
| `/api/promoter/withdraws` | GET | 提现记录 |
| `/api/promoter/statistics` | GET | 推广统计 |

### 10. 支付模块 (`/api/payment`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/payment/order/{orderId}` | POST | 创建订单支付 |
| `/api/payment/member/{orderId}` | POST | 创建会员支付 |
| `/api/payment/status/{paymentNo}` | GET | 查询支付状态 |
| `/notify/pay/wechat` | POST | 微信支付回调 |
| `/notify/refund/wechat` | POST | 微信退款回调 |

---

## ⏳ 待完成功能

- [x] 编译错误修复 ✅
- [x] 支付集成（微信支付）✅
- [ ] 消息通知服务
- [ ] 后台管理API
- [ ] 接口测试

---

## 代码优化

1. **订单状态枚举化**: 订单状态字段从 `VARCHAR(20)` 改为 `TINYINT` + `OrderStatus` 枚举
   - 枚举路径: `xj-service-order/src/main/java/com/xunjiqianxing/service/order/enums/OrderStatus.java`
   - 状态码: 0待支付、1待确认、2已确认、3出行中、4已完成、5已取消、6退款申请中、7已退款、8已关闭

## 已知问题

1. **Knife4j文档问题**: 8080端口doc.html显示空白，8081正常（暂时跳过）

---

*最后更新: 2026-01-24*
