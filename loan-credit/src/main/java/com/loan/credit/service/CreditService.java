package com.loan.credit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.loan.common.constant.RocketMQTopic;
import com.loan.common.dto.CreditApplyDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.LoanCore;
import com.loan.common.entity.LoanTransaction;
import com.loan.common.enums.LoanStatus;
import com.loan.common.enums.TransactionType;
import com.loan.common.mq.CreditApplyMessage;
import com.loan.common.mq.CreditResultMessage;
import com.loan.credit.mapper.LoanCoreMapper;
import com.loan.credit.mapper.LoanTransactionMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 授信审批服务
 */
@Service
public class CreditService {

    @Autowired
    private LoanCoreMapper loanCoreMapper;

    @Autowired
    private LoanTransactionMapper transactionMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 处理授信申请
     */
    @Transactional
    public void processCreditApply(CreditApplyMessage message) {
        CreditApplyDTO dto = message.getCreditApplyDTO();

        // 1. 创建贷款核心记录（待审批状态）
        LoanCore loanCore = new LoanCore();
        loanCore.setLoanNo("LN" + System.currentTimeMillis());
        loanCore.setCustomerId(dto.getCustomerId());
        loanCore.setProductId(dto.getProductId());
        loanCore.setProductType(dto.getApplyAmount() != null ? "TERM" : "REVOLVING");
        loanCore.setLoanStatus(LoanStatus.CREDIT_PENDING.name());
        loanCore.setTotalQuota(BigDecimal.ZERO);
        loanCore.setUsedQuota(BigDecimal.ZERO);
        loanCore.setAvailableQuota(BigDecimal.ZERO);
        loanCore.setCurrentBalance(BigDecimal.ZERO);
        loanCore.setTerm(dto.getApplyTerm());
        loanCore.setBizFlowNo(dto.getBizFlowNo());
        loanCoreMapper.insert(loanCore);

        // 2. 记录交易流水
        saveTransaction(loanCore.getId(), TransactionType.CREDIT_APPLY, dto.getApplyAmount(),
                LoanStatus.CREDIT_PENDING.name(), dto.getBizFlowNo(), "PENDING", null);

        // 3. 自动风控评分（简化版）
        int riskScore = calculateRiskScore(dto);

        // 4. 判断是否需要人工审批
        boolean needManualApproval = riskScore < 650 || dto.getApplyAmount().compareTo(new BigDecimal("50000")) > 0;

        if (!needManualApproval) {
            // 自动通过，直接授信
            approveCredit(loanCore, dto, riskScore);
        } else {
            // 需要人工审批，发布任务（简化处理）
            // 实际应该发布到人工审批服务，这里简化处理
            publishCreditTask(loanCore, dto, riskScore);
        }
    }

    /**
     * 自动审批通过
     */
    @Transactional
    public void approveCredit(LoanCore loanCore, CreditApplyDTO dto, int riskScore) {
        // 1. 计算额度
        BigDecimal approvedQuota = calculateQuota(dto.getApplyAmount(), dto.getApplyTerm(), riskScore);

        // 2. 计算利率
        BigDecimal interestRate = calculateInterestRate(riskScore, dto.getApplyTerm());

        // 3. 更新贷款状态
        loanCore.setTotalQuota(approvedQuota);
        loanCore.setAvailableQuota(approvedQuota);
        loanCore.setInterestRate(interestRate);
        loanCore.setLoanStatus(LoanStatus.CREDIT_APPROVED.name());
        loanCoreMapper.updateById(loanCore);

        // 4. 记录交易流水
        saveTransaction(loanCore.getId(), TransactionType.CREDIT_APPROVE, approvedQuota,
                LoanStatus.CREDIT_APPROVED.name(), dto.getBizFlowNo(), "AUTO_PASS", "自动审批通过");

        // 5. 发送授信结果消息
        sendCreditResultMessage(loanCore, "APPROVED", approvedQuota, interestRate, "自动审批通过");
    }

