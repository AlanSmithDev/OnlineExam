import axios from 'axios';

// backage-service is configured with context-path: /backage-api (see onlinetest-master/backage application.yml).
// Under webpack-dev-server (default :8084) we rely on proxy for /backage-api.
// Under other origins (e.g. :8086 static/dev hosting without proxy), call backend directly.
const currentPort = (typeof window !== 'undefined' && String(window.location.port)) || '';
const BACKAGE_ORIGIN = (currentPort === '8084' || currentPort === '8085')
  ? '/backage-api'
  : 'http://localhost:8094/backage-api';

const backageUrl = (path) => `${BACKAGE_ORIGIN}${path}`;
const testBase = backageUrl('/test');

const isSuccessResponse = (res) => !!(res && res.data && res.data.code === '0');

const withFallback = (primaryRequest, fallbackRequest) => {
  return primaryRequest()
    .then((res) => (isSuccessResponse(res) ? res : fallbackRequest()))
    .catch(() => fallbackRequest());
};

// (所有接口当前均使用 JSON @RequestBody 或 REST-style 参数，未使用表单编码 helper)

// 根据id删除考试
export const deleteTestByIds = (id) => {
  return axios.delete(`${testBase}/test/` + id);
};

// 根据教师编号分页查询考试
export const pageQueryTest = (params) => {
  return withFallback(
    () => axios.get(
      `${testBase}/test/` + params.pageNum + '/' + params.pageSize + '/' + params.username,
      { params: { name: params.name || '' } }
    ),
    () => axios.get(backageUrl(`/options/tests/page`), {
      params: {
        pageNum: params.pageNum,
        pageSize: params.pageSize,
        teacherNo: params.username,
        name: params.name || ''
      }
    })
  );
};

// 考试预览
export const previewTest = (testNo) => {
  return withFallback(
    () => axios.get(`${testBase}/preview/` + testNo),
    () => axios.get(backageUrl(`/options/test-preview/` + testNo))
  );
};

// 编辑考试
export const updateTest = (id, data) => {
  return axios.put(`${testBase}/test/` + id, data);
};

// 查询所有课程
export const getAllCourse = () => {
  return withFallback(
    () => axios.get(backageUrl(`/course/course`)),
    () => axios.get(backageUrl(`/options/courses`))
  );
};

// 新建考试
export const createTest = (params) => {
  return axios.post(`${testBase}/test`, params);
};

// 根据考试编号查询该场考试的试题
export const getQuestionByTestNo = (testNo) => {
  return axios.get(`${testBase}/questionDetail/` + testNo);
};

// 多条件分页查询选择题
export const choicePageQuery = (params) => {
  return withFallback(
    () => axios.get(backageUrl(`/question/choice`), { params }),
    () => axios.get(backageUrl(`/options/questions/choice`), { params })
  );
};

// 新增选择题
export const insertChoice = (params) => {
  return axios.post(backageUrl(`/question/choice`), params);
};

// 修改选择题（controller uses @RequestBody）
export const updateChoice = (params) => {
  return axios.put(backageUrl(`/question/choice`), params);
};

// 根据id删除当前选择题
export const deleteChoiceById = (id) => {
  return axios.delete(backageUrl(`/question/choice/` + id));
};

// 多条件分页查询主观题
export const subjectivePageQuery = (params) => {
  return withFallback(
    () => axios.get(backageUrl(`/question/subjective`), { params }),
    () => axios.get(backageUrl(`/options/questions/subjective`), { params })
  );
};

// 新增主观题
export const insertSubjective = (params) => {
  return axios.post(backageUrl(`/question/subjective`), params);
};

// 修改主观题
export const updateSubjective = (params) => {
  return axios.put(backageUrl(`/question/subjective`), params);
};

// 删除主观题
export const deleteSubjectiveById = (id) => {
  return axios.delete(backageUrl(`/question/subjective/` + id));
};

// 分页查询PaperStatus
export const pageQueryPaperStatus = (params) => {
  return axios.get(backageUrl(`/paper/paperStatus/` + params.pageNum + '/' + params.pageSize));
};

// 批改选择题
export const markChoice = (params) => {
  return axios.post(backageUrl(`/paper/choice/` + params));
};

// 查询未批改的一道主观题
export const queryScoreDetail = ({ testNo, graderNo } = {}) => {
  const url = backageUrl(`/paper/scoreDetail/` + testNo + (graderNo ? `?graderNo=${encodeURIComponent(graderNo)}` : ''));
  return axios.get(url);
};

// 保存当前批改题目的得分
export const saveStudentScoreDetail = (params) => {
  return axios.post(backageUrl(`/paper/scoreDetail/` + params.id + '/' + params.score));
};

// release a claimed subjective score detail (cancel claim)
export const releaseScoreDetail = (id) => {
  return axios.post(backageUrl(`/paper/scoreDetail/release/` + id));
};

