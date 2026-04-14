-- =========================================
-- 贷款审批系统 - 分层架构数据库初始化脚本
-- =========================================

CREATE DATABASE IF NOT EXISTS loan_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE loan_db;

-- =========================================
-- 客户表
-- =========================================
DROP TABLE IF EXISTS loan_customer;
CREATE TABLE loan_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_no VARCHAR(50) NOT NULL UNIQUE COMMENT '客户号',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    risk_level VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '风险等级：HIGH/MEDIUM/LOW',
    status VARCHAR(20) DEFAULT '1' COMMENT '状态：1启用，0禁用',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_no (customer_no),
    INDEX idx_phone (phone),
    INDEX idx_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- =========================================
-- 产品表
-- =========================================
DROP TABLE IF EXISTS loan_product;
CREATE TABLE loan_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE COMMENT '产品代码',
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    product_type VARCHAR(20) NOT NULL COMMENT '产品类型：REVOLVING-循环贷，TERM-一次性贷款',
    min_amount DECIMAL(15,2) NOT NULL COMMENT '最低金额',
    max_amount DECIMAL(15,2) NOT NULL COMMENT '最高金额',
    min_term INT NOT NULL COMMENT '最低期限（月）',
    max_term INT NOT NULL COMMENT '最高期限（月）',
    min_interest_rate DECIMAL(10,6) NOT NULL COMMENT '最低月利率',
    max_interest_rate DECIMAL(10,6) NOT NULL COMMENT '最高月利率',
    amount_strategy JSON COMMENT '额度策略配置',
    interest_strategy JSON COMMENT '利率策略配置',
    approval_strategy JSON COMMENT '审批策略配置',
    repayment_type VARCHAR(50) COMMENT '还款方式',
    quota_model VARCHAR(50) COMMENT '额度模型',
    status VARCHAR(20) DEFAULT '1' COMMENT '状态：1启用，0禁用',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_code (product_code),
    INDEX idx_product_type (product_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- =========================================
-- 贷款核心表（统一贷款信息）
-- =========================================
DROP TABLE IF EXISTS loan_core;
CREATE TABLE loan_core (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '贷款号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_type VARCHAR(20) NOT NULL COMMENT '产品类型：REVOLVING/TERM',
    loan_status VARCHAR(30) NOT NULL DEFAULT 'CREDIT_PENDING' COMMENT '贷款状态',
    total_quota DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '总授信额度',
    used_quota DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已用额度',
    available_quota DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '可用额度',
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '当前余额',
    interest_rate DECIMAL(10,6) COMMENT '当前利率（月利率）',
    term INT COMMENT '期限（月）',
    start_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '到期日期',
    biz_flow_no VARCHAR(50) COMMENT '关联业务流水号',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_no (loan_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_product_id (product_id),
    INDEX idx_loan_status (loan_status),
    INDEX idx_biz_flow_no (biz_flow_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贷款核心表';

-- =========================================
-- 额度变更记录表
-- =========================================
DROP TABLE IF EXISTS loan_quota_change;
CREATE TABLE loan_quota_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL COMMENT '关联贷款ID',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型：GRANT/REDUCE/FREEZE/UNFREEZE/USE/RELEASE/ADJUST',
    change_amount DECIMAL(15,2) NOT NULL COMMENT '变更金额',
    before_quota DECIMAL(15,2) COMMENT '变更前额度',
    after_quota DECIMAL(15,2) COMMENT '变更后额度',
    trigger_source VARCHAR(50) COMMENT '触发来源服务',
    biz_flow_no VARCHAR(50) COMMENT '业务流水号',
    remark VARCHAR(200) COMMENT '备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id),
    INDEX idx_change_type (change_type),
    INDEX idx_biz_flow_no (biz_flow_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='额度变更记录表';

-- =========================================
-- 交易流水表
-- =========================================
DROP TABLE IF EXISTS loan_transaction;
CREATE TABLE loan_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL COMMENT '关联贷款ID',
    trans_type VARCHAR(30) NOT NULL COMMENT '交易类型',
    trans_amount DECIMAL(15,2) COMMENT '交易金额',
    trans_status VARCHAR(30) COMMENT '交易状态',
    biz_flow_no VARCHAR(50) COMMENT '业务流水号',
    request_source VARCHAR(50) COMMENT '请求来源',
    approval_result VARCHAR(20) COMMENT '审批结果',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    approver_id BIGINT COMMENT '审批人ID',
    related_event_id VARCHAR(100) COMMENT '关联RocketMQ消息ID',
    before_status VARCHAR(30) COMMENT '变更前状态',
    after_status VARCHAR(30) COMMENT '变更后状态',
    ext_data JSON COMMENT '扩展信息',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id),
    INDEX idx_trans_type (trans_type),
    INDEX idx_biz_flow_no (biz_flow_no),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易流水表';

-- =========================================
-- 审批流程定义表
-- =========================================
DROP TABLE IF EXISTS approval_flow;
CREATE TABLE approval_flow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_code VARCHAR(50) NOT NULL UNIQUE COMMENT '流程代码',
    flow_name VARCHAR(100) NOT NULL COMMENT '流程名称',
    product_type VARCHAR(20) COMMENT '适用产品类型',
    stages JSON NOT NULL COMMENT '阶段定义JSON',
    status VARCHAR(20) DEFAULT '1' COMMENT '状态',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_flow_code (flow_code),
    INDEX idx_product_type (product_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程定义表';

-- =========================================
-- 审批任务表
-- =========================================
DROP TABLE IF EXISTS approval_task;
CREATE TABLE approval_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_no VARCHAR(50) NOT NULL UNIQUE COMMENT '任务编号',
    loan_id BIGINT NOT NULL COMMENT '关联贷款ID',
    flow_code VARCHAR(50) COMMENT '流程代码',
    current_stage VARCHAR(30) NOT NULL COMMENT '当前阶段',
    task_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/CLAIMED/COMPLETED/CANCELLED',
    assignee_id BIGINT COMMENT '处理人ID',
    assignee_name VARCHAR(100) COMMENT '处理人姓名',
    claim_time DATETIME COMMENT '领取时间',
    complete_time DATETIME COMMENT '完成时间',
    approval_result VARCHAR(20) COMMENT '审批结果：PASS/REJECT',
    comment VARCHAR(500) COMMENT '审批意见',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_no (task_no),
    INDEX idx_loan_id (loan_id),
    INDEX idx_task_status (task_status),
    INDEX idx_assignee_id (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- =========================================
-- 账务账户表
-- =========================================
DROP TABLE IF EXISTS loan_account;
CREATE TABLE loan_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL COMMENT '关联贷款ID',
    loan_no VARCHAR(50) NOT NULL COMMENT '贷款号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    product_type VARCHAR(20) NOT NULL COMMENT '产品类型',
    loan_amount DECIMAL(15,2) NOT NULL COMMENT '借款金额',
    interest_rate DECIMAL(10,6) NOT NULL COMMENT '月利率',
    total_interest DECIMAL(15,2) NOT NULL COMMENT '总利息',
    total_amount DECIMAL(15,2) NOT NULL COMMENT '总金额',
    repaid_principal DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已还本金',
    repaid_interest DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已还利息',
    repaid_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已还金额',
    loan_date DATE NOT NULL COMMENT '放款日期',
    expire_date DATE NOT NULL COMMENT '到期日期',
    term INT NOT NULL COMMENT '期限',
    repayment_type VARCHAR(50) NOT NULL COMMENT '还款方式',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账户状态：ACTIVE/CLEARED/OVERDUE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账务账户表';

-- =========================================
-- 还款计划表
-- =========================================
DROP TABLE IF EXISTS repayment_plan;
CREATE TABLE repayment_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL COMMENT '账户ID',
    loan_id BIGINT NOT NULL COMMENT '贷款ID',
    period INT NOT NULL COMMENT '期数',
    principal DECIMAL(15,2) NOT NULL COMMENT '本金',
    interest DECIMAL(15,2) NOT NULL COMMENT '利息',
    amount DECIMAL(15,2) NOT NULL COMMENT '还款金额',
    repaid_principal DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已还本金',
    repaid_interest DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已还利息',
    repaid_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '已还金额',
    due_date DATE NOT NULL COMMENT '应还日期',
    repaid_date DATE COMMENT '实际还款日期',
    status VARCHAR(20) NOT NULL DEFAULT 'FUTURE' COMMENT '状态：FUTURE/CURRENT/OVERDUE/CLEARED',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_account_id (account_id),
    INDEX idx_loan_id (loan_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款计划表';

-- =========================================
-- 插入默认产品数据
-- =========================================
INSERT INTO loan_product (product_code, product_name, product_type, min_amount, max_amount, min_term, max_term, min_interest_rate, max_interest_rate, amount_strategy, interest_strategy, approval_strategy, repayment_type, quota_model, status)
VALUES
-- 循环贷产品
('REV_CREDIT', '循环信用贷', 'REVOLVING', 5000.00, 100000.00, 1, 36, 0.006000, 0.012000,
 '{"minQuota": 5000, "maxQuota": 100000, "quotaModel": "SCORE_MODEL"}',
 '{"baseRate": 0.008, "riskAdjust": true}',
 '{"autoApproveScore": 700, "needManualAmount": 50000}',
 'REVOLVING_STYLE', 'CREDIT_MODEL', 1),

-- 一次性贷款产品
('TERM_PERSONAL', '个人消费贷', 'TERM', 5000.00, 500000.00, 3, 60, 0.004500, 0.010000,
 '{"minQuota": 5000, "maxQuota": 500000, "quotaModel": "INCOME_MODEL"}',
 '{"baseRate": 0.006, "termAdjust": true, "amountAdjust": true}',
 '{"autoApproveScore": 650, "needManualAmount": 300000}',
 'EQUAL_PRINCIPAL_INTEREST', 'PERSONAL_MODEL', 1);

-- =========================================
-- 插入测试客户数据
-- =========================================
INSERT INTO loan_customer (customer_no, name, id_card, phone, email, risk_level, status)
VALUES
('C001', '张三', '110101199001011234', '13800138000', 'zhangsan@example.com', 'LOW', '1'),
('C002', '李四', '110101199002021234', '13900139000', 'lisi@example.com', 'MEDIUM', '1'),
('C003', '王五', '110101199003031234', '13700137000', 'wangwu@example.com', 'HIGH', '1');

-- =========================================
-- 插入审批流程定义
-- =========================================
INSERT INTO approval_flow (flow_code, flow_name, product_type, stages, status)
VALUES
('CREDIT_APPLY', '授信申请流程', 'REVOLVING',
 '[{"stage": "AUTO_RISK", "type": "AUTO", "passRule": "SCORE>=700"}, {"stage": "MANUAL_RISK", "type": "MANUAL", "approverRole": "RISK_MANAGER"}, {"stage": "MANUAL_AMOUNT", "type": "MANUAL", "approverRole": "AMOUNT_MANAGER"}]',
 '1'),
('CREDIT_APPLY_TERM', '授信申请流程', 'TERM',
 '[{"stage": "AUTO_RISK", "type": "AUTO", "passRule": "SCORE>=650"}, {"stage": "MANUAL_RISK", "type": "MANUAL", "approverRole": "RISK_MANAGER"}, {"stage": "MANUAL_AMOUNT", "type": "MANUAL", "approverRole": "AMOUNT_MANAGER"}]',
 '1');
