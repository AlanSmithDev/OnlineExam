package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface NoticeDao {
    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(@Param("id") Long id);

    Notice getById(@Param("id") Long id);

    List<Notice> listByTarget(@Param("targetRole") Integer targetRole, @Param("limit") Integer limit);

    List<Notice> listRules(@Param("limit") Integer limit);
}
