# 调用链分析：`GET /position/list`

> 本文档由 Claude Code 生成，作为 Day 01 的输入材料。
> 用途：带去网页里做第二层分析（知识体系讲解），不是最终结论。
>
> **证据等级说明**：静态代码部分（类名、行号、SQL）已在仓库中逐一核对。
> 涉及框架内部行为的步骤标记为「按框架约定，未实测」，需要跑起来验证。

---

## 为什么选这条链

项目里可选的路径有 `GET /asset/list`、`POST /transaction/buy`、`GET /position/list` 等。

选 `GET /position/list` 的理由：

1. 它是**唯一一条真正的「读」路径**，会穿过 Controller → Service → Mapper → MySQL 全部四层
2. 它包含 **Redis 缓存分支**——缓存命中和未命中是两条不同的路径，适合理解「层层委托」
3. 它**没有 `@Transactional`**，可以和 `buy()` 对比，看清事务在哪一层
4. 它的返回值是 `PositionVO` 而不是 Entity，可以看清 DTO/VO 的存在意义

⚠️ 重要：**这条链不是一条直线，有一个分叉。** 这是它的价值，也是它的坑。

---

## 一、入口

```http
GET /position/list HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxx
```

---

## 二、完整链路

### 阶段 1：Tomcat（内嵌）

| 步骤 | 位置 | 说明 |
|------|------|------|
| 1.1 | `FamilyAssetApplication.java:14` | `SpringApplication.run()`。内嵌 Tomcat 随 `spring-boot-starter-web` 启动，监听 8080 |
| 1.2 | — | Tomcat 的 `Acceptor` 接受 TCP 连接，把 socket 交给 `Poller` |
| 1.3 | — | `Poller` 把可读事件派给 **Worker 线程池中的某一个线程**，该线程此后独占处理这个请求直到返回 |

> 按框架约定，未实测。这里对应今天的问题 3。

### 阶段 2：Filter 链

| 步骤 | 位置 | 说明 |
|------|------|------|
| 2.1 | — | 本项目**没有自定义 Filter**（全局搜索无 `implements Filter`） |
| 2.2 | — | 自动注册的只有 Spring Boot 的 `OrderedCharacterEncodingFilter`（字符编码） |

### 阶段 3：DispatcherServlet

| 步骤 | 位置 | 说明 |
|------|------|------|
| 3.1 | — | 所有请求进入 **同一个** `DispatcherServlet` 实例（`spring-boot-starter-web` 自动配置） |

> 关键：整个应用只有**一个** DispatcherServlet，它被所有请求共享。
> 区分不同请求的是**线程**，不是 servlet 实例。

### 阶段 4：HandlerMapping

| 步骤 | 位置 | 说明 |
|------|------|------|
| 4.1 | — | `RequestMappingHandlerMapping` 查表：`GET /position/list` → 哪个方法 |
| 4.2 | `Controller/PositionController.java:14` | 类上 `@RequestMapping("/position")` |
| 4.3 | `Controller/PositionController.java:20` | 方法上 `@GetMapping("/list")` |
| 4.4 | — | 拼出映射 `GET /position/list` → `PositionController#list` |
| 4.5 | `WebConfig.java:18-20` | 查出该路径匹配到的拦截器：`AuthInterceptor`（`/**`，排除 `/login`、`/register`） |
| 4.6 | — | 组装成 `HandlerExecutionChain` = [拦截器们] + [目标方法] |

> 这张映射表在**应用启动时**就建好了，不是每次请求现算。

### 阶段 5：拦截器 preHandle

