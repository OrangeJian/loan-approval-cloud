package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("loan_customer")
public class Customer extends BaseEntity {

    private String customerNo;       // 客户号
    private String name;            // 姓名
    private String idCard;          // 身份证号
    private String phone;           // 手机号
    private String email;           // 邮箱
    private String riskLevel;       // 风险等级
    private String status;          // 状态
}
