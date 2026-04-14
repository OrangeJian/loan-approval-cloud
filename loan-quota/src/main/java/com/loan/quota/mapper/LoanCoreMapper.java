package com.loan.quota.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.loan.common.entity.LoanCore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoanCoreMapper extends BaseMapper<LoanCore> {

    @Update("UPDATE loan_core SET used_quota = used_quota + #{amount}, " +
            "available_quota = total_quota - used_quota - #{amount} " +
            "WHERE id = #{id} AND available_quota >= #{amount}")
    int decreaseAvailableQuota(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);

    @Update("UPDATE loan_core SET used_quota = used_quota - #{amount}, " +
            "available_quota = total_quota - used_quota + #{amount} " +
            "WHERE id = #{id}")
    int increaseAvailableQuota(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);

    @Update("UPDATE loan_core SET total_quota = #{totalQuota}, " +
            "available_quota = #{totalQuota} - used_quota " +
            "WHERE id = #{id}")
    int updateTotalQuota(@Param("id") Long id, @Param("totalQuota") java.math.BigDecimal totalQuota);
}
