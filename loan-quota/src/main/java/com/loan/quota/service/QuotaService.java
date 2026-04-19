package com.loan.quota.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.loan.common.dto.QuotaChangeDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.LoanCore;
import com.loan.common.entity.LoanQuotaChange;
import com.loan.common.enums.QuotaChangeType;
import com.loan.common.mq.QuotaChangeMessage;
import com.loan.quota.mapper.LoanCoreMapper;
import com.loan.quota.mapper.LoanQuotaChangeMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class QuotaService {

    @Autowired
    private LoanCoreMapper loanCoreMapper;

    @Autowired
    private LoanQuotaChangeMapper quotaChangeMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private static final String QUOTA_LOCK_PREFIX = "quota:lock:";

    public Result<LoanCore> getByLoanId(Long loanId) {
        LoanCore loanCore = loanCoreMapper.selectById(loanId);
        return Result.success(loanCore);
    }

    public Result<LoanCore> getByLoanNo(String loanNo) {
        LambdaQueryWrapper<LoanCore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoanCore::getLoanNo, loanNo);
        LoanCore loanCore = loanCoreMapper.selectOne(wrapper);
        return Result.success(loanCore);
    }

    /**
     * 授信创建额度
     */
    @Transactional
    public Result<LoanCore> createQuota(Long customerId, Long productId, String productType,
                                         BigDecimal totalQuota, BigDecimal interestRate,
                                         Integer term, String bizFlowNo) {
        LoanCore loanCore = new LoanCore();
        loanCore.setLoanNo("LN" + System.currentTimeMillis());
        loanCore.setCustomerId(customerId);
        loanCore.setProductId(productId);
        loanCore.setProductType(productType);
        loanCore.setLoanStatus("CREDIT_APPROVED");
        loanCore.setTotalQuota(totalQuota);
        loanCore.setUsedQuota(BigDecimal.ZERO);
        loanCore.setAvailableQuota(totalQuota);
        loanCore.setCurrentBalance(BigDecimal.ZERO);
        loanCore.setInterestRate(interestRate);
        loanCore.setTerm(term);
        loanCore.setBizFlowNo(bizFlowNo);
        loanCoreMapper.insert(loanCore);

        // 记录额度变更
        recordQuotaChange(loanCore.getId(), QuotaChangeType.GRANT, totalQuota,
                BigDecimal.ZERO, totalQuota, "loan-credit", bizFlowNo, "授信创建额度");

        // 发送额度变更消息
        sendQuotaChangeMessage(loanCore, QuotaChangeType.GRANT, totalQuota, BigDecimal.ZERO, totalQuota);

        return Result.success(loanCore);
    }

    /**
     * 额度冻结（用信申请时）
     */
    @Transactional
    public Result<Void> freezeQuota(Long loanId, BigDecimal amount, String triggerSource, String bizFlowNo) {
        String lockKey = QUOTA_LOCK_PREFIX + loanId;
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10)))) {
            return Result.error("额度操作繁忙，请稍后重试");
        }

        try {
            LoanCore loanCore = loanCoreMapper.selectById(loanId);
            if (loanCore == null) {
                return Result.error("贷款记录不存在");
            }

            if (loanCore.getAvailableQuota().compareTo(amount) < 0) {
                return Result.error("可用额度不足");
            }

            // 冻结额度：可用额度减少，已用额度不变
            loanCore.setAvailableQuota(loanCore.getAvailableQuota().subtract(amount));
            loanCoreMapper.updateById(loanCore);

            // 记录额度变更
            recordQuotaChange(loanId, QuotaChangeType.FREEZE, amount,
                    loanCore.getTotalQuota().subtract(loanCore.getUsedQuota()).add(amount),
                    loanCore.getAvailableQuota(), triggerSource, bizFlowNo, "额度冻结");

            return Result.success();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 额度使用（放款时）
     */
    @Transactional
    public Result<Void> useQuota(Long loanId, BigDecimal amount, String triggerSource, String bizFlowNo) {
        String lockKey = QUOTA_LOCK_PREFIX + loanId;
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10)))) {
            return Result.error("额度操作繁忙，请稍后重试");
        }

        try {
            LoanCore loanCore = loanCoreMapper.selectById(loanId);
            if (loanCore == null) {
                return Result.error("贷款记录不存在");
            }

            // 使用额度：已用额度增加，可用额度不变（因为已经冻结）
            int rows = loanCoreMapper.decreaseAvailableQuota(loanId, amount);
            if (rows == 0) {
                return Result.error("额度不足");
            }

            // 更新余额
            loanCore.setCurrentBalance(loanCore.getCurrentBalance().add(amount));
            loanCoreMapper.updateById(loanCore);

            // 记录额度变更
            BigDecimal beforeUsed = loanCore.getUsedQuota();
            BigDecimal afterUsed = beforeUsed.add(amount);
            recordQuotaChange(loanId, QuotaChangeType.USE, amount,
                    beforeUsed, afterUsed, triggerSource, bizFlowNo, "额度使用");

            // 发送消息
            sendQuotaChangeMessage(loanCore, QuotaChangeType.USE, amount, beforeUsed, afterUsed);

            return Result.success();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 额度释放（还款时）
     */
    @Transactional
    public Result<Void> releaseQuota(Long loanId, BigDecimal amount, String triggerSource, String bizFlowNo) {
        String lockKey = QUOTA_LOCK_PREFIX + loanId;
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10)))) {
            return Result.error("额度操作繁忙，请稍后重试");
        }

        try {
            LoanCore loanCore = loanCoreMapper.selectById(loanId);
            if (loanCore == null) {
                return Result.error("贷款记录不存在");
            }

            // 释放额度：已用额度减少，可用额度增加
            int rows = loanCoreMapper.increaseAvailableQuota(loanId, amount);
            if (rows == 0) {
                return Result.error("额度释放失败");
            }

            // 更新余额
            loanCore.setCurrentBalance(loanCore.getCurrentBalance().subtract(amount));
            loanCoreMapper.updateById(loanCore);

            // 记录额度变更
            BigDecimal beforeUsed = loanCore.getUsedQuota();
            BigDecimal afterUsed = beforeUsed.subtract(amount);
            recordQuotaChange(loanId, QuotaChangeType.RELEASE, amount,
                    beforeUsed, afterUsed, triggerSource, bizFlowNo, "额度释放");

            // 发送消息
            sendQuotaChangeMessage(loanCore, QuotaChangeType.RELEASE, amount, beforeUsed, afterUsed);

            return Result.success();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 额度调减
     */
    @Transactional
    public Result<Void> reduceQuota(Long loanId, BigDecimal newTotalQuota, String triggerSource, String bizFlowNo) {
        LoanCore loanCore = loanCoreMapper.selectById(loanId);
        if (loanCore == null) {
            return Result.error("贷款记录不存在");
        }

        BigDecimal oldTotal = loanCore.getTotalQuota();
        if (newTotalQuota.compareTo(loanCore.getUsedQuota()) < 0) {
            return Result.error("总额度不能小于已用额度");
        }

        loanCoreMapper.updateTotalQuota(loanId, newTotalQuota);

        // 记录额度变更
        recordQuotaChange(loanId, QuotaChangeType.REDUCE, oldTotal.subtract(newTotalQuota),
                oldTotal, newTotalQuota, triggerSource, bizFlowNo, "额度调减");

        return Result.success();
    }

    private void recordQuotaChange(Long loanId, QuotaChangeType changeType, BigDecimal changeAmount,
                                    BigDecimal beforeQuota, BigDecimal afterQuota,
                                    String triggerSource, String bizFlowNo, String remark) {
        LoanQuotaChange change = new LoanQuotaChange();
        change.setLoanId(loanId);
        change.setChangeType(changeType.name());
        change.setChangeAmount(changeAmount);
        change.setBeforeQuota(beforeQuota);
        change.setAfterQuota(afterQuota);
        change.setTriggerSource(triggerSource);
        change.setBizFlowNo(bizFlowNo);
        change.setRemark(remark);
        quotaChangeMapper.insert(change);
    }

    private void sendQuotaChangeMessage(LoanCore loanCore, QuotaChangeType changeType,
                                         BigDecimal changeAmount, BigDecimal beforeQuota, BigDecimal afterQuota) {
        try {
            QuotaChangeMessage message = new QuotaChangeMessage();
            message.setLoanId(loanCore.getId());
            message.setBizFlowNo(loanCore.getBizFlowNo());
            message.setProductType(loanCore.getProductType());
            message.setChangeType(changeType.name());
            message.setChangeAmount(changeAmount);
            message.setBeforeQuota(beforeQuota);
            message.setAfterQuota(afterQuota);
            rocketMQTemplate.convertAndSend("LOAN_QUOTA:change", message);
        } catch (Exception e) {
            // 消息发送失败不影响主流程
        }
    }
}
