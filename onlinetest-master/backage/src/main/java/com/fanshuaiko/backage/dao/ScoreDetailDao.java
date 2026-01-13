package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.Score;
import com.fanshuaiko.backage.entity.ScoreDetail;
import com.fanshuaiko.backage.entity.VO.ScoreDetailReturnVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface ScoreDetailDao {
    int insert(ScoreDetail record);

    int insertSelective(ScoreDetail record);

    ScoreDetailReturnVo queryScoreDetail(long testNo); // 根据考试编码查询未批改的主观题

    int updateStudentScoreById(@Param("id") long id, @Param("score") int score); //根据score_detail的id更新学生题目得分

    List<ScoreDetail> queryAllChoiceByTestNo(Long testNo); //根据考试编号查询所有选择题的得分信息

    int batchUpdateStudentScore(@Param("scoreDetails") List<ScoreDetail> scoreDetails); //批量更新学生得分

    List<Score> sumTotalScore(long testNo); //统计每个人的总分

    List<Map<String, Object>> findWrongQuestionsByStudentNo(String studentNo);

    // --- new methods for claim/release flow ---
    /**
     * Atomically claim the next ungraded subjective question (flag='0') by setting flag='2' and recording grader.
     * Returns number of rows updated (0 or 1).
     */
    int claimNextSubjective(@Param("testNo") Long testNo, @Param("graderNo") String graderNo);

    /**
     * Query a claimed subjective score detail (flag='2') for the given testNo. Returns one record.
     */
    ScoreDetailReturnVo queryClaimedForTest(@Param("testNo") Long testNo);

    /**
     * Release a previously claimed subjective question (set flag back to '0') if still in-progress.
     */
    int releaseClaimById(@Param("id") long id);
    List<Map<String, Object>> findMarkDetailByTestNo(@Param("testNo") long testNo);

    List<Map<String, Object>> listReferenceAnswersByTestNo(@Param("testNo") long testNo);
}