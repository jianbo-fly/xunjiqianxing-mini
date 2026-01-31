# 寻迹千行 🏔️

<p align="center">
  <b>综合旅游服务平台</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.2-green" alt="Spring Boot 3.2.2">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue" alt="MySQL 8.0">
  <img src="https://img.shields.io/badge/Redis-7.x-red" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

---

## 📖 项目简介

**寻迹千行**是一个综合旅游服务平台，一期聚焦「跟团游」和「定制游」业务，后续扩展租车、民宿、门票、美食等模块。

### 产品形态

| 端 | 形态 | 用户 |
|---|---|---|
| C端 | 微信小程序 | 普通用户、会员、领队、推广员 |
| B端 | PC Web管理后台 | 供应商、管理员 |

### 核心特色

- **双业务线**：跟团游（标品）+ 定制游（非标品）
- **多角色体系**：普通用户 / 会员(¥99/年) / 领队(¥2000认证) / 推广员
- **社交裂变**：推广中心、专属推广码、积分体系
- **企微客服**：替代 IM 系统，降低开发成本

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 LTS | 开发语言 |
| Spring Boot | 3.2.2 | 应用框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| Sa-Token | 1.37.0 | 权限认证 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.x | 缓存、分布式锁、Session |
| Druid | 1.2.21 | 数据库连接池 |
| Redisson | 3.25.2 | Redis 客户端 |
| Knife4j | 4.4.0 | API 文档 |
| Hutool | 5.8.25 | 工具库 |
| MapStruct | 1.5.5 | Bean 转换 |

### 第三方服务

| 服务 | 用途 |
|------|------|
| 微信小程序 API | 登录、手机号获取 |
| 微信支付 v3 | 支付、退款 |
| 微信订阅消息 | 消息推送 |
| 企业微信 API | 客服、内部通知 |
| 腾讯云 COS | 图片存储 |

---

## 📁 项目结构

```
xunjiqianxing/
├── pom.xml                        # 父 POM
├── docs/
│   └── sql/
│       └── init.sql               # 数据库初始化脚本
│
├── xj-common/                     # 公共模块
│   └── src/main/java/
│       └── com/xunjiqianxing/common/
│           ├── base/              # 基类 (BaseEntity, PageQuery)
│           ├── enums/             # 枚举 (BizType, OrderStatus)
│           ├── exception/         # 异常处理
│           ├── result/            # 统一响应 (Result<T>)
│           ├── config/            # 公共配置
│           └── utils/             # 工具类
│
├── xj-api/                        # 接口定义模块
│   ├── xj-api-user/
│   ├── xj-api-product/
│   ├── xj-api-order/
│   ├── xj-api-payment/
│   └── xj-api-message/
│
├── xj-service/                    # 服务实现模块
│   ├── xj-service-user/           # 用户服务
│   ├── xj-service-product/        # 商品服务
│   ├── xj-service-order/          # 订单服务
│   ├── xj-service-payment/        # 支付服务
│   ├── xj-service-member/         # 会员服务
│   ├── xj-service-promotion/      # 推广服务
│   ├── xj-service-message/        # 消息服务
│   ├── xj-service-content/        # 内容服务
│   └── xj-service-custom/         # 定制服务
│
├── xj-biz/                        # 业务扩展模块
│   ├── xj-biz-route/              # 跟团游业务
│   ├── xj-biz-car/                # 租车业务（预留）
│   └── xj-biz-hotel/              # 酒店业务（预留）
│
├── xj-app/                        # 小程序聚合服务 (端口: 8080)
│   └── src/main/java/
│       └── com/xunjiqianxing/app/
│           ├── XjAppApplication.java
│           ├── controller/        # C端 API
│           ├── dto/               # 数据传输对象
│           ├── config/            # 配置
│           └── listener/          # 事件监听器
│
└── xj-admin/                      # 管理后台聚合服务 (端口: 8081)
    └── src/main/java/
        └── com/xunjiqianxing/admin/
            ├── XjAdminApplication.java
            └── controller/        # B端 API
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/xunjiqianxing.git
cd xunjiqianxing
```

### 2. 初始化数据库

```bash
mysql -u root -p < docs/sql/init.sql
```

### 3. 修改配置

编辑 `xj-app/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xunjiqianxing
    username: your-username
    password: your-password

  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动项目

```bash
# 编译项目
mvn clean install -DskipTests

# 启动小程序后端
cd xj-app
mvn spring-boot:run

