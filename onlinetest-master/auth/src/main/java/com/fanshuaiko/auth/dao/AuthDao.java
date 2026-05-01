package com.fanshuaiko.auth.dao;

import com.fanshuaiko.auth.entity.Auth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import org.springframework.stereotype.Repository;

/**
 * @ClassName AuthDao
 * @Description
 * @Author fanshuaiko
 * @Date 19-3-20 下午4:09
 * @Version 1.0
 **/
@Mapper
@Repository
public interface AuthDao {

    @Select("select password from auth where username = #{username}")
    String getPassByNo(String username);

    @Results(id = "authRow", value = {
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Long.class),
            @Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
            @Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
            @Result(column = "roleId", property = "roleId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "flag", property = "flag", jdbcType = JdbcType.VARCHAR),
            @Result(column = "salt", property = "salt", jdbcType = JdbcType.VARCHAR)
    })
    @Select("SELECT id, username, password, `roleId`, flag, salt FROM auth WHERE username = #{username}")
    Auth getAuthByNo(String username);
}
