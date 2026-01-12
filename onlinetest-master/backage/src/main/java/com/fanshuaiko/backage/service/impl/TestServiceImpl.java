package com.fanshuaiko.backage.service.impl;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.fanshuaiko.backage.dao.ChoiceDao;
import com.fanshuaiko.backage.dao.ScoreDao;
import com.fanshuaiko.backage.dao.SubjectiveDao;
import com.fanshuaiko.backage.dao.TestDao;
import com.fanshuaiko.backage.dict.QuestionType;
import com.fanshuaiko.backage.dict.TestStatus;
import com.fanshuaiko.backage.entity.Choice;
import com.fanshuaiko.backage.entity.Subjective;
import com.fanshuaiko.backage.entity.TestQuestion;
import com.fanshuaiko.backage.entity.VO.QuestionReturnVo;
import com.fanshuaiko.backage.entity.VO.TestAnalyze;
import com.fanshuaiko.backage.entity.VO.TestReturnVo;
import com.fanshuaiko.backage.entity.VO.TestVO;
import com.fanshuaiko.backage.entity.VO.TestPreviewVo;
import com.fanshuaiko.backage.entity.mail.ScoreParams;
import com.fanshuaiko.backage.service.MailService;
import com.fanshuaiko.backage.service.TestService;
import com.fanshuaiko.backage.utils.*;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName TestServiceImpl
 * @Description
 * @Author ALanSmith
 * @Date 26-3-17 下午1:56
 * @Version 1.0
 **/
@Service
@Transactional
public class TestServiceImpl implements TestService {

  private static Logger log = LoggerFactory.getLogger(TestServiceImpl.class);
  @Autowired
  private TestDao testDao;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private ChoiceDao choiceDao;

  @Autowired
  private SubjectiveDao subjectiveDao;

  @Autowired
  private ImportUtilService importUtil;

  @Autowired
  private ScoreDao scoreDao;

  @Autowired
  private MailService mailService;

