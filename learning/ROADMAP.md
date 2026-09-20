# 12 个月 AI 工程师路线图

**起点**：2026-09-20
**终点**：2027-08（12 个月）

## 总目标

> 从 Android 开发者 → 能独立设计、开发、部署、维护 AI 后端系统的工程师。

不是「学会 Spring Boot」，也不是「学会调用 DeepSeek」，而是能自己完成这条链路：

```text
用户 → Web/App → Nginx → Spring Boot → 业务服务
     → MySQL / Redis / MQ → AI Service → LLM
     → RAG / Agent / Tool / MCP → Docker → Linux → 监控 / 日志 / 部署
```

## 主航道：FamilyAsset 2.0

`family_asset` 不是练习项目，是**贯穿 12 个月的主项目**。我们不「学完一个阶段再做项目」，
而是每个阶段都给这个项目加一层能力。

目标形态：**一个带 AI Agent 的家庭资产管理后端**。

```text
family-asset
├── auth            User / Login / Permission
├── asset           Asset / Position / Transaction / Profit
├── infrastructure  MySQL / Redis / RabbitMQ / Nginx
├── ai              Chat / RAG / Tool / Agent / Memory / Evaluation
├── monitoring
└── deployment
```

最终能直接问它：「我今年的投资收益怎么样？」——由 Agent 决定调哪些 Tool、查哪些数据、
算完再由 LLM 用自然语言回答。（涉及真实金融决策时，**计算、数据、模型意见严格分开**，
不让 LLM 自己编数字。）

## 六个阶段

| 阶段 | 时间 | 核心目标 |
|------|------|----------|
| ① 后端基础 | 2026-09 ~ 2026-10 | Java / Spring Boot / MySQL / Redis |
| ② 后端深入 | 2026-11 ~ 2026-12 | MQ / 事务 / 并发 / 缓存 / 分布式 |
| ③ 工程化 | 2027-01 ~ 2027-02 | Linux / Docker / Nginx / CI-CD / 监控 |
| ④ AI Backend | 2027-03 ~ 2027-04 | LLM / RAG / Tool / Agent |
| ⑤ AI System | 2027-05 ~ 2027-06 | MCP / Memory / Evaluation / Agent |
| ⑥ 开源实战 | 2027-07 ~ 2027-08 | GitHub 源码 + 完整项目 |

### ① 后端基础（第 1–2 月）

不重学 Java。你是 Android/Java 背景，重点补 **「Android 开发者缺失的后端思维」**：

- 第 1 月：HTTP → REST → Controller → Service → Repository → DTO → Entity → Transaction → Exception → Validation → Authentication → Authorization
- 第 2 月：Redis → Cache → RabbitMQ → 异步 → 事务 → 幂等 → 一致性

这一阶段结束时要能真正回答：

- MySQL 写成功了，RabbitMQ 失败怎么办？
- RabbitMQ 消费两次怎么办？
- Redis 和 MySQL 数据不一致怎么办？
- 用户重复点击怎么办？

### ② 后端深入（第 3–4 月）

并发 → 线程池 → 锁 → Redis → 分布式锁 → MQ → 事务 → 幂等 → 限流 → 熔断 → 最终一致性

实战实验：用户连点 10 次「买入 10000 元标普500」，系统会怎样？
流程是 **你先设计 → DeepSeek Code Review → GLM Architecture Review → 你改 → 写测试 → 故意制造并发 → 观察结果**。

### ③ 工程化（第 5–6 月）

Linux → Shell → Docker → Docker Compose → Nginx → HTTPS → CI/CD → 日志 → 监控。
目标是打通：

```text
git push → CI → build → test → Docker image → server → deploy
```

从「会写代码」变成「**能把系统跑起来的人**」。

### ④ AI Backend（第 7–8 月）

LLM API → Streaming → Structured Output → Function Calling → Tool Calling
→ Embedding → Vector Database → RAG

### ⑤ AI System（第 9–10 月）

Agent → Planning → Tool → Memory → Workflow → MCP → Evaluation

### ⑥ 开源实战（第 11–12 月）

后端：Spring Boot / Spring Framework / Redis / RabbitMQ
AI：Spring AI / Dify / LangChain-LangGraph 类项目 / MCP 相关项目

**每次只攻一个**，不「今天看这个明天看那个」。

## 源码学习方法（统一流程）

```text
① 项目结构 → ② 启动入口 → ③ 核心调用链 → ④ 核心数据结构
→ ⑤ 一个真实请求 → ⑥ Debug → ⑦ 修改 → ⑧ 测试
```

不背答案，自己一路追。

## 英语（内嵌，不单独占时间）

每天 20 分钟：5 分钟日常对话 + 10 分钟技术英语 + 5 分钟复述。

做法：当天学的技术主题，让 AI 用英语提问，你用英语回答，AI 纠正。
例如学 Spring MVC 当天，被问：

> What happens when an HTTP request reaches a Spring Boot application?

**后端学习 + 英语学习同时进行。**

## 模型分工

不固定「DeepSeek 永远负责 A，GLM 永远负责 B」。按角色分：

- **模型 A（DeepSeek）**：第一次解决问题
- **模型 B（GLM）**：挑错

```text
DeepSeek 出方案 → GLM 找问题 → 你修改 → DeepSeek 复审 → 你实现
```

比问「两个模型谁更聪明」有价值得多。

## 每月额度账本（2000 元）

| 类别 | 目标 |
|------|-----:|
| GitHub 源码 | 500 |
| 后端 | 400 |
| AI | 500 |
| FamilyAsset | 400 |
| 英语 | 100 |
| 探索 | 100 |
| **总计** | **2000** |

> **额度是预算，不是 KPI。** 某个月只花 1300 但完成了一个大项目，完全没问题。

## 进度

- [ ] ① 后端基础
- [ ] ② 后端深入
- [ ] ③ 工程化
- [ ] ④ AI Backend
- [ ] ⑤ AI System
- [ ] ⑥ 开源实战