    /**
     * 发布人工审批任务（简化版）
     */
    private void publishCreditTask(LoanCore loanCore, CreditApplyDTO dto, int riskScore) {
        // 实际应该发送到人工审批服务loan-approve
        // 这里简化处理为：记录需要人工审批的任务
        saveTransaction(loanCore.getId(), TransactionType.CREDIT_APPLY, dto.getApplyAmount(),
                LoanStatus.CREDIT_PENDING.name(), dto.getBizFlowNo(), "PENDING", "等待人工审批");
    }

    /**
     * 发送授信结果消息
     */
    private void sendCreditResultMessage(LoanCore loanCore, String approvalResult,
                                         BigDecimal approvedQuota, BigDecimal interestRate, String comment) {
        try {
            CreditResultMessage resultMessage = new CreditResultMessage();
            resultMessage.setLoanId(loanCore.getId());
            resultMessage.setLoanNo(loanCore.getLoanNo());
            resultMessage.setApprovalResult(approvalResult);
            resultMessage.setApprovedQuota(approvedQuota);
            resultMessage.setInterestRate(interestRate);
            resultMessage.setComment(comment);
            resultMessage.setBizFlowNo(loanCore.getBizFlowNo());

            rocketMQTemplate.convertAndSend(
                    RocketMQTopic.LOAN_CREDIT + ":" + RocketMQTopic.Tag.APPROVE,
                    resultMessage
            );
        } catch (Exception e) {
            // 消息发送失败不影响主流程
        }
    }

    /**
     * 计算风险评分（简化版）
     */
    private int calculateRiskScore(CreditApplyDTO dto) {
        // 简化评分逻辑，实际应该调用风控服务
        // 这里假设分数在600-750之间
        return 600 + (int) (Math.random() * 150);
    }

    /**
     * 计算审批额度
     */
    private BigDecimal calculateQuota(BigDecimal applyAmount, Integer applyTerm, int riskScore) {
        // 简化额度计算逻辑
        // 额度 = 申请金额 * 风险系数
        BigDecimal riskFactor = new BigDecimal(riskScore).divide(new BigDecimal("800"), 4, RoundingMode.HALF_UP);
        BigDecimal approvedQuota = applyAmount.multiply(riskFactor);

        // 上限为申请金额的120%
        BigDecimal maxQuota = applyAmount.multiply(new BigDecimal("1.2"));
        if (approvedQuota.compareTo(maxQuota) > 0) {
            approvedQuota = maxQuota;
        }

        // 最低额度5000
        if (approvedQuota.compareTo(new BigDecimal("5000")) < 0) {
            approvedQuota = new BigDecimal("5000");
        }

        return approvedQuota.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算利率
     */
    private BigDecimal calculateInterestRate(int riskScore, Integer applyTerm) {
        // 简化利率计算
        // 基础利率 + 风险调整
        BigDecimal baseRate = new BigDecimal("0.008"); // 月利率0.8%
        if (riskScore < 650) {
            baseRate = baseRate.add(new BigDecimal("0.003")); // 高风险加3厘
        } else if (riskScore >= 700) {
            baseRate = baseRate.subtract(new BigDecimal("0.001")); // 优质客户减1厘
        }
        return baseRate;
    }

    /**
     * 保存交易流水
     */
    private void saveTransaction(Long loanId, TransactionType transType, BigDecimal transAmount,
                                String transStatus, String bizFlowNo, String approvalResult, String comment) {
        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoanId(loanId);
        transaction.setTransType(transType.name());
        transaction.setTransAmount(transAmount);
        transaction.setTransStatus(transStatus);
        transaction.setBizFlowNo(bizFlowNo);
        transaction.setApprovalResult(approvalResult);
        transaction.setApprovalComment(comment);
        transaction.setBeforeStatus(LoanStatus.CREDIT_PENDING.name());
        transaction.setAfterStatus(transStatus);
        transactionMapper.insert(transaction);
    }
}
