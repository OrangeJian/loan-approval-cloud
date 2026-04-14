package com.loan.gateway.controller;

import com.loan.common.dto.ChannelRequestDTO;
import com.loan.common.dto.ChannelResponseDTO;
import com.loan.gateway.feign.FacadeFeignClient;
import com.loan.gateway.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 渠道接入控制器
 */
@RestController
@RequestMapping("/gateway")
public class ChannelController {

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

        // 1. 签名验证
        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "5001", "签名验证失败", null);
        }

        // 2. 频率控制
        if (!channelService.checkRateLimit(channel, request.getBizFlowNo())) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "5003", "请求过于频繁", null);
        }

        // 3. 解析业务数据
        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            // 4. 调用前置服务
            facadeFeignClient.creditApply(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(), "0000", "授信申请提交成功", null);
        } catch (Exception e) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "9999", "系统错误：" + e.getMessage(), null);
        }
    }

    /**
     * 用信申请
     */
    @PostMapping("/{channel}/loan/apply")
    public ChannelResponseDTO loanApply(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "5001", "签名验证失败", null);
        }

        if (!channelService.checkRateLimit(channel, request.getBizFlowNo())) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "5003", "请求过于频繁", null);
        }

        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            facadeFeignClient.loanApply(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(), "0000", "用信申请提交成功", null);
        } catch (Exception e) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "9999", "系统错误：" + e.getMessage(), null);
        }
    }

    /**
     * 还款
     */
    @PostMapping("/{channel}/repay")
    public ChannelResponseDTO repay(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "5001", "签名验证失败", null);
        }

        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            facadeFeignClient.repay(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(), "0000", "还款提交成功", null);
        } catch (Exception e) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "9999", "系统错误：" + e.getMessage(), null);
        }
    }

    /**
     * 查询
     */
    @PostMapping("/{channel}/query")
    public ChannelResponseDTO query(@PathVariable String channel, @RequestBody ChannelRequestDTO request) {
        request.setChannelCode(channel);

        if (!channelService.verifySign(request)) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "5001", "签名验证失败", null);
        }

        Map<String, Object> businessData = channelService.parseRequestData(request);
        businessData.put("channelCode", channel);
        businessData.put("bizFlowNo", request.getBizFlowNo());

        try {
            var result = facadeFeignClient.query(businessData);
            return channelService.buildResponse(channel, request.getBizFlowNo(), "0000", "查询成功", result.getData());
        } catch (Exception e) {
            return channelService.buildResponse(channel, request.getBizFlowNo(), "9999", "系统错误：" + e.getMessage(), null);
        }
    }
}
