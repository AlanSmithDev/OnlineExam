package com.fanshuaiko.backage.service.impl;

import com.fanshuaiko.backage.dao.ChoiceDao;
import com.fanshuaiko.backage.dao.PaperStatusDao;
import com.fanshuaiko.backage.dao.ScoreDao;
import com.fanshuaiko.backage.dao.ScoreDetailDao;
import com.fanshuaiko.backage.dao.TestDao;
import com.fanshuaiko.backage.entity.PaperStatus;
import com.fanshuaiko.backage.entity.Score;
import com.fanshuaiko.backage.entity.ScoreDetail;
import com.fanshuaiko.backage.entity.VO.ScoreDetailReturnVo;
import com.fanshuaiko.backage.entity.VO.ScoreQueryTerm;
import com.fanshuaiko.backage.entity.VO.ScoreReturnVo;
import com.fanshuaiko.backage.service.PaperService;
import com.fanshuaiko.backage.utils.ResultData;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private ScoreDetailDao scoreDetailDao;

    @Autowired
    private ChoiceDao choiceDao;

    @Autowired
    private TestDao testDao;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private PaperStatusDao paperStatusDao;

    @Override
    public ResultData queryScoreDetail(long testNo, String graderNo) {
        // Attempt to atomically claim a subjective question (flag '0' -> '2')
        int claimed = scoreDetailDao.claimNextSubjective(testNo, graderNo == null ? "unknown" : graderNo);
        if (claimed <= 0) {
            // no unclaimed subjective left
            paperStatusDao.updateSubjectiveStatusByTestNo(testNo);
            finalizeScoresIfReady(testNo);
            return ResultData.newResultData("0", "试卷全部批改完成");
        }
        // Query the claimed row
        ScoreDetailReturnVo scoreDetailReturnVo = scoreDetailDao.queryClaimedForTest(testNo);
        if (scoreDetailReturnVo == null) {
            // fallback: nothing claimed
            paperStatusDao.updateSubjectiveStatusByTestNo(testNo);
            finalizeScoresIfReady(testNo);
            return ResultData.newResultData("0", "试卷全部批改完成");
        }
        return ResultData.newSuccessResultData(scoreDetailReturnVo);
    }

    @Override
    public ResultData saveStudentScoreDetail(long id, int score) {
        int updated = scoreDetailDao.updateStudentScoreById(id, score);
        return ResultData.newSuccessResultData(updated);
    }

    @Override
    public ResultData markChoice(long testNo) {
        List<Long> questionNos = testDao.queryQuestionNos(testNo);
        if (questionNos == null || questionNos.isEmpty()) {
            paperStatusDao.updateChoiceStatusByTestNo(testNo);
            finalizeScoresIfReady(testNo);
            return ResultData.newSuccessResultData(0);
        }

        List<Map<String, Object>> mapList = choiceDao.questionAnswerMap(questionNos);
        Map<Long, String> answerMap = new HashMap<>();
        if (mapList != null) {
            for (Map<String, Object> row : mapList) {
                Object idObj = row.get("id");
                Object answerObj = row.get("answer");
                if (idObj == null || answerObj == null) {
                    continue;
                }
                Long id;
                if (idObj instanceof Long) {
                    id = (Long) idObj;
                } else if (idObj instanceof Number) {
                    id = ((Number) idObj).longValue();
                } else {
                    try {
                        id = Long.valueOf(idObj.toString());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                answerMap.put(id, String.valueOf(answerObj));
            }
        }

        List<ScoreDetail> scoreDetails = scoreDetailDao.queryAllChoiceByTestNo(testNo);
        if (scoreDetails == null || scoreDetails.isEmpty()) {
            paperStatusDao.updateChoiceStatusByTestNo(testNo);
            finalizeScoresIfReady(testNo);
            return ResultData.newSuccessResultData(0);
        }

        for (ScoreDetail scoreDetail : scoreDetails) {
            Long qNo = scoreDetail.getQuestionNo();
            String rightAnswer = answerMap.get(qNo);
            String stuAnswer = scoreDetail.getStudentAnswer();
            if (isAnswerCorrect(rightAnswer, stuAnswer)) {
                scoreDetail.setStudentScore(scoreDetail.getQuestionScore());
            } else {
                scoreDetail.setStudentScore(0);
            }
        }

        int updated = scoreDetailDao.batchUpdateStudentScore(scoreDetails);
        paperStatusDao.updateChoiceStatusByTestNo(testNo);
        finalizeScoresIfReady(testNo);
        return ResultData.newSuccessResultData(updated);
    }

    @Override
    public ResultData pageQueryPaperStatus(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PaperStatus> paperStatuses = paperStatusDao.selectAll();
        PageInfo<PaperStatus> paperStatusPageInfo = new PageInfo<>(paperStatuses);
        return ResultData.newSuccessResultData(paperStatusPageInfo);
    }

    @Override
    public ResultData pageQueryStudentScore(ScoreQueryTerm scoreQueryTerm) {
        if (scoreQueryTerm == null) {
            scoreQueryTerm = new ScoreQueryTerm();
        }
        int pageNum = scoreQueryTerm.getPageNum() > 0 ? scoreQueryTerm.getPageNum() : 1;
        int pageSize = scoreQueryTerm.getPageSize() > 0 ? scoreQueryTerm.getPageSize() : 100;
        PageHelper.startPage(pageNum, pageSize);
        List<ScoreReturnVo> scoreReturnVoList = scoreDao.queryByConditions(scoreQueryTerm);
        PageInfo<ScoreReturnVo> scoreReturnVoPageInfo = new PageInfo<>(scoreReturnVoList);
        return ResultData.newSuccessResultData(scoreReturnVoPageInfo);
    }

    @Override
    public ResultData releaseClaim(long id) {
        int updated = scoreDetailDao.releaseClaimById(id);
        if (updated > 0) {
            return ResultData.newSuccessResultData(updated);
        } else {
            return ResultData.newResultData("0", "释放失败或该记录未处于已领取状态");
        }
    }

    @Override
    public ResultData queryHistoryPapersByStudentNo(String studentNo) {
        try {
            List<Map<String, Object>> scoreList = scoreDao.queryScoresWithDetailsByStudentNo(studentNo);
            return ResultData.newSuccessResultData(scoreList);
        } catch (Exception e) {
            return ResultData.newResultData("-1", "查询失败");
        }
    }

    @Override
    public ResultData queryWrongQuestionsByStudentNo(String studentNo) {
        try {
            List<Map<String, Object>> wrongQuestions = scoreDetailDao.findWrongQuestionsByStudentNo(studentNo);
            return ResultData.newSuccessResultData(wrongQuestions);
        } catch (Exception e) {
            return ResultData.newResultData("-1", "查询错题失败");
        }
    }

    @Override
    public ResultData getMarkDetail(long testNo) {
        List<Map<String, Object>> list = scoreDetailDao.findMarkDetailByTestNo(testNo);
        ResultData resultData = new ResultData();
        resultData.setCode("0");
        resultData.setMessage("SUCCESS");
        resultData.setData(list);
        return resultData;
    }

    @Override
    public ResultData getTestAnswers(long testNo) {
        List<Map<String, Object>> list = scoreDetailDao.listReferenceAnswersByTestNo(testNo);
        if (list != null) {
            for (Map<String, Object> row : list) {
                Object typeObj = row.get("questionType");
                String label;
                if (typeObj == null) {
                    label = "未知";
                } else {
                    switch (typeObj.toString()) {
                        case "1":
                            label = "单选题";
                            break;
                        case "2":
                            label = "判断题";
                            break;
                        case "3":
                            label = "多选题";
                            break;
                        case "4":
                            label = "主观题";
                            break;
                        default:
                            label = "未知";
                    }
                }
                row.put("questionTypeLabel", label);
            }
        }
        return ResultData.newSuccessResultData(list);
    }

    private void finalizeScoresIfReady(long testNo) {
        PaperStatus paperStatus = paperStatusDao.selectByTestNo(testNo);
        if (paperStatus == null) {
            return;
        }
        boolean choiceDone = "1".equals(paperStatus.getChoiceStatus());
        boolean subjectiveDone = "1".equals(paperStatus.getSubjectiveStatus());
        if (!choiceDone || !subjectiveDone) {
            return;
        }

        List<Score> scoreList = scoreDetailDao.sumTotalScore(testNo);
        scoreDao.deleteByTestNo(testNo);
        if (scoreList != null && !scoreList.isEmpty()) {
            scoreDao.batchAdd(scoreList);
        }
    }

    private boolean isAnswerCorrect(String rightAnswer, String stuAnswer) {
        if (rightAnswer == null || stuAnswer == null) {
            return false;
        }
        String right = normalizeAnswer(rightAnswer);
        String student = normalizeAnswer(stuAnswer);
        return right.equals(student);
    }

    /**
     * Normalize objective answers:
     * 1) remove spaces, convert full-width comma to comma, upper-case
     * 2) for multi-answer forms like "B,A" -> "A,B" (order-insensitive)
     */
    private String normalizeAnswer(String raw) {
        String s = raw.trim().toUpperCase().replace("，", ",").replaceAll("\\s+", "");
        if (!s.contains(",")) {
            return s;
        }
        String[] arr = s.split(",");
        java.util.TreeSet<String> set = new java.util.TreeSet<>();
        for (String item : arr) {
            if (item != null && item.length() > 0) {
                set.add(item);
            }
        }
        return String.join(",", set);
    }
}
