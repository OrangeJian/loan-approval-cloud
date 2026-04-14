package com.loan.loan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loan.common.entity.LoanCore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoanCoreMapper extends BaseMapper<LoanCore> {

    @Update("UPDATE loan_core SET current_balance = current_balance + #{amount}, " +
            "loan_status = #{status} WHERE id = #{id}")
    int updateBalanceAndStatus(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount, @Param("status") String status);
}
