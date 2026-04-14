package com.loan.loan.service;

import com.loan.common.constant.RocketMQTopic;
import com.loan.common.dto.LoanApplyDTO;
import com.loan.common.entity.LoanCore;
import com.loan.common.entity.LoanTransaction;
import com.loan.common.enums.LoanStatus;
import com.loan.common.enums.TransactionType;
import com.loan.common.mq.LoanApplyMessage;
import com.loan.loan.mapper.LoanCoreMapper;
import com.loan.loan.mapper.LoanTransactionMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用信审批服务 - 处理支用、还款等
 */
@Service
public class LoanService {

    @Autowired
    private LoanCoreMapper loanCoreMapper;

    @Autowired
    private LoanTransactionMapper transactionMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 处理用信申请（支用申请）
     */
    @Transactional
    public void processLoanApply(LoanApplyMessage message) {
        LoanApplyDTO dto = message.getLoanApplyDTO();

        // 1. 查询贷款核心记录
        LoanCore loanCore = loanCoreMapper.selectById(dto.getLoanId());
        if (loanCore == null) {
            throw new RuntimeException("贷款记录不存在");
        }

        // 2. 检查贷款状态
        if (!LoanStatus.CREDIT_APPROVED.name().equals(loanCore.getLoanStatus())
                && !LoanStatus.ACTIVE.name().equals(loanCore.getLoanStatus())) {
            throw new RuntimeException("当前状态不允许支用");
        }

        // 3. 检查可用额度
        if (loanCore.getAvailableQuota().compareTo(dto.getApplyAmount()) < 0) {
            throw new RuntimeException("可用额度不足");
        }

        // 4. 记录交易流水
        saveTransaction(loanCore.getId(), TransactionType.LOAN_APPLY, dto.getApplyAmount(),
                LoanStatus.LOAN_PENDING.name(), dto.getBizFlowNo(), "PENDING", null);

        // 5. 发送账务创建消息
        sendAccountCreateMessage(loanCore, dto);

        // 6. 发送支付请求消息
        sendPayRequestMessage(loanCore, dto);
    }

    /**
     * 处理放款确认
     */
    @Transactional
    public void confirmLoan(Long loanId, BigDecimal amount, String bizFlowNo) {
        LoanCore loanCore = loanCoreMapper.selectById(loanId);
        if (loanCore == null) {
            throw new RuntimeException("贷款记录不存在");
        }

        // 1. 更新额度使用
        loanCore.setUsedQuota(loanCore.getUsedQuota().add(amount));
        loanCore.setAvailableQuota(loanCore.getAvailableQuota().subtract(amount));
        loanCore.setCurrentBalance(loanCore.getCurrentBalance().add(amount));
        loanCore.setLoanStatus(LoanStatus.ACTIVE.name());
        loanCoreMapper.updateById(loanCore);

        // 2. 记录交易流水
        saveTransaction(loanId, TransactionType.LOAN_APPROVE, amount,
                LoanStatus.ACTIVE.name(), bizFlowNo, "AUTO_PASS", "放款成功");
    }

    /**
     * 处理还款
     */
    @Transactional
    public void processRepay(Map<String, Object> request) {
        String bizFlowNo = (String) request.get("bizFlowNo");
        Long loanId = getLong(request.get("loanId"));
        BigDecimal repayAmount = getBigDecimal(request.get("repayAmount"));

        LoanCore loanCore = loanCoreMapper.selectById(loanId);
        if (loanCore == null) {
            throw new RuntimeException("贷款记录不存在");
        }

        // 1. 释放额度
        loanCore.setUsedQuota(loanCore.getUsedQuota().subtract(repayAmount));
        loanCore.setAvailableQuota(loanCore.getAvailableQuota().add(repayAmount));
        loanCore.setCurrentBalance(loanCore.getCurrentBalance().subtract(repayAmount));

        // 2. 检查是否已还清
        if (loanCore.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loanCore.setCurrentBalance(BigDecimal.ZERO);
            loanCore.setLoanStatus(LoanStatus.CLEARED.name());
        }
        loanCoreMapper.updateById(loanCore);

        // 3. 发送账务更新消息
        sendAccountUpdateMessage(loanCore, repayAmount, bizFlowNo);

        // 4. 记录交易流水
        saveTransaction(loanId, TransactionType.REPAY, repayAmount,
                loanCore.getLoanStatus(), bizFlowNo, "PASS", "还款成功");
    }

    /**
     * 发送账务创建消息
     */
    private void sendAccountCreateMessage(LoanCore loanCore, LoanApplyDTO dto) {
        try {
            Map<String, Object> accountMessage = Map.of(
                    "loanId", loanCore.getId(),
                    "loanNo", loanCore.getLoanNo(),
                    "customerId", loanCore.getCustomerId(),
                    "productType", loanCore.getProductType(),
                    "amount", dto.getApplyAmount(),
                    "term", dto.getApplyTerm() != null ? dto.getApplyTerm() : loanCore.getTerm(),
                    "interestRate", loanCore.getInterestRate(),
                    "bizFlowNo", dto.getBizFlowNo()
            );
            rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_ACCOUNT + ":" + RocketMQTopic.Tag.APPLY, accountMessage);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 发送支付请求消息
     */
    private void sendPayRequestMessage(LoanCore loanCore, LoanApplyDTO dto) {
        try {
            Map<String, Object> payMessage = Map.of(
                    "loanId", loanCore.getId(),
                    "loanNo", loanCore.getLoanNo(),
                    "customerId", loanCore.getCustomerId(),
                    "amount", dto.getApplyAmount(),
                    "bizFlowNo", dto.getBizFlowNo()
            );
            rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_PAY + ":" + RocketMQTopic.Tag.APPLY, payMessage);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 发送账务更新消息
     */
    private void sendAccountUpdateMessage(LoanCore loanCore, BigDecimal repayAmount, String bizFlowNo) {
        try {
            Map<String, Object> accountMessage = Map.of(
                    "loanId", loanCore.getId(),
                    "repayAmount", repayAmount,
                    "bizFlowNo", bizFlowNo
            );
            rocketMQTemplate.convertAndSend(RocketMQTopic.LOAN_ACCOUNT + ":" + "repay", accountMessage);
        } catch (Exception e) {
            // ignore
        }
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
        transaction.setBeforeStatus(transStatus);
        transaction.setAfterStatus(transStatus);
        transactionMapper.insert(transaction);
    }

    private Long getLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String && !((String) value).isEmpty()) {
            return Long.parseLong((String) value);
        }
        return null;
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof String && !((String) value).isEmpty()) {
            return new BigDecimal((String) value);
        }
        return null;
    }
}
