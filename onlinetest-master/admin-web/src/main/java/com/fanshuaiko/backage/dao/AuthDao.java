package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.Auth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AuthDao {
    Auth selectByUsername(@Param("username") String username);
}
