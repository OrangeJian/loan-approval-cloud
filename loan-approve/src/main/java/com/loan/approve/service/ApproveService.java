package com.loan.approve.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.constant.RocketMQTopic;
import com.loan.common.dto.ApprovalDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.ApprovalTask;
import com.loan.common.entity.LoanCore;
import com.loan.common.enums.LoanStatus;
import com.loan.common.mq.CreditResultMessage;
import com.loan.approve.mapper.ApprovalTaskMapper;
import com.loan.approve.mapper.LoanCoreMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人工审批服务
 */
@Service
public class ApproveService {

    @Autowired
    private ApprovalTaskMapper taskMapper;

    @Autowired
    private LoanCoreMapper loanCoreMapper;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 获取待审批任务列表
     */
    public Result<Page<ApprovalTask>> getPendingTasks(int pageNum, int pageSize, String stage) {
        Page<ApprovalTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApprovalTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTask::getTaskStatus, "PENDING");
        if (stage != null && !stage.isEmpty()) {
            wrapper.eq(ApprovalTask::getCurrentStage, stage);
        }
        wrapper.orderByAsc(ApprovalTask::getCreatedTime);
        Page<ApprovalTask> result = taskMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    /**
     * 领取任务
     */
    @Transactional
    public Result<Void> claimTask(String taskNo, Long approverId, String approverName) {
        ApprovalTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<ApprovalTask>()
                        .eq(ApprovalTask::getTaskNo, taskNo)
                        .eq(ApprovalTask::getTaskStatus, "PENDING")
        );
        if (task == null) {
            return Result.error("任务不存在或已被领取");
        }

        task.setAssigneeId(approverId);
        task.setAssigneeName(approverName);
        task.setClaimTime(LocalDateTime.now());
        task.setTaskStatus("CLAIMED");
        taskMapper.updateById(task);

        return Result.success();
    }

    /**
     * 执行审批
     */
    @Transactional
    public Result<Void> approve(ApprovalDTO dto) {
        ApprovalTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<ApprovalTask>()
                        .eq(ApprovalTask::getTaskNo, dto.getTaskNo())
        );
        if (task == null) {
            return Result.error("任务不存在");
        }

        if (!"CLAIMED".equals(task.getTaskStatus())) {
            return Result.error("任务未处于可审批状态");
        }

        // 更新任务
        task.setApprovalResult(dto.getApprovalResult());
        task.setComment(dto.getComment());
        task.setCompleteTime(LocalDateTime.now());
        task.setTaskStatus("COMPLETED");
        taskMapper.updateById(task);

        // 更新贷款状态
        LoanCore loanCore = loanCoreMapper.selectById(task.getLoanId());
        if (loanCore != null) {
            String newStatus;
            if ("PASS".equals(dto.getApprovalResult())) {
                if ("MANUAL_RISK".equals(task.getCurrentStage())) {
                    newStatus = LoanStatus.CREDIT_APPROVED.name();
                } else if ("MANUAL_AMOUNT".equals(task.getCurrentStage())) {
                    newStatus = LoanStatus.CREDIT_APPROVED.name();
                } else {
                    newStatus = LoanStatus.CREDIT_APPROVED.name();
                }

                // 发送审批通过消息
                sendCreditResultMessage(loanCore, "APPROVED", loanCore.getTotalQuota(), loanCore.getInterestRate(), dto.getComment());
            } else {
                newStatus = LoanStatus.CREDIT_REJECTED.name();
                sendCreditResultMessage(loanCore, "REJECTED", null, null, dto.getComment());
            }

            loanCore.setLoanStatus(newStatus);
            loanCoreMapper.updateById(loanCore);
        }

        return Result.success();
    }

    /**
     * 发送授信结果消息
     */
    private void sendCreditResultMessage(LoanCore loanCore, String approvalResult,
                                         java.math.BigDecimal approvedQuota,
                                         java.math.BigDecimal interestRate, String comment) {
        try {
            CreditResultMessage resultMessage = new CreditResultMessage();
            resultMessage.setLoanId(loanCore.getId());
            resultMessage.setLoanNo(loanCore.getLoanNo());
            resultMessage.setApprovalResult(approvalResult);
            if (approvedQuota != null) {
                resultMessage.setApprovedQuota(approvedQuota);
            }
            if (interestRate != null) {
                resultMessage.setInterestRate(interestRate);
            }
            resultMessage.setComment(comment);
            resultMessage.setBizFlowNo(loanCore.getBizFlowNo());

            rocketMQTemplate.convertAndSend(
                    RocketMQTopic.LOAN_CREDIT + ":" + RocketMQTopic.Tag.APPROVE,
                    resultMessage
            );
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 获取任务历史
     */
    public Result<List<ApprovalTask>> getTaskHistory(Long loanId) {
        List<ApprovalTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<ApprovalTask>()
                        .eq(ApprovalTask::getLoanId, loanId)
                        .orderByDesc(ApprovalTask::getCreatedTime)
        );
        return Result.success(tasks);
    }
}