# 启动管理后台（另一个终端）
cd xj-admin
mvn spring-boot:run
```

### 5. 访问接口文档

- 小程序 API：http://localhost:8080/doc.html
- 管理后台 API：http://localhost:8081/doc.html

---

## 📡 API 接口概览

### C端接口 (小程序)

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录、登出 |
| 首页 | `/api/home` | 首页数据、轮播图 |
| 线路 | `/api/route` | 线路列表、详情、套餐 |
| 订单 | `/api/order` | 下单、订单管理、退款 |
| 支付 | `/api/pay` | 支付、回调 |
| 用户 | `/api/user` | 用户信息 |
| 出行人 | `/api/traveler` | 出行人管理 |
| 定制 | `/api/custom` | 定制需求 |
| 会员 | `/api/member` | 会员信息、购买 |
| 优惠券 | `/api/coupon` | 优惠券管理 |
| 推广 | `/api/promoter` | 推广员中心 |
| 消息 | `/api/message` | 消息通知 |

### B端接口 (管理后台)

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/admin/auth` | 管理员登录 |
| 线路 | `/admin/route` | 线路管理 |
| 订单 | `/admin/order` | 订单管理 |
| 退款 | `/admin/refund` | 退款审核 |
| 用户 | `/admin/user` | 用户管理 |
| 内容 | `/admin/content` | 轮播图、分类 |
| 系统 | `/admin/system` | 系统配置 |

---

## 📊 业务流程

### 跟团游下单流程

```
浏览线路 → 选择套餐 → 选择日期 → 选择人数 
    → 填写出行人 → 选择优惠券 → 确认订单 
    → 微信支付 → 商家确认 → 待出行 → 完成
```

### 订单状态流转

```
待支付 → 待确认 → 已确认 → 已完成
   ↓        ↓        ↓
已取消   已拒绝   退款中 → 已退款
```

### 定制游流程

```
选择目的地 → 选择时间 → 选择人数 → 选择预算 
    → 填写需求 → 提交 → 客服跟进 → 方案确认
```

---

## 🗄️ 数据库设计

### 核心表

| 域 | 表名 | 说明 |
|---|------|------|
| 用户 | `user_info` | 用户表 |
| 用户 | `user_traveler` | 出行人表 |
| 商品 | `product_main` | 商品主表 |
| 商品 | `product_sku` | SKU表 |
| 商品 | `product_price_stock` | 价格库存表 |
| 订单 | `order_main` | 订单主表 |
| 订单 | `order_traveler` | 订单出行人 |
| 订单 | `order_refund` | 退款表 |
| 支付 | `payment_record` | 支付记录 |
| 会员 | `member_order` | 会员订单 |
| 推广 | `promotion_user` | 推广员 |
| 推广 | `coupon_template` | 优惠券模板 |
| 推广 | `coupon_user` | 用户优惠券 |
| 定制 | `custom_demand` | 定制需求 |
| 消息 | `message_record` | 消息记录 |
| 内容 | `content_banner` | 轮播图 |

---

## 📝 开发规范

### 代码规范

- 使用 Lombok 注解简化代码
- 实体类继承 `BaseEntity`（包含 id, createdAt, updatedAt）
- 分页查询使用 `PageQuery`（包含 page, pageSize）
- 统一返回 `Result<T>`
- 异常使用 `BizException`

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 表名 | 模块前缀_业务名 | `user_info`, `order_main` |
| 字段名 | 小写下划线 | `user_id`, `created_at` |
| 主键 | id (BIGINT) | 使用雪花算法 |
| 金额 | DECIMAL(10,2) | 单位：元 |
| 状态 | status / is_xxx | `status`, `is_deleted` |

### Git 提交规范

```
feat: 新功能
fix: 修复Bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

---

## 📋 开发进度

### ✅ 已完成

- [x] 项目架构搭建
- [x] 用户登录模块
- [x] 首页数据接口
- [x] 线路列表/详情
- [x] 套餐/价格日历
- [x] 订单模块基础
- [x] 出行人管理
- [x] 定制游提交
- [x] 会员模块
- [x] 优惠券模块
- [x] 推广员模块

### ⏳ 进行中

- [ ] 微信支付集成
- [ ] 消息通知服务
- [ ] 订单状态流转完善

### 📅 待开发

- [ ] 管理后台 API
- [ ] 微信订阅消息
- [ ] 定时任务（出行提醒）
- [ ] 数据统计

---

## 📚 相关文档

| 文档 | 路径 |
|------|------|
| 技术方案 | `../技术方案/寻迹千行技术方案_V1.md` |
| 详细设计 | `../技术方案/寻迹千行详细设计_V1.md` |
| 需求方案 | `../需求文档/寻迹千行需求方案_V4.md` |
| 功能清单 | `../需求文档/寻迹千行功能清单.md` |
| 页面交互 | `../需求文档/寻迹千行页面交互文档_V4.md` |

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 发起 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

<p align="center">
  Made with ❤️ by 寻迹千行团队
</p>

<p align="center">
  <i>最后更新: 2026-01-24</i>
</p>
