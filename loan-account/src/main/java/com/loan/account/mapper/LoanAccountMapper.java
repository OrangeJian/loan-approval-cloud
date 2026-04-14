package com.loan.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loan.account.entity.LoanAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoanAccountMapper extends BaseMapper<LoanAccount> {
}
