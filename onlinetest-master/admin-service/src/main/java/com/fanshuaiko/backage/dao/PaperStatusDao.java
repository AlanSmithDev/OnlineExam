package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.PaperStatus;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PaperStatusDao {
    int deleteByPrimaryKey(Long id);

    int insert(PaperStatus record);

    int insertSelective(PaperStatus record);

    PaperStatus selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PaperStatus record);

    int updateByPrimaryKey(PaperStatus record);

    List<PaperStatus> selectAll();

    PaperStatus selectByTestNo(long testNo);

    int updateChoiceStatusByTestNo(long testNo);

    int updateSubjectiveStatusByTestNo(long testNo);
}
