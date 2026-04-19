package com.loan.common.constant;

/**
 * RocketMQ Topic常量
 */
public class RocketMQTopic {

    public static final String LOAN_CREDIT = "LOAN_CREDIT";
    public static final String LOAN_LOAN = "LOAN_LOAN";
    public static final String LOAN_QUOTA = "LOAN_QUOTA";
    public static final String LOAN_ACCOUNT = "LOAN_ACCOUNT";
    public static final String LOAN_PAY = "LOAN_PAY";
    public static final String LOAN_EVENT = "LOAN_EVENT";

    /**
     * Tag
     */
    public static class Tag {
        public static final String APPLY = "apply";
        public static final String APPROVE = "approve";
        public static final String REJECT = "reject";
        public static final String CHANGE = "change";
        public static final String QUERY = "query";
        public static final String CALLBACK = "callback";
    }
}