// 模糊查询专业名称
export const likeQueryMajorName = (params) => {
  return axios.get(backageUrl(`/class/major/name`), { params });
};

// 多条件分页查询分数
export const pageQueryStudentScore = (params) => {
  return axios.get(backageUrl(`/paper/score`), { params });
};

// 根据多条件导出分数
export const exportScoreExcel = (params) => {
  return axios.get(backageUrl(`/file/score`), { params });
};

// 获取考试分析数据
export const getTestAnalyzeData = (params) => {
  return axios.get(backageUrl(`/test/analyze/` + params));
};

// 发送成绩单邮件
export const sendTestScoreMail = (params) => {
  return axios.post(backageUrl(`/test/mail/` + params));
};

// 条件分页查询教师课程权限
export const pageQueryCourseAuthority = (params) => {
  return axios.get(backageUrl(`/authority/course`), { params });
};

// 修改教师课程权限
export const editCourseAuthority = (params) => {
  return axios.post(backageUrl(`/authority/course`), params);
};

// class/institute/major tree data
export const queryInstituteByCollegeNo = (collegeNo) => {
  return axios.get(backageUrl(`/class/institute/` + collegeNo));
};

export const queryMajorByInstituteNo = (instituteNo) => {
  return axios.get(backageUrl(`/class/major/` + instituteNo));
};

export const queryClassByMajorNo = (majorNo) => {
  return axios.get(backageUrl(`/class/class/` + majorNo));
};

// 获取考试答案
export const getTestAnswers = (testNo) => axios.get(backageUrl(`/paper/testAnswers/${testNo}`));

// 获取阅卷明细（某场考试所有学生主观题作答）
export const listMarkDetailByTestNo = (testNo) => {
  return axios.get(backageUrl(`/paper/markDetail/` + encodeURIComponent(testNo)));
};

// 查询考试行为记录（支持按考试/学生筛选）
export const listExamBehavior = (params) => {
  return axios.get(backageUrl(`/examBehavior/list`), { params });
};

// 通知列表（targetRole: 0全部 1学生 2教师）
export const fetchTeacherNotices = (params = {}) => {
  const query = { targetRole: 2, ...params };
  return axios.get(backageUrl('/notice/list'), { params: query });
};

export const fetchStudentNotices = (params = {}) => {
  const query = { targetRole: 1, ...params };
  return axios.get(backageUrl('/notice/list'), { params: query });
};

export const fetchExamRules = (limit = 20) => {
  return axios.get(backageUrl('/notice/list'), { params: { targetRole: 9, limit } });
};

export const publishNotice = (payload) => {
  return axios.post(backageUrl('/notice/publish'), payload);
};

export const publishExamRule = (payload) => {
  return axios.post(backageUrl('/notice/publish'), { ...payload, targetRole: 9 });
};

export const updateNotice = (payload) => {
  return axios.put(backageUrl('/notice/update'), payload);
};

export const deleteNotice = (id) => {
  return axios.delete(backageUrl('/notice/delete/' + encodeURIComponent(id)));
};

// 即时通讯历史记录
export const fetchMessageHistory = (limit = 50) => {
  return axios.get(backageUrl('/message/history'), { params: { limit } });
};

// 学生管理：分页查询/新增或修改/删除
export const pageQueryStudents = (params) => {
  // backend may expect /student/page?pageNum=1&pageSize=10
  return axios.get(backageUrl(`/student/page`), { params });
};

export const saveStudent = (student) => {
  // if student has id or studentNo, backend should handle upsert
  return axios.post(backageUrl(`/student`), student);
};

export const deleteStudent = (studentNo) => {
  return axios.delete(backageUrl(`/student/` + encodeURIComponent(studentNo)));
};

// 教师管理：分页查询/新增或修改/删除
export const pageQueryTeachers = (params) => {
  return axios.get(backageUrl(`/teacher/page`), { params });
};

export const listInstitutes = (collegeNo) => {
  const params = {};
  if (collegeNo) params.collegeNo = collegeNo;
  return axios.get(backageUrl(`/options/institutes`), { params });
};

export const listMajors = (instituteNo) => {
  const params = {};
  if (instituteNo) params.instituteNo = instituteNo;
  return axios.get(backageUrl(`/options/majors`), { params });
};

export const listClasses = (majorNo) => {
  const params = {};
  if (majorNo) params.majorNo = majorNo;
  return axios.get(backageUrl(`/options/classes`), { params });
};

export const listSubjectiveAnswerSheets = (questionNo) => {
  return axios.get(backageUrl(`/subjective/answers/` + encodeURIComponent(questionNo)));
};

export const saveTeacher = (teacher) => {
  return axios.post(backageUrl(`/teacher`), teacher);
};

export const deleteTeacher = (teacherNo) => {
  return axios.delete(backageUrl(`/teacher/` + encodeURIComponent(teacherNo)));
};
