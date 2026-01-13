package com.fanshuaiko.backage.service;

import com.fanshuaiko.backage.entity.VO.ScoreQueryTerm;
import com.fanshuaiko.backage.utils.ResultData;

/**
 * @ClassName PaperService
 * @Description
 * @Author fanshuaiko
 * @Date 19-4-1 下午2:54
 * @Version 1.0
 **/
public interface PaperService {
    /**
     * 根据考试编码查询未批改的主观题，并可标注当前批改人
     *
     * @param testNo
     * @param graderNo  当前批改教师工号/账号（可选）
     * @return
     */
    ResultData queryScoreDetail(long testNo, String graderNo);

    ResultData saveStudentScoreDetail(long id,int score);

    ResultData markChoice(long testNo);

    ResultData pageQueryPaperStatus(int pageNum, int pageSize);

    ResultData pageQueryStudentScore(ScoreQueryTerm scoreQueryTerm);

    // Release a claimed subjective question by id (cancel grading claim)
    ResultData releaseClaim(long id);

    // 历史试卷与错题查询功能
    ResultData queryHistoryPapersByStudentNo(String studentNo);

    ResultData queryWrongQuestionsByStudentNo(String studentNo);

    ResultData getMarkDetail(long testNo);

    ResultData getTestAnswers(long testNo);
}
