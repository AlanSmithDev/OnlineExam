package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.ImMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ImMessageDao {
    int insert(ImMessage message);

    List<ImMessage> selectLatest(@Param("limit") int limit);
}
