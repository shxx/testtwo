package com.ledger.session;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SessionMapper {

    @Insert("INSERT INTO sys_session (token, account_id, expire_at, created_at) " +
            "VALUES (#{token}, #{accountId}, #{expireAt}, #{createdAt})")
    int insert(Session session);

    @Select("SELECT token, account_id, expire_at, created_at FROM sys_session WHERE token = #{token}")
    Session findByToken(@Param("token") String token);

    @Delete("DELETE FROM sys_session WHERE token = #{token}")
    int deleteByToken(@Param("token") String token);

    @Delete("DELETE FROM sys_session WHERE expire_at < #{now}")
    int deleteExpired(@Param("now") java.time.LocalDateTime now);
}
