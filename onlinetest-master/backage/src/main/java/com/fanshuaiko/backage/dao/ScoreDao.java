package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.Score;
import com.fanshuaiko.backage.entity.VO.ScoreQueryTerm;
import com.fanshuaiko.backage.entity.VO.ScoreReturnVo;
import com.fanshuaiko.backage.entity.VO.TestAnalyze;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface ScoreDao {
    int deleteByPrimaryKey(Long id);

    int insert(Score record);

    int insertSelective(Score record);

    Score selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Score record);

    int updateByPrimaryKey(Score record);

    int batchAdd(@Param("scoreList") List<Score> scoreList);

    int deleteByTestNo(Long testNo);

    List<ScoreReturnVo> queryByConditions(ScoreQueryTerm scoreQueryTerm);

    TestAnalyze selectMaxAndMinAndAvgScore(Long testNo);

    int countPassStudent(Long testNo);

    List<Map<String, Object>> queryScoresWithDetailsByStudentNo(String studentNo);
}
