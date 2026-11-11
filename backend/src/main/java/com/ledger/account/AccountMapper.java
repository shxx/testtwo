package com.ledger.account;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 台账账号查询（本期只读：账号仅由初始化脚本预置，无维护界面）。
 */
@Mapper
public interface AccountMapper {

    @Select("SELECT id, name, mobile, status, created_at, updated_at " +
            "FROM ledger_account WHERE mobile = #{mobile}")
    Account findByMobile(@Param("mobile") String mobile);

    @Select("SELECT id, name, mobile, status, created_at, updated_at " +
            "FROM ledger_account WHERE id = #{id}")
    Account findById(@Param("id") Long id);
}
