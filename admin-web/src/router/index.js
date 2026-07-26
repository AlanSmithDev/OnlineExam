import Vue from 'vue'
import Router from 'vue-router'

import Login from '../views/Login.vue'
import Main from '../views/Main.vue'
import Tests from '../views/Tests.vue'
import Paper from '../views/Paper.vue'
import ChoiceManager from '../views/ChoiceManager.vue'
import SubjectiveManager from '../views/SubjectiveManager.vue'
import UploadQuestion from '../views/UploadQuestion.vue'
import MarkPaper from '../views/MarkPaper.vue'
import StudentScore from '../views/StudentScore.vue'
import Authority from '../views/Authority.vue'
import TeacherNotices from '../views/TeacherNotices.vue'
import TeacherMessages from '../views/TeacherMessages.vue'
import TeacherManagement from '../views/TeacherManagement.vue'
import StudentManagement from '../views/StudentManagement.vue'

Vue.use(Router)

const router = new Router({
  routes: [
    { path: '/', name: 'login', component: Login },
    {
      path: '/main',
      name: 'main',
      meta: { requiresAuth: true },
      component: Main,
      children: [
        { path: 'teacher/info/notices', component: TeacherNotices, name: '通知中心' },
        { path: 'teacher/info/messages', component: TeacherMessages, name: '消息中心' },
        { path: 'tests', component: Tests, name: '所有考试' },
        { path: 'choiceManager', component: ChoiceManager, name: '选择题管理' },
        { path: 'subjectiveManager', component: SubjectiveManager, name: '主观题管理' },
        { path: 'uploadQuestion', component: UploadQuestion, name: '题目上传' },
        { path: 'markPaper', component: MarkPaper, name: '试卷批改' },
        { path: 'studentScore', component: StudentScore, name: '分数查询' },
        { path: 'teacherManagement', component: TeacherManagement, name: '教师管理', meta: { adminOnly: true } },
        { path: 'studentManagement', component: StudentManagement, name: '学生管理' }
      ]
    },
    { path: '/paper', name: 'paper', meta: { requiresAuth: true }, component: Paper },
    { path: '/authority', name: '权限管理', component: Authority }
  ]
})

// 导航守卫：判断用户是否登录
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('AUTHORIZATION')
  const roleId = sessionStorage.getItem('serverRoleId') || sessionStorage.getItem('roleId') || sessionStorage.getItem('flag')
  if (to.matched.some(record => record.meta.requiresAuth) && (!token || token === null)) {
    next({ path: '/' })
    return
  }
  if (to.matched.some(record => record.meta.adminOnly) && String(roleId) !== '0') {
    next({ path: '/main/tests' })
    return
  }
  next()
})

export default router