`AuthInterceptor.preHandle()`（[AuthInterceptor.java:15](src/main/java/org/swan/familyasset/AuthInterceptor.java#L15)）

| 行号 | 动作 |
|------|------|
| :17 | 读 `Authorization` 请求头 |
| :19 | 判断 `token == null \|\| !token.startsWith("Bearer")` → 401「未登录」，返回 false，**链条在此终止** |
| :26 | `token.substring(7)` 裁掉 `Bearer ` |
| — | `JwtUtil.parseToken(token)`（[JwtUtil.java:20-28](src/main/java/org/swan/familyasset/Utils/JwtUtil.java#L20-L28)）验证签名 + 取出 subject |
| :27 | `Long userId = ...` 解析出用户 id |
| :28 | `request.setAttribute("userId", userId)` 放进 request 作用域 |
| :29 | **`UserContext.setUserId(userId)`** — 写进 `ThreadLocal` |
| :30 | 返回 true，继续 |

### 阶段 6：参数绑定

| 步骤 | 说明 |
|------|------|
| 6.1 | `PositionController#list()` **没有参数** → 无需绑定 |
| 6.2 | （对比：`POST /transaction/buy` 有 `@RequestBody Transaction`，会走 `RequestResponseBodyMethodProcessor` + Jackson 反序列化） |

### 阶段 7：Controller

[PositionController.java:21-23](src/main/java/org/swan/familyasset/Controller/PositionController.java#L21-L23)

```java
@GetMapping("/list")
public List<PositionVO> list() {
    return positionService.list();   // 只有一行，纯委托
}
```

### 阶段 8：Service —— **这里有分叉**

[PositionService.java:30](src/main/java/org/swan/familyasset/Service/PositionService.java#L30)

| 行号 | 动作 |
|------|------|
| :32 | `UserContext.getUserId()` — 从 ThreadLocal 取用户 id |
| :34 | 拼缓存 key：`"position:list:" + userId` |
| :36 | `redisTemplate.opsForValue().get(key)` → **Redis GET** |

```text
        ┌──────────────┐
        │  Redis GET   │
        └──────┬───────┘
               │
      ┌────────┴────────┐
      │                 │
   命中 :38          未命中
      │                 │
 :39 return        :41 查 MySQL
  (链条结束)             │
                   :45-67 组装 VO
                        │
                   :69 写回 Redis
                        │
                   :70 return
```

> **缓存命中时，MySQL 从头到尾没被碰过。** 同一条 HTTP 请求，两条完全不同的执行路径。
> 这是今天最值得盯住的地方。

#### 8a. 缓存未命中路径

| 行号 | 动作 |
|------|------|
| :41 | `positionMapper.findByUserId(userId)` |
| — | → `PositionMapper.findByUserId`（[PositionMapper.java:33-37](src/main/java/org/swan/familyasset/Mapper/PositionMapper.java#L33-L37)）|
| — | SQL：``SELECT * FROM position WHERE user_id = #{userId}`` |
| — | MyBatis `Executor` → `PreparedStatement` → JDBC |
| — | 连到 `jdbc:mysql://localhost:3308/family_asset`（[application.properties:4](src/main/resources/application.properties#L4)） |
| — | 返回 `List<Position>`，**一条或多条记录，按 `position` 表主键序** |
| :43 | 新建空 `ArrayList<PositionVO> result` |
| :45 | `for (Position p : positions)` 开始循环 |
| :47 | **`assetMapper.findById(p.getAssetId())`** → `AssetMapper.findById`（[AssetMapper.java:20-21](src/main/java/org/swan/familyasset/Mapper/AssetMapper.java#L20-L21)）|
| — | SQL：`SELECT * FROM asset WHERE id = #{id}` |
| :50 | `marketPrice = new BigDecimal("500")` — 写死的模拟价格 |
| :52 | `profit = (marketPrice - avgCost) * quantity` |
| :54-64 | 组装 `PositionVO`，塞进 `result` |
| :69 | `redisTemplate.opsForValue().set(key, result, 10, TimeUnit.MINUTES)` → **Redis SET + TTL 600s** |
| :70 | `return result` |

#### 8b. 这个循环里的往返次数

假设该用户有 **N** 个持仓：

```text
第 1 次查询：SELECT * FROM position WHERE user_id = ?     → 1 次
   循环 N 次：
       第 1 个持仓：SELECT * FROM asset WHERE id = ?      → 1 次
       第 2 个持仓：SELECT * FROM asset WHERE id = ?      → 1 次
       ...
       第 N 个持仓：SELECT * FROM asset WHERE id = ?      → 1 次
```

**总计 N + 1 次数据库往返。** N 越大，往返越多。

> 这个模式有名字，但先不写在这里——留给你在网页里分析时自己判断这算什么问题。

### 阶段 9：返回与序列化

| 步骤 | 位置 | 说明 |
|------|------|------|
| 9.1 | `PositionController.java:13` | `@RestController` = `@Controller` + `@ResponseBody` |
| 9.2 | — | 返回值不是 `ModelAndView` → 走 `RequestResponseBodyMethodProcessor` |
| 9.3 | — | `MappingJackson2HttpMessageConverter` 把 `List<PositionVO>` 转成 JSON |
| 9.4 | — | `PositionVO` 的 getter 由 Lombok `@Data`（[PositionVO.java:7](src/main/java/org/swan/familyasset/VO/PositionVO.java#L7)）生成，Jackson 靠 getter 发现字段 |

实际响应体（README 里记录的样例）：

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

### 阶段 10：拦截器 afterCompletion

| 行号 | 动作 |
|------|------|
| [AuthInterceptor.java:39-41](src/main/java/org/swan/familyasset/AuthInterceptor.java#L39-L41) | `afterCompletion()` → **`UserContext.clear()`**，即 `ThreadLocal.remove()` |

> `postHandle()` 本项目没有重写。

### 阶段 11：响应写回

Worker 线程把响应写进 socket → Tomcat 归还该线程到线程池 → 线程去处理下一个请求。

---

## 三、涉及的文件清单

| # | 文件 | 角色 |
|---|------|------|
| 1 | `FamilyAssetApplication.java` | 启动入口 |
| 2 | `WebConfig.java` | 注册拦截器 + 路径规则 |
| 3 | `AuthInterceptor.java` | 鉴权，写 ThreadLocal |
| 4 | `Utils/JwtUtil.java` | JWT 签发/校验 |
| 5 | `UserContext.java` | ThreadLocal 持有当前用户 id |
| 6 | `Controller/PositionController.java` | HTTP 入口 |
| 7 | `Service/PositionService.java` | 业务逻辑 + 缓存 |
| 8 | `Mapper/PositionMapper.java` | 持仓表 SQL |
| 9 | `Mapper/AssetMapper.java` | 资产表 SQL（循环内调用） |
| 10 | `config/RedisConfig.java` | 提供 `RedisTemplate` bean，指定序列化器 |
| 11 | `Entity/Position.java`、`Entity/Asset.java` | 数据库映射对象 |
| 12 | `VO/PositionVO.java` | 对外返回对象 |
| 13 | `src/main/resources/application.properties` | 数据库/Redis 地址 |

**没有出现的东西**：任何 `Filter`、任何 `@Transactional`、任何 `@Aspect`、`service/` 之外的第二层业务类。

---

## 四、观察到的疑点（**不是结论，是问题**）

> 以下每条我都只写「观察到了什么」，不写「应该怎么改」。
> 请带去网页里逐条分析。

### A. 关于「一个请求 = 一个线程」

我的理解是：从 Tomcat 接手到响应写完，整个链条（含 3 次 Redis/MySQL 往返）
是**同一个线程**从头走到尾。它不是「Tomcat 解析完就交给别人」。

但这只是我的理解，**没有实测**。问题 3 问的就是这个。

### B. 鉴权为什么放在 Interceptor，而不是 Filter

`AuthInterceptor` 是 MVC 拦截器，意味着它跑在 **DispatcherServlet 之后、HandlerMapping 之后**。

一个可观察的推论：请求 `GET /不存在的路径` 且不带 token 时，
HandlerMapping 找不到 handler → 直接 404，**拦截器根本不会被调用**。

如果换成 Filter，这个请求会先被 401 拦下。**同样的代码，不同的位置，行为不一样。**

### C. ThreadLocal 的清理时机

`UserContext.setUserId` 在 `preHandle` 里（:29），`clear()` 在 `afterCompletion` 里（:41）。

但 `preHandle` 返回 false 那条路径（:20-23，未登录）**没有写 ThreadLocal，也就无所谓清理**——
这个是对的。真正的问题是：

- `afterCompletion` 一定会被调用吗？
- 如果业务代码抛异常呢？
- Tomcat 的线程是**复用**的，如果某条路径漏了 `clear()`，下一个请求复用这个线程会读到什么？

### D. Redis 里存的到底是什么类型

`RedisConfig.java:17` 用的是 `RedisSerializer.json()`。

`PositionService.java:36` 的写法是：

```java
List<PositionVO> cache = (List<PositionVO>) redisTemplate.opsForValue().get(key);
```

这是**无检查的强制转换**——编译期只是一个警告，运行期因为泛型擦除不会报错。

那么：缓存命中时，`cache` 里的元素**真的是 `PositionVO` 实例吗？**
如果不是，为什么接口还能返回正确的 JSON？

### E. 循环里的 N+1 次查询

见 8b。N 个持仓 = N+1 次数据库往返。

另外循环体内每次都调 `assetMapper.findById()`，同一个 `assetId` 出现两次会查两次。

### F. `marketPrice` 是写死的

`PositionService.java:50` 和 `PortfolioService.java:53` 都是 `new BigDecimal("500")`。

两处独立写死，**说明「当前市价」这个概念在系统里还没有归属**。

（顺带：这也是路线图阶段④「AI Backend」存在的理由之一——真实行情数据从哪来、
谁来算、LLM 在中间扮演什么角色。先记着。）

### G. 这条链里完全没有事务

对比 `POST /transaction/buy`：那里有 `@Transactional`，而且
`TransactionService.buy()` 当前末尾有一句 `throw new RuntimeException("测试事务")`（工作区代码）。

为什么这条链**不需要**事务？什么情况下会需要？

### H. 缓存的写入与失效

- 写：只有缓存未命中路径才写（:69）
- 失效：`TransactionService.buy()` / `sell()` 里手动 `redisTemplate.delete("position:list:" + userId)`
- TTL：10 分钟

那么：**为什么这里需要手动 delete，而不是让 TTL 自然过期？**

### I. `PositionVO` 和 `Position` 为什么要分开

`Position` 是数据库映射对象（含 `id`、`userId`、`assetId`），
`PositionVO` 只有 5 个字段（含算出来的 `profit`、写死的 `marketPrice`，不含内部 id）。

为什么不直接把 `Position` 返回给客户端？

### J. 我还没看的部分

- Tomcat 线程池的默认大小是多少？1000 个并发请求会怎样？
- 三个并发请求进来，`position:list:{userId}` 这个 key 会发生什么？
- 如果 MySQL 卡 3 秒，同时有 1000 个请求，线程池会先耗尽还是 Redis 会先超时？

**这三条正好是今天问题 3 的内容。**

---

## 五、和我原答案的对照

（在网页分析完之后回来填，用于对照我原本的理解哪里偏了）

| 问题 | 我原来的理解 | 现在看到的 | 偏差 |
|------|-------------|-----------|------|
| 问题 1 | | | |
| 问题 2 | | | |
| 问题 3 | | | |
