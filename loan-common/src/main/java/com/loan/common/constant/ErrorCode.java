package com.loan.common.constant;

/**
 * 错误码常量
 */
public class ErrorCode {

    public static final String SUCCESS = "0000";
    public static final String SYSTEM_ERROR = "9999";
    public static final String PARAM_ERROR = "1001";
    public static final String BIZ_FLOW_NO_EXIST = "1002";
    public static final String DUPLICATE_REQUEST = "1003";

    public static final String CUSTOMER_NOT_FOUND = "2001";
    public static final String PRODUCT_NOT_FOUND = "2002";
    public static final String LOAN_NOT_FOUND = "2003";

    public static final String QUOTA_NOT_ENOUGH = "3001";
    public static final String QUOTA_FROZEN = "3002";
    public static final String LOAN_STATUS_ERROR = "3003";

    public static final String APPROVAL_TASK_NOT_FOUND = "4001";
    public static final String APPROVAL_ALREADY_DONE = "4002";

    public static final String CHANNEL_AUTH_FAILED = "5001";
    public static final String CHANNEL_SIGN_INVALID = "5002";
}
