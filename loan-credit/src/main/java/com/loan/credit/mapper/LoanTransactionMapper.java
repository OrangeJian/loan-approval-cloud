package com.loan.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loan.common.entity.LoanTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoanTransactionMapper extends BaseMapper<LoanTransaction> {
}
