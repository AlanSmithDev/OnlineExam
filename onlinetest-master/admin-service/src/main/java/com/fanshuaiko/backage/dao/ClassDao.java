package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.SchoolClass;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ClassDao {
    int deleteByPrimaryKey(Long id);

    int insert(SchoolClass record);

    int insertSelective(SchoolClass record);

    SchoolClass selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SchoolClass record);

    int updateByPrimaryKey(SchoolClass record);

    List<SchoolClass> queryClassByMajorNo(String majorNo); //根据专业编码查询班级
}