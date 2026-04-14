package com.loan.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审批流程定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval_flow")
public class ApprovalFlow extends BaseEntity {

    private String flowCode;          // 流程代码
    private String flowName;          // 流程名称
    private String productType;       // 适用产品类型
    private String stages;            // 阶段定义JSON
    private String status;            // 状态
}
