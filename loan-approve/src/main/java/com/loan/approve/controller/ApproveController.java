package com.loan.approve.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.ApprovalDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.ApprovalTask;
import com.loan.approve.service.ApproveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approve")
public class ApproveController {

    @Autowired
    private ApproveService approveService;

    @GetMapping("/tasks/pending")
    public Result<Page<ApprovalTask>> getPendingTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String stage) {
        return approveService.getPendingTasks(pageNum, pageSize, stage);
    }

    @PostMapping("/claim")
    public Result<Void> claimTask(@RequestParam String taskNo,
                                   @RequestParam Long approverId,
                                   @RequestParam String approverName) {
        return approveService.claimTask(taskNo, approverId, approverName);
    }

    @PostMapping("/execute")
    public Result<Void> approve(@RequestBody ApprovalDTO dto) {
        return approveService.approve(dto);
    }

    @GetMapping("/history/{loanId}")
    public Result<List<ApprovalTask>> getTaskHistory(@PathVariable Long loanId) {
        return approveService.getTaskHistory(loanId);
    }
}
