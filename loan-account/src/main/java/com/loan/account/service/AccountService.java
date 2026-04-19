package com.loan.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.loan.account.entity.LoanAccount;
import com.loan.account.entity.RepaymentPlan;
import com.loan.account.mapper.LoanAccountMapper;
import com.loan.account.mapper.RepaymentPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 账务服务
 */
@Service
public class AccountService {

    @Autowired
    private LoanAccountMapper accountMapper;

    @Autowired
    private RepaymentPlanMapper planMapper;

    /**
     * 创建账务（收到放款消息后）
     */
    @Transactional
    public void createAccount(Map<String, Object> message) {
        Long loanId = getLong(message.get("loanId"));
        String loanNo = (String) message.get("loanNo");
        Long customerId = getLong(message.get("customerId"));
        String productType = (String) message.get("productType");
        BigDecimal amount = getBigDecimal(message.get("amount"));
        BigDecimal interestRate = getBigDecimal(message.get("interestRate"));
        Integer term = getInteger(message.get("term"));
        String bizFlowNo = (String) message.get("bizFlowNo");

        // 计算利息
        BigDecimal totalInterest = calculateInterest(amount, interestRate, term, productType);
        BigDecimal totalAmount = amount.add(totalInterest);

        // 创建账务记录
        LoanAccount account = new LoanAccount();
        account.setLoanId(loanId);
        account.setLoanNo(loanNo);
        account.setCustomerId(customerId);
        account.setProductType(productType);
        account.setLoanAmount(amount);
        account.setInterestRate(interestRate);
        account.setTotalInterest(totalInterest);
        account.setTotalAmount(totalAmount);
        account.setRepaidPrincipal(BigDecimal.ZERO);
        account.setRepaidInterest(BigDecimal.ZERO);
        account.setRepaidAmount(BigDecimal.ZERO);
        account.setLoanDate(LocalDate.now());
        account.setExpireDate(LocalDate.now().plusMonths(term));
        account.setTerm(term);
        account.setRepaymentType("EQUAL_PRINCIPAL_INTEREST");
        account.setStatus("ACTIVE");
        accountMapper.insert(account);

        // 生成还款计划
        generateRepaymentPlan(account, productType);
    }

    /**
     * 处理还款
     */
    @Transactional
    public void processRepay(Map<String, Object> message) {
        Long loanId = getLong(message.get("loanId"));
        BigDecimal repayAmount = getBigDecimal(message.get("repayAmount"));

        LoanAccount account = accountMapper.selectOne(
                new LambdaQueryWrapper<LoanAccount>().eq(LoanAccount::getLoanId, loanId)
        );
        if (account == null) {
            return;
        }

        // 更新账户
        account.setRepaidAmount(account.getRepaidAmount().add(repayAmount));

        // 检查是否已还清
        if (account.getRepaidAmount().compareTo(account.getTotalAmount()) >= 0) {
            account.setStatus("CLEARED");
            account.setRepaidAmount(account.getTotalAmount());
        }
        accountMapper.updateById(account);

        // 更新还款计划
        List<RepaymentPlan> plans = planMapper.selectList(
                new LambdaQueryWrapper<RepaymentPlan>()
                        .eq(RepaymentPlan::getAccountId, account.getId())
                        .orderByAsc(RepaymentPlan::getPeriod)
        );

        BigDecimal remaining = repayAmount;
        for (RepaymentPlan plan : plans) {
            if ("CLEARED".equals(plan.getStatus())) continue;

            BigDecimal needToRepay = plan.getAmount().subtract(plan.getRepaidAmount());
            if (needToRepay.compareTo(remaining) <= 0) {
                // 还清本期
                plan.setRepaidAmount(plan.getAmount());
                plan.setRepaidPrincipal(plan.getPrincipal());
                plan.setRepaidInterest(plan.getInterest());
                plan.setRepaidDate(LocalDate.now());
                plan.setStatus("CLEARED");
                remaining = remaining.subtract(needToRepay);
            } else {
                // 部分还款
                plan.setRepaidAmount(plan.getRepaidAmount().add(remaining));
                if (remaining.compareTo(plan.getInterest()) >= 0) {
                    plan.setRepaidInterest(plan.getInterest());
                    plan.setRepaidPrincipal(remaining.subtract(plan.getInterest()));
                } else {
                    plan.setRepaidInterest(remaining);
                }
                remaining = BigDecimal.ZERO;
            }
            planMapper.updateById(plan);

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
        }
    }

