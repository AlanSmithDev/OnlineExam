package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.entity.VO.QuestionQueryTerm;
import com.fanshuaiko.backage.entity.VO.ScoreQueryTerm;
import com.fanshuaiko.backage.service.PaperService;
import com.fanshuaiko.backage.utils.ErrorCode;
import com.fanshuaiko.backage.utils.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName PaperController
 * @Description
 * @Author ALanSmith
 * @Date 26-2-25 下午2:51
 * @Version 1.0
 **/
@RestController
@RequestMapping("/paper")
public class PaperController {
    private static Logger log = LoggerFactory.getLogger(PaperController.class);

    @Autowired
    private PaperService paperService;

    /**
     * 根据考试编码查询未批改的主观题
     *
     * @param testNo
     * @param graderNo 可选的批改教师工号/账号
     * @return
     */
    @GetMapping("/scoreDetail/{testNo}")
    public ResultData queryScoreDetail(@PathVariable("testNo") long testNo, @RequestParam(value = "graderNo", required = false) String graderNo) {
        try {
            ResultData resultData = paperService.queryScoreDetail(testNo, graderNo);
            return resultData;
        } catch (Exception e) {
            log.info("--------paper:queryScoreDetail--------");
            e.printStackTrace();
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, ErrorCode.QUERY_FAILOR_MSG);
        }
    }

    /**
     * 根据score_detail的id更新学生题目得分
     * @param id
     * @param score
     * @return
     */
    @PostMapping("/scoreDetail/{id}/{score}")
    public ResultData saveStudentScoreDetail(@PathVariable("id") long id,@PathVariable("score") int score){
        try {
            ResultData resultData = paperService.saveStudentScoreDetail(id,score);
            return resultData;
        } catch (Exception e) {
            log.info("--------paper:saveStudentScoreDetail--------");
            e.printStackTrace();
            return ResultData.newResultData(ErrorCode.UPDATE_FAILOR, ErrorCode.UPDATE_FAILOR_MSG);
        }
    }

    /**
     * 释放已claim的主观题（取消批改）
     * @param id
     * @return
     */
    @PostMapping("/scoreDetail/release/{id}")
    public ResultData releaseScoreDetail(@PathVariable("id") long id) {
        try {
            ResultData resultData = paperService.releaseClaim(id);
            return resultData;
        } catch (Exception e) {
            log.info("--------paper:releaseScoreDetail--------");
            e.printStackTrace();
            return ResultData.newResultData(ErrorCode.UPDATE_FAILOR, ErrorCode.UPDATE_FAILOR_MSG);
        }
    }

    /**
     * 批改该场考试的选择题
     * @param testNo
     * @return
     */
    @PostMapping("choice/{testNo}")
    public ResultData markChoice(@PathVariable("testNo") long testNo){
        try {
            ResultData resultData = paperService.markChoice(testNo);
            return resultData;
        } catch (Exception e) {
            log.info("--------paper:markChoice--------");
            e.printStackTrace();
            return ResultData.newResultData(ErrorCode.FAILOR, "批改出现异常！");
        }
    }

    /**
     * 分页查询PagerStatus
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("paperStatus/{pageNum}/{pageSize}")
    public ResultData pageQueryPaperStatus(@PathVariable("pageNum") int pageNum,@PathVariable("pageSize") int pageSize){
        try {
            ResultData resultData = paperService.pageQueryPaperStatus(pageNum,pageSize);
            return resultData;
        } catch (Exception e) {
            log.info("--------paper:pageQueryPaperStatus--------");
            e.printStackTrace();
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, ErrorCode.QUERY_FAILOR_MSG);
        }
    }

    /**
     * 多参数分页查询学生分数
     * @param scoreQueryTerm
     * @return
     */
    @GetMapping("/score")
    public ResultData pageQueryStudentScore(ScoreQueryTerm scoreQueryTerm){
        try {
            ResultData resultData = paperService.pageQueryStudentScore(scoreQueryTerm);
            return resultData;
        } catch (Exception e) {
            log.info("--------paper:pageQueryStudentScore--------");
            e.printStackTrace();
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, ErrorCode.QUERY_FAILOR_MSG);
        }
    }

    /**
     * 根据学生学号查询其所有已完成的试卷列表
     *
     * @param studentNo 学生学号
     * @return 已完成的试卷列表
     */
    @GetMapping("/student/history/{studentNo}")
    public ResultData getStudentHistoryPapers(@PathVariable("studentNo") String studentNo) {
        return paperService.queryHistoryPapersByStudentNo(studentNo);
    }

    /**
     * 根据学生学号查询其所有错题
     * @param studentNo 学号
     * @return
     */
    @GetMapping("/student/wrong/{studentNo}")
    public ResultData getStudentWrongQuestions(@PathVariable("studentNo") String studentNo) {
        return paperService.queryWrongQuestionsByStudentNo(studentNo);
    }

    /**
     * 获取阅卷明细（含所有学生的主观题答案及参考答案）
     */
    @GetMapping("/markDetail/{testNo}")
    public ResultData getMarkDetail(@PathVariable("testNo") long testNo) {
        return paperService.getMarkDetail(testNo);
    }

    /**
     * 查看整套试卷的题型答案
     */
    @GetMapping("/testAnswers/{testNo}")
    public ResultData getTestAnswers(@PathVariable("testNo") long testNo) {
        try {
            return paperService.getTestAnswers(testNo);
        } catch (Exception e) {
            log.info("--------paper:getTestAnswers--------", e);
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, ErrorCode.QUERY_FAILOR_MSG);
        }
    }
}
