package com.loan.common.util;

import java.util.UUID;

/**
 * ID生成工具
 */
public class IdGenerator {

    /**
     * 生成业务流水号
     */
    public static String generateBizFlowNo(String prefix) {
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * 生成贷款号
     */
    public static String generateLoanNo() {
        return "LN" + System.currentTimeMillis();
    }

    /**
     * 生成合同号
     */
    public static String generateContractNo() {
        return "CT" + System.currentTimeMillis();
    }

    /**
     * 生成任务号
     */
    public static String generateTaskNo() {
        return "TK" + System.currentTimeMillis();
    }
}