    /**
     * 生成还款计划
     */
    private void generateRepaymentPlan(LoanAccount account, String productType) {
        List<RepaymentPlan> plans = new ArrayList<>();

        if ("REVOLVING_STYLE".equals(account.getRepaymentType())) {
            // 随借随还：只生成一期
            RepaymentPlan plan = createPlan(account, 1, account.getLoanAmount(), account.getTotalInterest());
            plans.add(plan);
        } else if ("EQUAL_PRINCIPAL_INTEREST".equals(account.getRepaymentType())) {
            // 等额本息
            BigDecimal monthlyPayment = calculateMonthlyPayment(
                    account.getLoanAmount(), account.getInterestRate(), account.getTerm());
            BigDecimal totalPrincipal = account.getLoanAmount();
            BigDecimal totalInterest = account.getTotalInterest();

            for (int i = 1; i <= account.getTerm(); i++) {
                BigDecimal interest = totalInterest.multiply(
                        BigDecimal.valueOf(account.getTerm() - i + 1).divide(
                                BigDecimal.valueOf(account.getTerm()), 10, RoundingMode.HALF_UP));
                BigDecimal principal = monthlyPayment.subtract(interest);
                if (i == account.getTerm()) {
                    principal = totalPrincipal;
                }
                totalPrincipal = totalPrincipal.subtract(principal);

                RepaymentPlan plan = createPlan(account, i, principal, interest);
                plans.add(plan);
            }
        } else {
            // 一次性还本付息
            RepaymentPlan plan = createPlan(account, 1, account.getLoanAmount(), account.getTotalInterest());
            plans.add(plan);
        }

        // 第一期设为CURRENT状态
        if (!plans.isEmpty()) {
            plans.get(0).setStatus("CURRENT");
            planMapper.insert(plans.get(0));
            for (int i = 1; i < plans.size(); i++) {
                plans.get(i).setStatus("FUTURE");
                planMapper.insert(plans.get(i));
            }
        }
    }

    private RepaymentPlan createPlan(LoanAccount account, int period, BigDecimal principal, BigDecimal interest) {
        RepaymentPlan plan = new RepaymentPlan();
        plan.setAccountId(account.getId());
        plan.setLoanId(account.getLoanId());
        plan.setPeriod(period);
        plan.setPrincipal(principal);
        plan.setInterest(interest);
        plan.setAmount(principal.add(interest));
        plan.setRepaidPrincipal(BigDecimal.ZERO);
        plan.setRepaidInterest(BigDecimal.ZERO);
        plan.setRepaidAmount(BigDecimal.ZERO);
        plan.setDueDate(LocalDate.now().plusMonths(period));
        plan.setStatus("FUTURE");
        return plan;
    }

    /**
     * 计算利息
     */
    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal monthlyRate, Integer term, String productType) {
        if ("REVOLVING_STYLE".equals(productType)) {
            // 随借随还：按实际使用天数计算
            return principal.multiply(monthlyRate).multiply(BigDecimal.valueOf(term)).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 等额本息/一次性：利息 = 本金 * 月利率 * 期数
            return principal.multiply(monthlyRate).multiply(BigDecimal.valueOf(term)).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 计算等额本息月还款额
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, Integer term) {
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(term, MathContext.DECIMAL128);
        return principal.multiply(monthlyRate).multiply(pow)
                .divide(pow.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
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
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof String && !((String) value).isEmpty()) {
            return new BigDecimal((String) value);
        }
        return BigDecimal.ZERO;
    }

    private Integer getInteger(Object value) {
        if (value == null) return 1;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String && !((String) value).isEmpty()) {
            return Integer.parseInt((String) value);
        }
        return 1;
    }
}
