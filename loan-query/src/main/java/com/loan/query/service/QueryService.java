package com.loan.query.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.loan.common.dto.LoanQueryDTO;
import com.loan.common.dto.Result;
import com.loan.common.entity.LoanCore;
import com.loan.common.entity.LoanTransaction;
import com.loan.common.entity.LoanQuotaChange;
import com.loan.query.mapper.LoanCoreMapper;
import com.loan.query.mapper.LoanTransactionMapper;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询服务
 */
@Service
public class QueryService implements RocketMQListener<Object> {

    @Autowired
    private LoanCoreMapper loanCoreMapper;

    @Autowired
    private LoanTransactionMapper transactionMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 查询贷款详情
     */
    public Result<Map<String, Object>> getLoanDetail(String loanNo) {
        // 先查缓存
        String cacheKey = "loan:detail:" + loanNo;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // 实际应该反序列化
        }

        LoanCore loanCore = loanCoreMapper.selectOne(
                new LambdaQueryWrapper<LoanCore>().eq(LoanCore::getLoanNo, loanNo)
        );
        if (loanCore == null) {
            return Result.error("贷款记录不存在");
        }

        // 获取交易流水
        List<LoanTransaction> transactions = transactionMapper.selectList(
                new LambdaQueryWrapper<LoanTransaction>()
                        .eq(LoanTransaction::getLoanId, loanCore.getId())
                        .orderByDesc(LoanTransaction::getCreatedTime)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("loanCore", loanCore);
        result.put("transactions", transactions);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, "1");
        redisTemplate.expire(cacheKey, 300); // 5分钟

        return Result.success(result);
    }

    /**
     * 查询贷款列表
     */
    public Result<Page<LoanCore>> getLoanList(LoanQueryDTO queryDTO) {
        Page<LoanCore> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<LoanCore> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getCustomerId() != null) {
            wrapper.eq(LoanCore::getCustomerId, queryDTO.getCustomerId());
        }
        if (queryDTO.getLoanStatus() != null) {
            wrapper.eq(LoanCore::getLoanStatus, queryDTO.getLoanStatus());
        }
        if (queryDTO.getProductType() != null) {
            wrapper.eq(LoanCore::getProductType, queryDTO.getProductType());
        }

        wrapper.orderByDesc(LoanCore::getCreatedTime);
        Page<LoanCore> result = loanCoreMapper.selectPage(page, wrapper);

        return Result.success(result);
    }

    /**
     * 查询交易流水
     */
    public Result<List<LoanTransaction>> getTransactionHistory(Long loanId) {
        List<LoanTransaction> transactions = transactionMapper.selectList(
                new LambdaQueryWrapper<LoanTransaction>()
                        .eq(LoanTransaction::getLoanId, loanId)
                        .orderByDesc(LoanTransaction::getCreatedTime)
        );
        return Result.success(transactions);
    }

    @Override
    public void onMessage(Object message) {
        // 消费各类事件，刷新缓存
        // 实际应该根据不同消息类型处理
    }
}