  @Override
  public ResultData createTest(TestVO testVO) {
    try {
      testVO.setId(SnowflakeIdWorker.nextId());
      testVO.setStatus(TestStatus.NotBegin.getCODE());
      if (!org.springframework.util.StringUtils.hasText(testVO.getExamNotice())) {
        testVO.setExamNotice("考试注意事项：开考前请确认网络稳定，严禁切换或缩小考试窗口。");
      }
      if (!org.springframework.util.StringUtils.hasText(testVO.getExamIntro())) {
        testVO.setExamIntro("开考前请核对考试信息并保持摄像头开启。");
      }
      if (testVO.getCameraRequired() == null) {
        testVO.setCameraRequired(true);
      }

      // 设置考试结束时间
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date startTime = sdf.parse(testVO.getStartTime());
      Date endTime = new Date(startTime.getTime() + testVO.getTestTime() * 60 * 1000);
      String endTime2 = sdf.format(endTime);
      testVO.setEndTime(endTime2);

      // 保存上传的题目保存到题目表
      if (testVO.getSingleRedisId() != 0) {
        List<Choice> singleList = (List<Choice>) redisTemplate.opsForValue().get(testVO.getSingleRedisId());
        if (CollectionUtils.isEmpty(singleList)) {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "保存单选题失败，请重新上传！");
        } else {
          // 保存题目到提目表
          choiceDao.batchAdd(singleList);
          // 保存题目与考试相关表
          List<Long> singleIdList = returnIdFromObj(singleList);
          testDao.insertTestQuestion(testVO.getId(), singleIdList, testVO.getSingleScore(),
              QuestionType.SingleChoice.getCODE());
        }
      }
      if (testVO.getJudgeRedisId() != 0) {
        List<Choice> judgeList = (List<Choice>) redisTemplate.opsForValue().get(testVO.getJudgeRedisId());
        if (CollectionUtils.isEmpty(judgeList)) {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "保存判断题失败，请重新上传！");
        } else {
          choiceDao.batchAdd(judgeList);
          List<Long> judgeIdList = returnIdFromObj(judgeList);
          testDao.insertTestQuestion(testVO.getId(), judgeIdList, testVO.getJudgeScore(),
              QuestionType.JudgeChoice.getCODE());
        }
      }
      if (testVO.getMultipleRedisId() != 0) {
        List<Choice> multipleList = (List<Choice>) redisTemplate.opsForValue().get(testVO.getMultipleRedisId());
        if (CollectionUtils.isEmpty(multipleList)) {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "保存多选题失败，请重新上传！");
        } else {
          choiceDao.batchAdd(multipleList);
          List<Long> multipleIdList = returnIdFromObj(multipleList);
          testDao.insertTestQuestion(testVO.getId(), multipleIdList, testVO.getMultipleScore(),
              QuestionType.MultipleChoice.getCODE());
        }
      }
      if (testVO.getSubjectiveRedisId() != 0) {
        List<Subjective> subjectiveList = (List<Subjective>) redisTemplate.opsForValue()
            .get(testVO.getSubjectiveRedisId());
        if (CollectionUtils.isEmpty(subjectiveList)) {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "保存主观题失败，请重新上传！");
        } else {
          subjectiveDao.batchAdd(subjectiveList);
          LinkedList<Long> subjectiveIdList = new LinkedList<>();
          for (Subjective subjective : subjectiveList) {
            subjectiveIdList.add(subjective.getId());
          }
          testDao.insertTestQuestion(testVO.getId(), subjectiveIdList, testVO.getSubjectiveScore(),
              QuestionType.Subjective.getCODE());
        }
      }

      // 保存题目到test_question表
      if (testVO.getSingleRandomCount() != 0) {
        // 根据课程和题目类型随机返回指定数量的题目id
        List<Long> ids = choiceDao.returnRandomQuestionIds(QuestionType.SingleChoice.getCODE(), testVO.getSingleRandomCount(),
            testVO.getCourseId());
        if (ids.size() != 0) {
          testDao.insertTestQuestion(testVO.getId(), ids, testVO.getSingleScore(), QuestionType.SingleChoice.getCODE());
        } else {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "题库中没有该课程的单选题！");
        }
      }
      if (testVO.getJudgeRandomCount() != 0) {
        // 根据课程和题目类型随机返回指定数量的题目id
        List<Long> ids = choiceDao.returnRandomQuestionIds(QuestionType.JudgeChoice.getCODE(), testVO.getJudgeRandomCount(),
            testVO.getCourseId());
        if (ids.size() != 0) {
          testDao.insertTestQuestion(testVO.getId(), ids, testVO.getJudgeScore(), QuestionType.JudgeChoice.getCODE());
        } else {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "题库中没有该课程的判断题！");
        }
      }
      if (testVO.getMultipleRandomCount() != 0) {
        // 根据课程和题目类型随机返回指定数量的题目id
        List<Long> ids = choiceDao.returnRandomQuestionIds(QuestionType.MultipleChoice.getCODE(), testVO.getMultipleRandomCount(),
            testVO.getCourseId());
        if (ids.size() != 0) {
          testDao.insertTestQuestion(testVO.getId(), ids, testVO.getMultipleScore(),
              QuestionType.MultipleChoice.getCODE());
        } else {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "题库中没有该课程的多选题！");
        }
      }
      if (testVO.getSubjectiveRandomCount() != 0) {
        // 根据课程和题目类型随机返回指定数量的题目id
        List<Long> ids = subjectiveDao.returnRandomQuestionIds(QuestionType.Subjective.getCODE(), testVO.getSubjectiveRandomCount(),
            testVO.getCourseId());
        if (ids.size() != 0) {
          testDao.insertTestQuestion(testVO.getId(), ids, testVO.getSubjectiveScore(),
              QuestionType.Subjective.getCODE());
        } else {
          return ResultData.newResultData(ErrorCode.ADD_FAILOR, "题库中没有该课程的主观题！");
        }
      }

      // 保存test相关信息到test表
      int i = testDao.insertTest(testVO);
      log.info("插入test成功，记录" + i + "条");
      // 保存class信息到test_class表
      int i1 = testDao.insertTestClass(testVO);
      log.info("插入test_class成功，记录" + i1 + "条");

      return ResultData.newSuccessResultData();
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.ADD_FAILOR, "新建考试失败！");
    }

  }

  @Override
  public ResultData uploadChoice(MultipartFile file, String type) {
    try {
      ResultData resultData = importUtil.checkImportChoice(file, type);
      if (resultData.getData() == null) {
        return resultData;
      }
      List<Choice> choiceList = (List<Choice>) resultData.getData();
      for (Choice choice : choiceList) {
        choice.setId(SnowflakeIdWorker.nextId());
        choice.setType(type);
      }
      HashMap<Long, Integer> map = new HashMap<>();
      if (type.equals(QuestionType.SingleChoice.getCODE())) {
        long singleRedisId = SnowflakeIdWorker.nextId();
        redisTemplate.opsForValue().set(singleRedisId, choiceList);
        redisTemplate.expire(singleRedisId, 1, TimeUnit.HOURS);
        map.put(singleRedisId, choiceList.size());
      } else if (type.equals(QuestionType.JudgeChoice.getCODE())) {
        long judgeRedisId = SnowflakeIdWorker.nextId();
        redisTemplate.opsForValue().set(judgeRedisId, choiceList);
        redisTemplate.expire(judgeRedisId, 1, TimeUnit.HOURS);
        map.put(judgeRedisId, choiceList.size());
      } else if (type.equals(QuestionType.MultipleChoice.getCODE())) {
        long multipleRedisId = SnowflakeIdWorker.nextId();
        redisTemplate.opsForValue().set(multipleRedisId, choiceList);
        redisTemplate.expire(multipleRedisId, 1, TimeUnit.HOURS);
        map.put(multipleRedisId, choiceList.size());
      }
      return ResultData.newSuccessResultData(map);
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.FAILOR, "上传失败！");
    }
  }

  @Override
  public ResultData uploadSubjective(MultipartFile file, String type) {
    try {
      long subjectiveRedisId = SnowflakeIdWorker.nextId();
      ResultData resultData = importUtil.checkImportSubjective(file, type);
      if (resultData.getData() == null) {
        return resultData;
      }
      List<Subjective> subjectiveList = (List<Subjective>) resultData.getData();
      redisTemplate.opsForValue().set(subjectiveRedisId, subjectiveList);
      redisTemplate.expire(subjectiveRedisId, 1, TimeUnit.HOURS);
      HashMap<Long, Integer> map = new HashMap<>();
      map.put(subjectiveRedisId, subjectiveList.size());
      return ResultData.newSuccessResultData(map);
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.FAILOR, "上传失败！");
    }
  }

  @Override
  public ResultData deleteTest(List<Long> ids) {
    try {
      if (ids == null || ids.isEmpty()) {
        return ResultData.newResultData(ErrorCode.DEL_ID_FAILOR, ErrorCode.DEL_ID_FAILOR_MSG);
      }
      // 当前实现只删除 test 表，这里保留简单批量删除逻辑
      int i = testDao.batchDeleteByIds(ids);
      return ResultData.newSuccessResultData(i);
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.DEL_ID_FAILOR, ErrorCode.DEL_FAILOR_MSG);
    }
  }

  @Override
  public ResultData previewTest(long testNo) {
    try {
      // 查询考试基本信息
      com.fanshuaiko.backage.entity.Test test = testDao.selectByPrimaryKey(testNo);
      if (test == null) {
        return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "考试不存在");
      }
      // 查询班级列表
      List<String> classNos = testDao.queryClassNosByTestNo(testNo);

      // 题目明细：容错处理，避免脏数据导致整个预览失败
      List<QuestionReturnVo> questionList = new LinkedList<>();
      ResultData questionResult = queryQuestionDetail(testNo);
      if (questionResult != null && "0".equals(String.valueOf(questionResult.getCode())) && questionResult.getData() instanceof List) {
        questionList = (List<QuestionReturnVo>) questionResult.getData();
      }

      TestPreviewVo vo = new TestPreviewVo();
      vo.setTestInfo(test);
      vo.setClassNoList(classNos == null ? new LinkedList<>() : classNos);
      vo.setQuestions(questionList);
      return ResultData.newSuccessResultData(vo);
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.QUERY_FAILOR, ErrorCode.QUERY_FAILOR_MSG);
    }
  }

  @Override
  public ResultData updateTest(TestVO testVo) {
    try {
      if (testVo == null || testVo.getId() == null) {
        return ResultData.newResultData(ErrorCode.UPDATE_FAILOR, "考试ID不能为空");
      }
      if (!StringUtils.hasText(testVo.getExamNotice())) {
        testVo.setExamNotice("考试注意事项：开考前请确认网络稳定，严禁切换或缩小考试窗口。");
      }
      if (!StringUtils.hasText(testVo.getExamIntro())) {
        testVo.setExamIntro("开考前请核对考试信息并保持摄像头开启。");
      }
      if (testVo.getCameraRequired() == null) {
        testVo.setCameraRequired(true);
      }
      // 仅更新考试的基础信息字段，不变更题目结构和班级关联
      com.fanshuaiko.backage.entity.Test test = new com.fanshuaiko.backage.entity.Test();
      test.setId(testVo.getId());
      test.setName(testVo.getName());
      test.setCourseId(testVo.getCourseId());
      test.setTeacherNo(testVo.getTeacherNo());
      // Test.totalScore 为 String 类型，这里做显式转换
      if (testVo.getTotalScore() != null) {
        test.setTotalScore(String.valueOf(testVo.getTotalScore()));
      }
      test.setStatus(testVo.getStatus());
      test.setTestTime(testVo.getTestTime());
      test.setExamNotice(testVo.getExamNotice());
      test.setExamIntro(testVo.getExamIntro());
      test.setCameraRequired(testVo.getCameraRequired());

      // 处理开始/结束时间与考试时长，容错解析
      if (StringUtils.hasText(testVo.getStartTime())) {
        try {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Date startTime = sdf.parse(testVo.getStartTime());
          test.setStartTime(startTime);
          if (testVo.getTestTime() != null && testVo.getTestTime() > 0) {
            Date endTime = new Date(startTime.getTime() + testVo.getTestTime() * 60L * 1000L);
            test.setEndTime(endTime);
          }
        } catch (Exception parseEx) {
          log.warn("updateTest parse startTime failed: {}", testVo.getStartTime(), parseEx);
        }
      }

      testDao.updateByPrimaryKeySelective(test);

      // 编辑时同步更新参考班级（先清空再插入）
      testDao.deleteTestClassByTestNo(testVo.getId());
      if (testVo.getClassNoList() != null && !testVo.getClassNoList().isEmpty()) {
        testDao.insertTestClass(testVo);
      }

      return ResultData.newSuccessResultData();
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.UPDATE_FAILOR, ErrorCode.UPDATE_FAILOR_MSG);
    }
  }

  @Override
  public ResultData pageQueryTest(int pageNum, int pageSize, String teacherNo, String name) {
    PageHelper.startPage(pageNum, pageSize);
    String teacherNoFilter = teacherNo == null ? null : teacherNo.trim();
    if (!StringUtils.hasText(teacherNoFilter) || "all".equalsIgnoreCase(teacherNoFilter)) {
      teacherNoFilter = null;
    }
    String testNameFilter = name == null ? null : name.trim();
    if (!StringUtils.hasText(testNameFilter)) {
      testNameFilter = null;
    }
    List<TestReturnVo> testVOList = testDao.queryTests(teacherNoFilter, testNameFilter);
    if (teacherNoFilter != null && (testVOList == null || testVOList.isEmpty())) {
      testVOList = testDao.queryTests(null, testNameFilter);
    }
    if (testVOList != null) {
      Date now = new Date();
      for (TestReturnVo vo : testVOList) {
        Date start = safeParseDate(vo.getStartTime());
        Date end = safeParseDate(vo.getEndTime());
        vo.setStatus(resolveDynamicStatus(vo.getStatus(), start, end, now));
      }
    }
    PageInfo<TestReturnVo> info = new PageInfo<>(testVOList);
    return ResultData.newSuccessResultData(info);
  }

  private Date safeParseDate(String source) {
    if (!StringUtils.hasText(source)) {
      return null;
    }
    try {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(source);
    } catch (Exception ex) {
      log.warn("Failed to parse date string [{}]", source, ex);
      return null;
    }
  }

  private String resolveDynamicStatus(String storedStatus, Date startTime, Date endTime, Date now) {
    if (startTime != null && now.before(startTime)) {
      return "0";
    }
    if (endTime != null && now.after(endTime)) {
      return "2";
    }
    if (startTime != null && (endTime == null || (now.after(startTime) && !now.after(endTime)))) {
      return "1";
    }
    return storedStatus == null ? "0" : storedStatus;
  }

  @Override
  public ResultData queryQuestionDetail(long testNo) {
    LinkedList<QuestionReturnVo> questionReturnVos = new LinkedList<>();
    List<TestQuestion> testQuestions = testDao.queryTestQuestion(testNo);
    if (testQuestions == null || testQuestions.isEmpty()) {
      return ResultData.newSuccessResultData(questionReturnVos);
    }

    for (TestQuestion testQuestion : testQuestions) {
      if (QuestionType.Subjective.getCODE().equals(testQuestion.getQuestionType())) {
        Subjective subjective = subjectiveDao.selectByPrimaryKey(testQuestion.getQuestionNo());
        if (subjective == null) {
          continue;
        }
        QuestionReturnVo questionReturnVo = new QuestionReturnVo();
        questionReturnVo.setId(subjective.getId());
        questionReturnVo.setQuestion(subjective.getQuestion());
        questionReturnVo.setAnswer(subjective.getAnswer());
        questionReturnVo.setCourseName(subjective.getCourseName());
        questionReturnVo.setScore(testQuestion.getScore());
        questionReturnVo.setType(testQuestion.getQuestionType());
        questionReturnVo.setCreateTime(subjective.getCreateTime());
        questionReturnVo.setUpdateTime(subjective.getUpdateTime());
        questionReturnVos.add(questionReturnVo);
      } else {
        Choice choice = choiceDao.selectByPrimaryKey(testQuestion.getQuestionNo());
        if (choice == null) {
          continue;
        }
        QuestionReturnVo questionReturnVo = new QuestionReturnVo();
        questionReturnVo.setId(choice.getId());
        questionReturnVo.setQuestion(choice.getQuestion());
        questionReturnVo.setChoiceA(choice.getChoiceA());
        questionReturnVo.setChoiceB(choice.getChoiceB());
        if (!StringUtils.isEmpty(choice.getChoiceC())) {
          questionReturnVo.setChoiceC(choice.getChoiceC());
        }
        if (!StringUtils.isEmpty(choice.getChoiceD())) {
          questionReturnVo.setChoiceD(choice.getChoiceD());
        }
        questionReturnVo.setAnswer(choice.getAnswer());
        questionReturnVo.setCourseName(choice.getCourseName());
        questionReturnVo.setCreateTime(choice.getCreateTime());
        questionReturnVo.setUpdateTime(choice.getUpdateTime());
        questionReturnVo.setScore(testQuestion.getScore());
        questionReturnVo.setType(testQuestion.getQuestionType());
        questionReturnVos.add(questionReturnVo);
      }
    }
    return ResultData.newSuccessResultData(questionReturnVos);
  }

  @Override
  public ResultData getTestAnalyzeData(Long testNo) {
    TestAnalyze testAnalyze = new TestAnalyze();
    testAnalyze.setTestNo(testNo);
    // 计算参考人数和考试总分
    TestAnalyze testAnalyze2 = testDao.sumStudentCount(testNo);
    int studentCount = 0;
    int totalScore = 0;
    if (testAnalyze2 != null) {
      studentCount = testAnalyze2.getStudentCount();
      totalScore = testAnalyze2.getTotalScore();
    }
    testAnalyze.setTotalScore(totalScore);
    testAnalyze.setStudentCount(studentCount);
    // 计算最高分、最低分、平均分
    TestAnalyze testAnalyze1 = scoreDao.selectMaxAndMinAndAvgScore(testNo);
    int maxScore = 0;
    int minScore = 0;
    double averageScore = 0D;
    if (testAnalyze1 != null) {
      maxScore = testAnalyze1.getMaxScore();
      minScore = testAnalyze1.getMinScore();
      averageScore = testAnalyze1.getAverageScore();
    }
    testAnalyze.setMaxScore(maxScore);
    testAnalyze.setMinScore(minScore);
    testAnalyze.setAverageScore(averageScore);
    // 及格人数
    int passCount = scoreDao.countPassStudent(testNo);
    testAnalyze.setPassCount(passCount);
    // 不及格人数
    testAnalyze.setUnPassCount(Math.max(studentCount - passCount, 0));
    // 及格率
    testAnalyze.setPassRate(studentCount == 0 ? 0D : (double) passCount / studentCount);

    return ResultData.newSuccessResultData(testAnalyze);
  }

  @Override
  public ResultData sendTestScoreMail(Long testNo) {
    try {

      // get analysis data (defensive)
      Object dataObj = getTestAnalyzeData(testNo).getData();
      TestAnalyze testAnalyze = dataObj instanceof TestAnalyze ? (TestAnalyze) dataObj : null;

      List<ScoreParams> paramsList = testDao.queryParamsForMail(testNo);
      if (paramsList == null || paramsList.isEmpty()) {
        return ResultData.newResultData(ErrorCode.FAILOR, "暂无可发送的成绩数据，请先完成阅卷");
      }

      // populate analysis fields and sanitize recipients
      int populated = 0;
      List<ScoreParams> toSend = new java.util.ArrayList<>();
      for (ScoreParams scoreParams : paramsList) {
        // set aggregate fields from testAnalyze if available
        if (testAnalyze != null) {
          scoreParams.setStudentCount(testAnalyze.getStudentCount());
          scoreParams.setAverageScore(testAnalyze.getAverageScore());
          scoreParams.setMaxScore(testAnalyze.getMaxScore());
          scoreParams.setPassRate(testAnalyze.getPassRate());
          // if testName not present in row, set from analyze (if set)
          if ((scoreParams.getTestName() == null || scoreParams.getTestName().isEmpty()) && testAnalyze.getTestName() != null) {
            scoreParams.setTestName(testAnalyze.getTestName());
          }
        }
        // ensure recipient address exists
        if (scoreParams.getToMailAddress() != null && !scoreParams.getToMailAddress().isEmpty()) {
          toSend.add(scoreParams);
        } else {
          log.warn("Skipping ScoreParams with missing email: " + scoreParams);
        }
      }
      if (toSend.isEmpty()) {
        return ResultData.newResultData(ErrorCode.FAILOR, "所有成绩数据均无有效邮件地址，无法发送");
      }
      String s = mailService.sendHTMLMail(toSend);
      return ResultData.newSuccessResultData(s);
    } catch (Exception e) {
      e.printStackTrace();
      return ResultData.newResultData(ErrorCode.FAILOR, "发送邮件失败！");
    }
  }

  private List<Long> returnIdFromObj(List<Choice> choiceList) {
    LinkedList<Long> idList = new LinkedList<>();
    for (Choice choice : choiceList) {
      idList.add(choice.getId());
    }
    return idList;
  }

}

