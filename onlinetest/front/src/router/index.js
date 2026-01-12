import Vue from 'vue'
import Router from 'vue-router'
import Login from "../views/Login";
import Tests from "../views/Tests";
import Submit from "../views/Submit";
import Profile from "../views/Profile";
import ExamTake from "../views/ExamTake";
import ScoreHistory from "../views/ScoreHistory";
import Messages from "../views/Messages";
import StudentMain from "../views/StudentMain";
import InfoCenter from "../views/InfoCenter";
import Notices from "../views/Notices";
import ExamRules from "../views/ExamRules";
import HistoricalPapers from "../views/student/HistoricalPapers";
import WrongQuestions from "../views/student/WrongQuestions";

Vue.use(Router)


const router = new Router({
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login
    },
    {
      path: '/app',
      component: StudentMain,
      meta:{ requiresAuth: true },
      children: [
        { path: '', redirect: 'tests' },
        {
          path: 'info/notices',
          name: 'Notices',
          component: Notices
        },
        {
          path: 'info/rules',
          name: 'ExamRules',
          component: ExamRules
        },
        {
          path: 'tests',
          name: 'StudentTests',
          component: Tests
        },
        {
          path: 'papers',
          name: 'HistoricalPapers',
          component: HistoricalPapers
        },
        {
          path: 'wrong-questions',
          name: 'WrongQuestions',
          component: WrongQuestions
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: Profile
        },
        { path: 'scores', component: ScoreHistory }
      ]
    },
    // add redirect so '/main' works for student users too
    { path: '/main', redirect: '/app' },
    { path: '/exam/take', component: ExamTake, meta: { requiresAuth: true } },
    { path: '/submit', name: 'submit', component: Submit }
  ]
});


// 导航守卫
// 使用 router.beforeEach 注册一个全局前置守卫，判断用户是否登陆
router.beforeEach((to, from, next) => {
  let token = sessionStorage.getItem('AUTHORIZATION')
  if (to.matched.some(record => record.meta.requiresAuth) && (!token)) {
    next({ path: '/', })
  } else {
    next()
  }
});

export default router;
