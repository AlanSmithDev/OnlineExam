import axios from 'axios';

const BACKAGE_BASE = '';

export const login = (username, password) => {
  const p = new URLSearchParams();
  p.append('username', username);
  p.append('password', password);
  return axios.post('/auth-api/auth/login', p);
}

export const getAvailableTests = (username) => axios.get(`${BACKAGE_BASE}/backage-api/test/student/${encodeURIComponent(username)}`);
export const getTestQuestions = (testNo) => {
  return axios.get('/front-api/test/' + testNo);
}
export const submitAnswers = (answers, token) => {
  return axios.post('/front-api/test/commit', answers, { headers: { 'AUTHORIZATION': token } });
}
export const getMyScores = (studentNo) => {
  const token = (typeof window !== 'undefined' && window.sessionStorage)
    ? (window.sessionStorage.getItem('AUTHORIZATION') || '')
    : '';
  const headers = token ? { AUTHORIZATION: token } : {};
  return axios.get('/backage-api/paper/student/history/' + encodeURIComponent(studentNo), { headers });
}

export const fetchStudentNotices = (limit = 20) => {
  return axios.get('/backage-api/notice/list', { params: { targetRole: 1, limit } });
}

export const fetchExamRules = (limit = 20) => {
  return axios.get('/backage-api/notice/list', { params: { targetRole: 9, limit } })
}

// 考试防作弊行为上报
export const reportExamBehavior = (payload) => {
  return axios.post(BACKAGE_BASE + '/examBehavior/report', payload);
}

// --- backage paper/paperStatus/score endpoints used by mark paper UI ---
export const pageQueryPaperStatus = (params) => axios.get(`${BACKAGE_BASE}/backage-api/paper/status`, { params });
export const markChoice = (testNo) => axios.post(`${BACKAGE_BASE}/backage-api/paper/markChoice/${encodeURIComponent(testNo)}`);
export const queryScoreDetail = ({ testNo, graderNo }) => axios.get(`${BACKAGE_BASE}/backage-api/score/detail`, { params: { testNo, graderNo } });
export const saveStudentScoreDetail = ({ id, score }) => axios.post(`${BACKAGE_BASE}/backage-api/score/detail/save`, { id, score });
export const releaseScoreDetail = (id) => axios.post(`${BACKAGE_BASE}/backage-api/score/detail/release/${encodeURIComponent(id)}`);
export const getTestAnalyzeData = (testNo) => axios.get(`${BACKAGE_BASE}/backage-api/test/analyze/${encodeURIComponent(testNo)}`);
export const sendTestScoreMail = (testNo) => axios.post(`${BACKAGE_BASE}/backage-api/test/sendMail/${encodeURIComponent(testNo)}`);
export const getTestAnswers = (testNo) => axios.get(`${BACKAGE_BASE}/backage-api/test/answers/${encodeURIComponent(testNo)}`);

// Student/Teacher management API (used by management views)
export function pageQueryStudents(pageNum, pageSize, query) {
  return axios.get(`${BACKAGE_BASE}/backage-api/student/page/${pageNum}/${pageSize}`, { params: { query } });
}
export function saveStudent(student) {
  return axios.post(`${BACKAGE_BASE}/backage-api/student/save`, student);
}
export function deleteStudent(studentId) {
  return axios.delete(`${BACKAGE_BASE}/backage-api/student/${encodeURIComponent(studentId)}`);
}
export function pageQueryTeachers(pageNum, pageSize, query) {
  return axios.get(`${BACKAGE_BASE}/backage-api/teacher/page/${pageNum}/${pageSize}`, { params: { query } });
}
export function saveTeacher(teacher) {
  return axios.post(`${BACKAGE_BASE}/backage-api/teacher/save`, teacher);
}
export function deleteTeacher(teacherId) {
  return axios.delete(`${BACKAGE_BASE}/backage-api/teacher/${encodeURIComponent(teacherId)}`);
}
