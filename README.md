# 校园二手书交易系统

基于 Spring Boot + Vue 3 的校园二手书交易平台，支持用户注册登录、书籍发布与搜索、购物车、订单管理、钱包支付及后台管理等功能。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 4.0.6, Maven |
| 前端 | Vue 3, Vite 6, Pinia, Vue Router 4, Element Plus |
| 数据库 | MySQL 8, utf8mb4 |
| 安全 | JWT, BCrypt + Pepper, RSA + AES 加密通信, HTTPS |

---

## 项目结构

```
Shopping/
├── backend/Shopping/         # 后端 Spring Boot 项目
│   └── src/main/java/com/example/shopping/
│       ├── controller/       # REST 控制器
│       ├── service/          # 业务逻辑层
│       ├── dao/              # 数据访问层 (JDBC)
│       ├── entity/           # 数据库实体 POJO
│       ├── dto/              # 数据传输对象
│       ├── config/           # 配置 (CORS、拦截器、静态资源)
│       ├── filter/           # JWT 过滤、加密过滤、闲置超时
│       ├── interceptor/      # 管理员权限拦截
│       ├── utils/            # 工具类 (JWT、密码、加解密、DB)
│       └── scheduler/        # 定时任务 (闲置监控、订单超时)
├── frontend/                 # 前端 Vue 3 项目
│   └── src/
│       ├── api/              # API 接口封装
│       ├── views/            # 页面组件
│       ├── components/       # 通用组件
│       ├── layouts/          # 布局组件
│       ├── stores/           # Pinia 状态管理
│       ├── router/           # 路由配置
│       └── utils/            # 前端工具 (加解密、请求)
├── db/
│   ├── init.sql              # 数据库初始化 (建库建表+默认数据)
│   └── runtime_data.sql      # 运行时数据 (测试用书籍/订单)
├── key/
│   └── pepper.key            # Pepper 密钥文件
├── uploads/images/           # 书籍图片上传目录
├── setup_db.bat              # 数据库初始化脚本
├── setup_test_db.bat         # 测试数据库初始化脚本
├── export_data.sql           # 运行时数据导出脚本
└── 使用须知.txt              # 使用注意事项
```

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8+

### 1. 初始化数据库

```bash
# 方式一：运行脚本（需输入 MySQL root 密码）
setup_db.bat

# 方式二：手动导入
mysql -u root -p < db/init.sql
mysql -u root -p < db/runtime_data.sql
```

### 2. 启动后端

```bash
cd backend/Shopping
mvn spring-boot:run
```

后端启动于 `https://localhost:8443`（HTTPS，自签名证书）。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器启动于 `http://localhost:3000`，自动代理 API 请求到后端。

### 4. 访问系统

打开浏览器访问 `http://localhost:3000`。

---

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| testuser | user123 | 普通用户 |

测试环境还预置了 test1 ~ test5 五个测试账号，密码均为 `123456`。

---

## 功能模块

### 用户端

- **注册登录** — 用户注册、登录、JWT 认证、闲置自动退出
- **书籍浏览** — 书籍列表、全文搜索、高级搜索（价格/分类）、推荐
- **书籍详情** — 图文详情、卖家信息
- **购物车** — 添加/删除/修改数量/清空
- **下单支付** — 创建订单、钱包余额支付、支付密码验证
- **订单管理** — 查看订单、卖家发货、确认收货、取消订单
- **发布书籍** — 填写书籍信息、上传图片
- **我的书籍** — 管理已发布书籍、下架
- **钱包** — 查看余额、设置/修改支付密码、交易流水

### 管理端

- **用户管理** — 查看用户列表、封禁/解封、锁定/解锁钱包
- **书籍管理** — 查看所有书籍、下架、永久删除
- **订单管理** — 查看所有订单、订单详情
- **资金管理** — 充值、交易流水、数据统计

---

## API 概览

| 路径 | 说明 |
|------|------|
| `POST /shopping/register` | 用户注册 |
| `POST /shopping/login` | 用户登录 |
| `GET /api/book/list` | 书籍列表 |
| `GET /api/book/detail/{id}` | 书籍详情 |
| `POST /api/book/search` | 搜索书籍 |
| `GET/POST /api/cart/**` | 购物车接口 |
| `POST /api/order/create` | 创建订单 |
| `POST /api/order/pay` | 支付订单 |
| `GET /api/order/my` | 我的订单 |
| `GET/POST /api/wallet/**` | 钱包接口 |
| `GET/POST /api/admin/**` | 管理员接口 |

完整接口请参阅后端 Controller 代码。

---

## 安全设计

- 通信加密：前端与后端通过 RSA 交换 AES 密钥，敏感字段经 AES-256-GCM 加密传输
- 密码存储：BCrypt + Pepper 双重哈希，明文不可逆
- 身份认证：JWT Token（24h 有效期），存放于 HttpOnly Cookie
- 权限控制：普通用户与管理员路由分离，后端接口有拦截器校验
- 闲置超时：5 分钟无操作自动退出
- 支付安全：支付密码独立于登录密码，错误多次锁定

---

## 数据库

数据库 `shopping_db` 包含以下核心表：

| 表名 | 说明 |
|------|------|
| `user` | 用户表（普通用户/管理员） |
| `wallet` | 钱包表（余额、支付密码、锁定状态） |
| `book` | 书籍表（含软删除标记） |
| `book_image` | 书籍图片表 |
| `cart` | 购物车表 |
| `order` | 订单表（状态：未支付/已支付/已发货/已完成/已取消） |
| `order_item` | 订单明细表 |
| `frozen_amount` | 冻结资金表（付款后冻结，收货后解冻） |
| `transaction_log` | 资金流水表 |

---

## 开发说明

- 后端配置文件：`backend/Shopping/src/main/resources/application.properties`
- 前端代理配置：`frontend/vite.config.js`
- 数据库连接信息硬编码于配置文件中，生产环境请改用环境变量或配置中心
- 自签名 SSL 证书仅用于开发，生产环境请替换为受信任证书
