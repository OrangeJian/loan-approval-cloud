# 贷款审批系统 - 分层架构

## 项目概述

基于 Spring Cloud + RocketMQ 的分层贷款审批系统，支持**循环贷**和**一次性贷款**两种产品类型。

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        渠道层 (Gateway)                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │   客户APP   │  │   合作方A   │  │   合作方B   │      │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘      │
└─────────┼─────────────────┼─────────────────┼──────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│              loan-gateway (8080) - 渠道接入服务              │
│  • 协议转换、签名验签、频率控制、路由分发                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               loan-facade (8081) - 前置服务                 │
│  • 业务编排、事件发布（发送RocketMQ消息）                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
               ┌───────────┴───────────┐
               │    RocketMQ          │
               │  (事务消息/事件)      │
               └───────────┬───────────┘
                           │
    ┌──────────────────────┼──────────────────────┐
    │                      │                      │
    ▼                      ▼                      ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│loan-credit  │    │  loan-loan  │    │ loan-account│
│  (8082)     │    │   (8083)    │    │   (8086)    │
│  授信审批   │    │  用信审批   │    │   账务中心  │
└─────────────┘    └─────────────┘    └─────────────┘
         │                │                │
         │                │                │
         ▼                ▼                ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ loan-quota  │    │  loan-pay   │    │ loan-query  │
│   (8085)    │    │   (8087)    │    │   (8090)    │
│   额度中心  │    │   支付中心  │    │  统一查询   │
└─────────────┘    └─────────────┘    └─────────────┘

中台底座：
• loan-customer (8088) - 客户中心
• loan-product (8089) - 产品中心
• loan-approve (8084) - 人工审批工作台
```

## 产品类型

### 循环贷 (REVOLVING)
- 共享额度，可循环使用
- 还款方式：随借随还
- 审批策略：自动风控评分 ≥ 650 分自动通过

### 一次性贷款 (TERM)
- 独立额度，一次性使用
- 还款方式：等额本息、等额本金、一次性还本付息
- 审批策略：自动风控评分 ≥ 650 分自动通过

## 技术栈

| 组件 | 技术 |
|------|------|
| 微服务框架 | Spring Cloud 2023.0 + Spring Boot 3.2 |
| 服务注册/配置 | Nacos |
| 消息中间件 | RocketMQ |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| ORM | MyBatis-Plus 3.5 |
| 前端 | Vue 3 + Element Plus |

## 项目结构

```
loan-approval-cloud/
├── loan-common/           # 公共模块（实体、DTO、枚举、工具类、消息）
├── loan-gateway/         # 渠道接入服务 (8080)
├── loan-facade/          # 前置服务 (8081)
├── loan-credit/          # 授信审批服务 (8082)
├── loan-loan/            # 用信审批服务 (8083)
├── loan-approve/         # 人工审批工作台 (8084)
├── loan-quota/           # 额度中心 (8085)
├── loan-account/         # 账务中心 (8086)
├── loan-pay/             # 支付中心 (8087)
├── loan-customer/        # 客户中心 (8088)
├── loan-product/         # 产品中心 (8089)
├── loan-query/           # 统一查询服务 (8090)
├── loan-admin/           # Vue前端
└── db/init.sql           # 数据库初始化脚本
```

## 快速开始

### 1. 环境准备
- JDK 17+
- Maven 3.8+
- Node.js 16+
- MySQL 8.0
- Redis
- RocketMQ
- Nacos

### 2. 初始化数据库
```bash
mysql -u root -p < db/init.sql
```

### 3. 启动中间件
确保 Nacos、RocketMQ、Redis 已启动。

### 4. 启动后端服务
按顺序启动各微服务：
```bash
# 基础服务
cd loan-common && mvn install

# 启动各微服务
cd loan-gateway && mvn spring-boot:run
cd loan-facade && mvn spring-boot:run
cd loan-credit && mvn spring-boot:run
cd loan-loan && mvn spring-boot:run
cd loan-quota && mvn spring-boot:run
cd loan-account && mvn spring-boot:run
cd loan-pay && mvn spring-boot:run
cd loan-customer && mvn spring-boot:run
cd loan-product && mvn spring-boot:run
cd loan-approve && mvn spring-boot:run
cd loan-query && mvn spring-boot:run
```

### 5. 启动前端
```bash
cd loan-admin
npm install
npm run dev
```

### 6. 访问系统
- 前端地址：http://localhost:3000
- Nacos控制台：http://localhost:8848

## 核心流程

### 授信申请流程
```
客户APP → 网关 → 前置服务 → RocketMQ → 授信服务
                                            ↓
                                      自动风控评分
                                            ↓
                    ┌───────────────────────┴───────────────────────┐
                    ↓                                               ↓
              分数≥650                                      分数<650
              自动通过                                        需要人工审批
                    ↓                                               ↓
              更新额度                                       人工审批工作台
                    ↓                                               ↓
              发送结果消息 ──────────────────────────────────→ 额度中心更新
```

### 用信申请流程（支用）
```
客户APP → 网关 → 前置服务 → RocketMQ → 用信服务
                                         ↓
                                   额度检查
                                   风控检查
                                         ↓
                              ┌────────┴────────┐
                              ↓                 ↓
                        额度充足            额度不足
                              ↓                 ↓
                        创建账务            拒绝
                              ↓
                        发送支付请求 → 支付中心
                              ↓
                        放款确认 → 更新额度
```

## RocketMQ Topic

| Topic | 说明 |
|-------|------|
| LOAN_CREDIT | 授信申请/审批结果 |
| LOAN_LOAN | 用信申请/还款 |
| LOAN_QUOTA | 额度变更 |
| LOAN_ACCOUNT | 账务创建/更新 |
| LOAN_PAY | 支付请求/回调 |
| LOAN_EVENT | 通用事件 |

## API 接口

### 渠道接入（通过网关）
```
POST /gateway/{channel}/credit/apply   # 授信申请
POST /gateway/{channel}/loan/apply     # 用信申请
POST /gateway/{channel}/repay          # 还款
POST /gateway/{channel}/query          # 查询
```

### 直接调用（内部）
```
产品中心: 8089
GET  /product/all                      # 获取所有产品
GET  /product/{id}                     # 获取产品详情

客户中心: 8088
GET  /customer/{id}                    # 获取客户详情

额度中心: 8085
GET  /quota/{loanId}                  # 获取额度信息
POST /quota/freeze                     # 冻结额度
POST /quota/use                        # 使用额度
POST /quota/release                    # 释放额度

账务中心: 8086
GET  /account/loan/{loanId}           # 获取账务信息
GET  /account/{id}/plans              # 获取还款计划

审批工作台: 8084
GET  /approve/tasks/pending           # 获取待审批任务
POST /approve/execute                   # 执行审批
```

## 数据库表

| 表名 | 说明 |
|------|------|
| loan_customer | 客户表 |
| loan_product | 产品表 |
| loan_core | 贷款核心表（统一贷款信息） |
| loan_quota_change | 额度变更记录 |
| loan_transaction | 交易流水 |
| approval_flow | 审批流程定义 |
| approval_task | 审批任务 |
| loan_account | 账务账户 |
| repayment_plan | 还款计划 |

## 默认数据

### 产品
- `REV_CREDIT` - 循环信用贷（5000-10万，利率0.6%-1.2%）
- `TERM_PERSONAL` - 个人消费贷（5000-50万，利率0.45%-1%）

### 客户
- C001 - 张三（低风险）
- C002 - 李四（中风险）
- C003 - 王五（高风险）
