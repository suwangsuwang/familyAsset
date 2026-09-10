# FamilyAsset - 家庭资产管理系统

一个基于 Spring Boot 的家庭资产管理系统，支持多资产类型（股票、基金、债券等）的买入、卖出、持仓管理和投资收益分析。

## 技术栈

- **Java 17**
- **Spring Boot 4.0.6**
- **MyBatis** - ORM 框架
- **MySQL 8.0** - 数据库
- **Redis 7** - 缓存（持仓列表）
- **RabbitMQ 3** - 消息队列（异步收益记录）
- **JWT (jjwt 0.12.5)** - 用户认证
- **Lombok** - 简化代码
- **Docker Compose** - 中间件容器化部署

## 项目结构

```
src/main/java/org/swan/familyasset/
├── Controller/        # 接口层
│   ├── UserController.java
│   ├── AssetController.java
│   ├── TransactionController.java
│   └── PositionController.java
├── Service/           # 业务逻辑层
│   ├── UserService.java
│   ├── AssetService.java
│   ├── TransactionService.java
│   ├── PositionService.java
│   └── PortfolioService.java
├── Mapper/            # 数据访问层
│   ├── UserMapper.java
│   ├── AssetMapper.java
│   ├── TransactionMapper.java
│   ├── PositionMapper.java
│   └── ProfitRecordMapper.java
├── Entity/            # 实体类
├── VO/                # 视图对象
├── config/            # 配置类
├── Consumer/          # RabbitMQ 消费者
├── Utils/             # 工具类
├── AuthInterceptor.java
├── WebConfig.java
└── UserContext.java
```

## 快速启动

### 前置条件

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. 启动中间件

```bash
docker compose up -d
```

启动以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3308 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672/15672 | 消息队列 |

### 2. 创建数据库表

连接 MySQL 执行以下 SQL：

```sql
CREATE DATABASE IF NOT EXISTS family_asset;
USE family_asset;

-- 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- 资产表
CREATE TABLE asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    INDEX idx_user_id (user_id)
);

-- 交易记录表
CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL COMMENT 'BUY/SELL',
    price DECIMAL(18,4) NOT NULL,
    quantity DECIMAL(18,4) NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_asset_id (asset_id)
);

-- 持仓表
CREATE TABLE position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    quantity DECIMAL(18,4) NOT NULL,
    avg_cost DECIMAL(18,4) NOT NULL,
    UNIQUE INDEX idx_user_asset (user_id, asset_id)
);

-- 收益记录表
CREATE TABLE profit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    quantity DECIMAL(18,4) NOT NULL,
    cost DECIMAL(18,4) NOT NULL,
    revenue DECIMAL(18,4) NOT NULL,
    profit DECIMAL(18,4) NOT NULL,
    INDEX idx_user_id (user_id)
);
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

## API 接口文档

### 认证说明

除注册和登录外，所有接口需要在请求头中携带 JWT Token：

```
Authorization: Bearer <token>
```

---

### 用户模块

#### 注册

```
POST /register
```

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名（唯一） |
| password | String | 是 | 密码 |

示例：

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser", "password":"123456"}'
```

响应：`"注册成功"`

#### 登录

```
POST /login
```

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

示例：

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser", "password":"123456"}'
```

响应：

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.xxxxx"
}
```

---

### 资产模块

#### 添加资产

```
POST /asset/add
```

请求头：`Authorization: Bearer <token>`

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| symbol | String | 是 | 资产代码（如 AAPL） |
| name | String | 是 | 资产名称（如 苹果） |

示例：

```bash
curl -X POST http://localhost:8080/asset/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"symbol":"AAPL", "name":"苹果"}'
```

响应：`"添加成功"`

#### 资产列表

```
GET /asset/list
```

请求头：`Authorization: Bearer <token>`

示例：

```bash
curl http://localhost:8080/asset/list \
  -H "Authorization: Bearer <token>"
```

响应：

```json
[
  {"id": 1, "userId": 1, "symbol": "AAPL", "name": "苹果"}
]
```

---

### 交易模块

#### 买入

```
POST /transaction/buy
```

请求头：`Authorization: Bearer <token>`

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| assetId | Long | 是 | 资产 ID |
| price | BigDecimal | 是 | 买入价格 |
| quantity | BigDecimal | 是 | 买入数量 |

示例：

```bash
curl -X POST http://localhost:8080/transaction/buy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"assetId":1, "price":150.5, "quantity":10}'
```

响应：`"买入成功"`

#### 卖出

```
POST /transaction/sell
```

请求头：`Authorization: Bearer <token>`

请求体：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| assetId | Long | 是 | 资产 ID |
| price | BigDecimal | 是 | 卖出价格 |
| quantity | BigDecimal | 是 | 卖出数量 |

示例：

```bash
curl -X POST http://localhost:8080/transaction/sell \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"assetId":1, "price":160, "quantity":5}'
```

响应：`"卖出成功"`

---

### 持仓模块

#### 持仓列表

```
GET /position/list
```

请求头：`Authorization: Bearer <token>`

> 说明：持仓列表有 Redis 缓存（10分钟过期），买入/卖出操作后自动清除缓存。

示例：

```bash
curl http://localhost:8080/position/list \
  -H "Authorization: Bearer <token>"
```

响应：

```json
[
  {
    "assetName": "苹果",
    "quantity": 5,
    "avgCost": 150.5,
    "marketPrice": 500,
    "profit": 1747.5
  }
]
```

---

### 组合模块

#### 投资汇总

```
GET /transaction/portfolio/summary
```

请求头：`Authorization: Bearer <token>`

示例：

```bash
curl http://localhost:8080/transaction/portfolio/summary \
  -H "Authorization: Bearer <token>"
```

响应：

```json
{
  "totalInvest": 1505.0,
  "totalValue": 2500.0,
  "profit": 995.0
}
```

## 架构说明

- **认证拦截器**：`/register` 和 `/login` 无需 Token，其他接口均需 JWT 认证
- **用户上下文**：通过 `UserContext`（ThreadLocal）在请求链路中传递当前用户 ID
- **Redis 缓存**：持仓列表查询结果缓存 10 分钟，写操作自动失效
- **异步收益**：卖出成功后通过 RabbitMQ 发送事件，`ProfitConsumer` 异步记录收益

## License

MIT
