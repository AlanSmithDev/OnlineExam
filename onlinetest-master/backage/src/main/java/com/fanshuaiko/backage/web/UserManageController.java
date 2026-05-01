package com.fanshuaiko.backage.web;

import com.fanshuaiko.backage.utils.ErrorCode;
import com.fanshuaiko.backage.utils.ResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserManageController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/options/institutes")
    public ResultData listInstitutes(@RequestParam(value = "collegeNo", required = false) String collegeNo) {
        try {
            String cNo = collegeNo == null ? "" : collegeNo.trim();
            List<Map<String, Object>> list;
            try {
                if (cNo.isEmpty()) {
                    list = jdbcTemplate.queryForList("select institute_no as instituteNo, name as instituteName, college_no as collegeNo from institute order by id asc");
                } else {
                    list = jdbcTemplate.queryForList("select institute_no as instituteNo, name as instituteName, college_no as collegeNo from institute where college_no = ? order by id asc", cNo);
                }
            } catch (Exception ignoreNameColumn) {
                if (cNo.isEmpty()) {
                    list = jdbcTemplate.queryForList("select institute_no as instituteNo, institute_name as instituteName, college_no as collegeNo from institute order by id asc");
                } else {
                    list = jdbcTemplate.queryForList("select institute_no as instituteNo, institute_name as instituteName, college_no as collegeNo from institute where college_no = ? order by id asc", cNo);
                }
            }
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询院系失败");
        }
    }

    @GetMapping("/options/majors")
    public ResultData listMajors(@RequestParam(value = "instituteNo", required = false) String instituteNo) {
        try {
            String iNo = instituteNo == null ? "" : instituteNo.trim();
            List<Map<String, Object>> list;
            try {
                if (iNo.isEmpty()) {
                    list = jdbcTemplate.queryForList("select major_no as majorNo, name as majorName, institute_no as instituteNo, college_no as collegeNo from major order by id asc");
                } else {
                    list = jdbcTemplate.queryForList("select major_no as majorNo, name as majorName, institute_no as instituteNo, college_no as collegeNo from major where institute_no = ? order by id asc", iNo);
                }
            } catch (Exception ignoreNameColumn) {
                if (iNo.isEmpty()) {
                    list = jdbcTemplate.queryForList("select major_no as majorNo, major_name as majorName, institute_no as instituteNo, college_no as collegeNo from major order by id asc");
                } else {
                    list = jdbcTemplate.queryForList("select major_no as majorNo, major_name as majorName, institute_no as instituteNo, college_no as collegeNo from major where institute_no = ? order by id asc", iNo);
                }
            }
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询专业失败");
        }
    }

    @GetMapping("/options/classes")
    public ResultData listClasses(@RequestParam(value = "majorNo", required = false) String majorNo) {
        try {
            String mNo = majorNo == null ? "" : majorNo.trim();
            List<Map<String, Object>> list;
            try {
                if (mNo.isEmpty()) {
                    list = jdbcTemplate.queryForList("select class_no as classNo, class_name as className, major_no as majorNo, institute_no as instituteNo, college_no as collegeNo from class order by id asc");
                } else {
                    list = jdbcTemplate.queryForList("select class_no as classNo, class_name as className, major_no as majorNo, institute_no as instituteNo, college_no as collegeNo from class where major_no = ? order by id asc", mNo);
                }
            } catch (Exception ignoreClassNameColumn) {
                if (mNo.isEmpty()) {
                    list = jdbcTemplate.queryForList("select class_no as classNo, class_no as className, major_no as majorNo, institute_no as instituteNo, college_no as collegeNo from class order by id asc");
                } else {
                    list = jdbcTemplate.queryForList("select class_no as classNo, class_no as className, major_no as majorNo, institute_no as instituteNo, college_no as collegeNo from class where major_no = ? order by id asc", mNo);
                }
            }
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询班级失败");
        }
    }

    @GetMapping("/options/courses")
    public ResultData listCourses() {
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select id, course_code as courseCode, name from course order by id asc"
            );
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询课程失败");
        }
    }

    @GetMapping("/subjective/answers/{questionNo}")
    public ResultData listSubjectiveAnswerSheets(@PathVariable("questionNo") long questionNo) {
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select sd.id as id, sd.student_no as studentNo, st.name as studentName, sd.test_no as testNo, t.name as testName, sd.question_no as questionNo, " +
                            "sd.student_answer as studentAnswer, sd.student_score as studentScore, sd.question_score as questionScore, sd.flag as flag, sd.create_time as createTime " +
                            "from score_detail sd " +
                            "left join student st on st.student_no = sd.student_no " +
                            "left join test t on t.id = sd.test_no " +
                            "where sd.question_type = '4' and sd.question_no = ? " +
                            "order by sd.create_time desc, sd.id desc",
                    questionNo);
            return ResultData.newSuccessResultData(list);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询学生试卷失败");
        }
    }

    @GetMapping("/student/page")
    public ResultData pageStudents(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                   @RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "query", required = false) String query) {
        try {
            String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : (query == null ? "" : query.trim());
            int p = Math.max(pageNum, 1);
            int s = Math.max(pageSize, 1);
            int offset = (p - 1) * s;
            String like = "%" + kw + "%";

            Integer total = jdbcTemplate.queryForObject(
                    "select count(1) from student where (? = '' or student_no like ? or name like ? or class_name like ?)",
                    Integer.class,
                    kw, like, like, like);

            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select id, student_no, name, sex, class_no, class_name, major_no, major_name, institute_no, institute_name, college_no, college_name, phone, email " +
                            "from student " +
                            "where (? = '' or student_no like ? or name like ? or class_name like ?) " +
                            "order by id desc limit ? offset ?",
                    kw, like, like, like, s, offset);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total == null ? 0 : total);
            data.put("pageNum", p);
            data.put("pageSize", s);
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询学生失败");
        }
    }

    @PostMapping("/student")
    public ResultData saveStudent(@RequestBody Map<String, Object> body) {
        try {
            String studentNo = str(body.get("student_no"));
            if (studentNo.isEmpty()) {
                studentNo = str(body.get("studentNo"));
            }
            if (studentNo.isEmpty()) {
                return ResultData.newResultData(ErrorCode.VALIDATION_ERROR, "studentNo不能为空");
            }

            String name = pick(body, "name");
            String sex = pick(body, "sex");
            String classNo = pick(body, "class_no", "classNo");
            String className = pick(body, "class_name", "className");
            String majorNo = pick(body, "major_no", "majorNo");
            String majorName = pick(body, "major_name", "majorName");
            String instituteNo = pick(body, "institute_no", "instituteNo");
            String instituteName = pick(body, "institute_name", "instituteName");
            String collegeNo = pick(body, "college_no", "collegeNo");
            String collegeName = pick(body, "college_name", "collegeName");
            String phone = pick(body, "phone");
            String email = pick(body, "email");

            Integer cnt = jdbcTemplate.queryForObject("select count(1) from student where student_no=?", Integer.class, studentNo);
            if (cnt != null && cnt > 0) {
                jdbcTemplate.update("update student set name=?, sex=?, class_no=?, class_name=?, major_no=?, major_name=?, institute_no=?, institute_name=?, college_no=?, college_name=?, phone=?, email=? where student_no=?",
                        name, sex, classNo, className, majorNo, majorName, instituteNo, instituteName, collegeNo, collegeName, phone, email, studentNo);
            } else {
                jdbcTemplate.update("insert into student (student_no,name,sex,class_no,class_name,major_no,major_name,institute_no,institute_name,college_no,college_name,phone,email) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        studentNo, name, sex, classNo, className, majorNo, majorName, instituteNo, instituteName, collegeNo, collegeName, phone, email);
            }
            return ResultData.newSuccessResultData();
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.ADD_FAILOR, "保存学生失败");
        }
    }

    @DeleteMapping("/student/{studentNo}")
    public ResultData deleteStudent(@PathVariable("studentNo") String studentNo) {
        try {
            jdbcTemplate.update("delete from student where student_no=?", studentNo);
            jdbcTemplate.update("delete from auth where username=?", studentNo);
            return ResultData.newSuccessResultData();
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.DEL_FAILOR, "删除学生失败");
        }
    }

    @GetMapping("/teacher/page")
    public ResultData pageTeachers(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                   @RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "query", required = false) String query) {
        try {
            String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : (query == null ? "" : query.trim());
            int p = Math.max(pageNum, 1);
            int s = Math.max(pageSize, 1);
            int offset = (p - 1) * s;
            String like = "%" + kw + "%";

            Integer total = jdbcTemplate.queryForObject(
                    "select count(1) from teacher where (? = '' or teacher_no like ? or name like ? or major_name like ?)",
                    Integer.class,
                    kw, like, like, like);

            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select id, teacher_no, name, sex, major_no, major_name, institute_no, institute_name, college_no, college_name, phone, email, teach_class_scope " +
                            "from teacher " +
                            "where (? = '' or teacher_no like ? or name like ? or major_name like ?) " +
                            "order by id desc limit ? offset ?",
                    kw, like, like, like, s, offset);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total == null ? 0 : total);
            data.put("pageNum", p);
            data.put("pageSize", s);
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询教师失败");
        }
    }

    @PostMapping("/teacher")
    public ResultData saveTeacher(@RequestBody Map<String, Object> body) {
        try {
            String teacherNo = str(body.get("teacher_no"));
            if (teacherNo.isEmpty()) {
                teacherNo = str(body.get("teacherNo"));
            }
            if (teacherNo.isEmpty()) {
                return ResultData.newResultData(ErrorCode.VALIDATION_ERROR, "teacherNo不能为空");
            }

            String name = pick(body, "name");
            String sex = pick(body, "sex");
            String majorNo = pick(body, "major_no", "majorNo");
            String majorName = pick(body, "major_name", "majorName");
            String instituteNo = pick(body, "institute_no", "instituteNo");
            String instituteName = pick(body, "institute_name", "instituteName");
            String collegeNo = pick(body, "college_no", "collegeNo");
            String collegeName = pick(body, "college_name", "collegeName");
            String phone = pick(body, "phone");
            String email = pick(body, "email");
            String teachClassScope = pick(body, "teach_class_scope", "teachClassScope");

            Integer cnt = jdbcTemplate.queryForObject("select count(1) from teacher where teacher_no=?", Integer.class, teacherNo);
            if (cnt != null && cnt > 0) {
                jdbcTemplate.update("update teacher set name=?, sex=?, major_no=?, major_name=?, institute_no=?, institute_name=?, college_no=?, college_name=?, phone=?, email=?, teach_class_scope=? where teacher_no=?",
                        name, sex, majorNo, majorName, instituteNo, instituteName, collegeNo, collegeName, phone, email, teachClassScope, teacherNo);
            } else {
                jdbcTemplate.update("insert into teacher (teacher_no,name,sex,major_no,major_name,institute_no,institute_name,college_no,college_name,phone,email,teach_class_scope) values (?,?,?,?,?,?,?,?,?,?,?,?)",
                        teacherNo, name, sex, majorNo, majorName, instituteNo, instituteName, collegeNo, collegeName, phone, email, teachClassScope);
            }
            return ResultData.newSuccessResultData();
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.ADD_FAILOR, "保存教师失败");
        }
    }

    @DeleteMapping("/teacher/{teacherNo}")
    public ResultData deleteTeacher(@PathVariable("teacherNo") String teacherNo) {
        try {
            jdbcTemplate.update("delete from teacher where teacher_no=?", teacherNo);
            jdbcTemplate.update("delete from auth where username=?", teacherNo);
            return ResultData.newSuccessResultData();
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.DEL_FAILOR, "删除教师失败");
        }
    }

    @GetMapping("/options/questions/choice")
    public ResultData listChoiceQuestions(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          @RequestParam(value = "question", required = false) String question,
                                          @RequestParam(value = "courseName", required = false) String courseName,
                                          @RequestParam(value = "type", required = false) String type) {
        try {
            String q = str(question);
            String c = str(courseName);
            String t = str(type);
            int p = Math.max(pageNum, 1);
            int s = Math.max(pageSize, 1);
            int offset = (p - 1) * s;
            String like = "%" + q + "%";

            Integer total = jdbcTemplate.queryForObject(
                    "select count(1) from choice where (? = '' or question like ?) and (? = '' or course_name = ?) and (? = '' or type = ?)",
                    Integer.class,
                    q, like, c, c, t, t);

            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select id, question, choice_a as choiceA, choice_b as choiceB, choice_c as choiceC, choice_d as choiceD, answer, course_name as courseName, type, create_time as createTime, update_time as updateTime " +
                            "from choice where (? = '' or question like ?) and (? = '' or course_name = ?) and (? = '' or type = ?) " +
                            "order by id desc limit ? offset ?",
                    q, like, c, c, t, t, s, offset);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total == null ? 0 : total);
            data.put("pageNum", p);
            data.put("pageSize", s);
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询选择题失败");
        }
    }

    @GetMapping("/options/questions/subjective")
    public ResultData listSubjectiveQuestions(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                              @RequestParam(value = "question", required = false) String question,
                                              @RequestParam(value = "courseName", required = false) String courseName) {
        try {
            String q = str(question);
            String c = str(courseName);
            int p = Math.max(pageNum, 1);
            int s = Math.max(pageSize, 1);
            int offset = (p - 1) * s;
            String like = "%" + q + "%";

            Integer total = jdbcTemplate.queryForObject(
                    "select count(1) from subjective where (? = '' or question like ?) and (? = '' or course_name = ?)",
                    Integer.class,
                    q, like, c, c);

            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select id, question, answer, course_name as courseName, type, create_time as createTime, update_time as updateTime " +
                            "from subjective where (? = '' or question like ?) and (? = '' or course_name = ?) " +
                            "order by id desc limit ? offset ?",
                    q, like, c, c, s, offset);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total == null ? 0 : total);
            data.put("pageNum", p);
            data.put("pageSize", s);
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询主观题失败");
        }
    }

    @GetMapping("/options/tests/page")
    public ResultData listTests(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                @RequestParam(value = "teacherNo", required = false) String teacherNo,
                                @RequestParam(value = "name", required = false) String name) {
        try {
            String teacher = str(teacherNo);
            if ("all".equalsIgnoreCase(teacher)) {
                teacher = "";
            }
            String testName = str(name);
            int p = Math.max(pageNum, 1);
            int s = Math.max(pageSize, 1);
            int offset = (p - 1) * s;
            String like = "%" + testName + "%";

            Integer total = jdbcTemplate.queryForObject(
                    "select count(1) from test t where (? = '' or t.teacher_no = ?) and (? = '' or t.name like ?)",
                    Integer.class,
                    teacher, teacher, testName, like);

            List<Map<String, Object>> list = jdbcTemplate.queryForList(
                    "select t.id, t.name, t.teacher_no as teacherNo, t.course_id as courseId, c.name as courseName, ifnull(te.name, t.teacher_no) as teacherName, " +
                            "t.total_score as totalScore, t.status, t.start_time as startTime, t.end_time as endTime, t.test_time as testTime, t.exam_notice as examNotice, t.exam_intro as examIntro, " +
                            "t.camera_required as cameraRequired, t.institute_no as instituteNo, t.institute_name as instituteName, t.major_no as majorNo, t.major_name as majorName, t.class_scope as classScope " +
                            "from test t left join course c on t.course_id = c.id left join teacher te on t.teacher_no = te.teacher_no " +
                            "where (? = '' or t.teacher_no = ?) and (? = '' or t.name like ?) order by t.start_time desc, t.id desc limit ? offset ?",
                    teacher, teacher, testName, like, s, offset);

            for (Map<String, Object> row : list) {
                Long testId = asLong(row.get("id"));
                if (testId != null) {
                    List<String> classNoList = jdbcTemplate.query(
                            "select class_no from test_class where test_no = ? order by id asc",
                            new Object[]{testId},
                            (rs, rowNum) -> rs.getString(1)
                    );
                    row.put("classNoList", classNoList);
                } else {
                    row.put("classNoList", new ArrayList<String>());
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total == null ? 0 : total);
            data.put("pageNum", p);
            data.put("pageSize", s);
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询考试失败");
        }
    }

    @GetMapping("/options/test-preview/{testNo}")
    public ResultData previewTest(@PathVariable("testNo") Long testNo) {
        try {
            List<Map<String, Object>> tests = jdbcTemplate.queryForList(
                    "select t.id, t.name, t.teacher_no as teacherNo, t.course_id as courseId, c.name as courseName, ifnull(te.name, t.teacher_no) as teacherName, " +
                            "t.total_score as totalScore, t.status, t.start_time as startTime, t.end_time as endTime, t.test_time as testTime, t.exam_notice as examNotice, t.exam_intro as examIntro, " +
                            "t.camera_required as cameraRequired, t.institute_no as instituteNo, t.institute_name as instituteName, t.major_no as majorNo, t.major_name as majorName, t.class_scope as classScope " +
                            "from test t left join course c on t.course_id = c.id left join teacher te on t.teacher_no = te.teacher_no where t.id = ? limit 1",
                    testNo);
            if (tests.isEmpty()) {
                return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "考试不存在");
            }
            Map<String, Object> testInfo = tests.get(0);
            List<String> classNoList = jdbcTemplate.query(
                    "select class_no from test_class where test_no = ? order by id asc",
                    new Object[]{testNo},
                    (rs, rowNum) -> rs.getString(1)
            );
            List<Map<String, Object>> questions = jdbcTemplate.queryForList(
                    "select tq.question_no as id, tq.score, tq.question_type as type, c.question, c.choice_a as choiceA, c.choice_b as choiceB, c.choice_c as choiceC, c.choice_d as choiceD, c.answer, c.course_name as courseName " +
                            "from test_question tq left join choice c on tq.question_type in ('1','2','3') and c.id = tq.question_no where tq.test_no = ? " +
                            "union all " +
                            "select tq.question_no as id, tq.score, tq.question_type as type, s.question, null as choiceA, null as choiceB, null as choiceC, null as choiceD, s.answer, s.course_name as courseName " +
                            "from test_question tq left join subjective s on tq.question_type = '4' and s.id = tq.question_no where tq.test_no = ?",
                    testNo, testNo);
            Map<String, Object> data = new HashMap<>();
            data.put("testInfo", testInfo);
            data.put("classNoList", classNoList);
            data.put("questions", questions);
            return ResultData.newSuccessResultData(data);
        } catch (Exception e) {
            return ResultData.newResultData(ErrorCode.QUERY_FAILOR, "查询考试详情失败");
        }
    }

    private String pick(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object v = body.get(key);
            if (v != null) {
                String s = String.valueOf(v).trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
        }
        return "";
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignore) {
            return null;
        }
    }
}
