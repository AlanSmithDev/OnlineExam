package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.Major;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MajorDao {
    int deleteByPrimaryKey(Long id);

    int insert(Major record);

    int insertSelective(Major record);

    Major selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Major record);

    int updateByPrimaryKey(Major record);

    List<Major> queryMajorByInstituteNo(String instituteNo); //根据学院编号查询专业

    List<String> likeQueryMajorName(String name); //模糊查询专业名称
}