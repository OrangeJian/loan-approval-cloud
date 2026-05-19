package com.loan.gateway.controller;

import com.loan.common.constant.ErrorCode;
import com.loan.common.dto.ChannelRequestDTO;
import com.loan.common.dto.ChannelResponseDTO;
import com.loan.gateway.config.PartnerChannelConfig;
import com.loan.gateway.dto.LoanApplyRequestDTO;
import com.loan.gateway.feign.FacadeFeignClient;
import com.loan.gateway.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 渠道接入控制器 - 外部合作方 HTTP 入口
 */
@RestController
@RequestMapping("/gateway")
public class ChannelController {

    private static final Logger log = LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FacadeFeignClient facadeFeignClient;

    /**
     * 授信申请
     */
    @PostMapping("/{channel}/credit/apply")
    public ChannelResponseDTO creditApply(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.CHANNEL_AUTH_FAILED, "签名验证失败", null);
        }

        if (!channelService.checkRateLimit(channel, request.getBizFlowNo())) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    "5003", "请求过于频繁", null);
        }

        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            facadeFeignClient.creditApply(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SUCCESS, "授信申请提交成功", null);
        } catch (Exception e) {
            log.error("授信申请失败 channel={}", channel, e);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage(), null);
        }
    }

    /**
     * 用信申请（贷款申请）— 外部合作方专用入口
     *
     * 请求流程：
     * 1. 渠道验证 — 检查渠道是否存在且已启用
     * 2. 签名验签 — HMAC-SHA256 验证
     * 3. 频率限制 — 渠道自定义阈值
     * 4. 业务参数解析与校验 — LoanApplyRequestDTO 反序列化 + Bean Validation
     * 5. 转发 loan-facade 异步处理
     * 6. 返回签名后的响应
     */
    @PostMapping("/{channel}/loan/apply")
    public ChannelResponseDTO loanApply(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        // 1. 渠道验证
        PartnerChannelConfig channelConfig = channelService.loadChannelConfig(channel);
        if (channelConfig == null) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.CHANNEL_AUTH_FAILED, "渠道不存在或已禁用", null);
        }

        // 2. 签名验证
        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.CHANNEL_SIGN_INVALID, "签名验证失败", null);
        }

        // 3. 频率控制
        if (!channelService.checkRateLimit(channel, request.getBizFlowNo())) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    "5003", "请求过于频繁", null);
        }

        // 4. 解析并校验业务参数
        LoanApplyRequestDTO loanRequest = channelService.parseLoanApplyRequest(request);
        if (loanRequest == null) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.PARAM_ERROR, "业务数据格式错误", null);
        }

        String validationError = channelService.validateLoanApplyRequest(loanRequest);
        if (validationError != null) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.PARAM_ERROR, "参数校验失败：" + validationError, null);
        }

        // 5. 组装业务数据并转发
        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());
        businessData.put("customerId", loanRequest.getCustomerId());
        businessData.put("productCode", loanRequest.getProductCode());
        businessData.put("applyAmount", loanRequest.getApplyAmount());
        businessData.put("applyTerm", loanRequest.getApplyTerm());
        businessData.put("repaymentType", loanRequest.getRepaymentType());

        try {
            facadeFeignClient.loanApply(businessData);
            // 6. 返回签名响应
            return channelService.buildSignedResponse(channelConfig, request.getBizFlowNo(),
                    ErrorCode.SUCCESS, "用信申请提交成功", null);
        } catch (Exception e) {
            log.error("用信申请失败 channel={}", channel, e);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage(), null);
        }
    }

    /**
     * 还款
     */
    @PostMapping("/{channel}/repay")
    public ChannelResponseDTO repay(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.CHANNEL_AUTH_FAILED, "签名验证失败", null);
        }

        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            facadeFeignClient.repay(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SUCCESS, "还款提交成功", null);
        } catch (Exception e) {
            log.error("还款失败 channel={}", channel, e);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage(), null);
        }
    }

    /**
     * 查询
     */
    @PostMapping("/{channel}/query")
    public ChannelResponseDTO query(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.CHANNEL_AUTH_FAILED, "签名验证失败", null);
        }

        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            var result = facadeFeignClient.query(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SUCCESS, "查询成功", result.getData());
        } catch (Exception e) {
            log.error("查询失败 channel={}", channel, e);
            return channelService.buildResponse(channel, request.getBizFlowNo(),
                    ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage(), null);
        }
    }
}
