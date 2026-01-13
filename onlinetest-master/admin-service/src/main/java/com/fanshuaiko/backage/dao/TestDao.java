package com.fanshuaiko.backage.dao;

import com.fanshuaiko.backage.entity.Test;
import com.fanshuaiko.backage.entity.TestQuestion;
import com.fanshuaiko.backage.entity.VO.TestAnalyze;
import com.fanshuaiko.backage.entity.VO.TestReturnVo;
import com.fanshuaiko.backage.entity.VO.TestVO;
import com.fanshuaiko.backage.entity.mail.ScoreParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TestDao {
    int deleteByPrimaryKey(Long id);

    int insert(Test record);

    int insertSelective(Test record);

    Test selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Test record);

    int updateByPrimaryKey(Test record);

    int insertTest(TestVO testVO); //保存考试信息到test表

    int insertTestClass(TestVO testVO); //保存参与考试的班级到test_class表

    int insertTestQuestion(@Param("testNo") long testNo, @Param("questionNoList") List<Long> questionNoList,
                           @Param("score") int score, @Param("questionType") String questionType); //保存考试题目信息到test_question 表

    List<TestReturnVo> queryTests(@Param("teacherNo") String teacherNo,
                                  @Param("name") String name); //条件查询考试信息

    List<TestQuestion> queryTestQuestion(@Param("testNo") long testNo); //根据testNo查询考试题目关系

    List<Long> queryQuestionNos(@Param("testNo") Long testNo); //根据考试编号查询所有题目id

    @Select("select id,name,status,start_time as startTime,end_time as endTime from test where status !='2'")
    List<Test> getAllTest();

    @Select("update test set status = '1' where id = #{id}")
    void setRunningStatus(Long id);

    @Select("update test set status = '2' where id = #{id}")
    void setFinishedStatus(Long id);

    int batchDeleteByIds(@Param("ids") List<Long> ids);//批量删除

    int deleteTestClassByTestNo(@Param("testNo") Long testNo);

    List<TestQuestion> selectByQuestionNo(Long testNo);//根据试题编号查询

    TestAnalyze sumStudentCount(@Param("testNo") Long testNo);//计算参加考试人数

    List<ScoreParams> queryParamsForMail(@Param("testNo") Long testNo);//根据考试编码查询多个发成绩单需要的参数的值

    /** 根据考试编号查询参与考试的班级编号列表 */
    List<String> queryClassNosByTestNo(@Param("testNo") Long testNo);
}
