<template>
  <el-container style=" border: 1px solid #eee;">
    <el-aside width="200px">

      <el-header class="aside-header">
        ONLINEEXAM
      </el-header>

      <el-menu :default-openeds="['1']" @select="handleMenuSelect" :default-active="activeMenu">
        <el-submenu index="1">
          <template slot="title"><i class="el-icon-message"></i>管理</template>
          <el-menu-item-group>
            <el-menu-item v-if="isAdmin" index="teacher">教师管理</el-menu-item>
            <el-menu-item index="student">学生管理</el-menu-item>
            <el-menu-item index="course">课程权限</el-menu-item>
          </el-menu-item-group>
        </el-submenu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <div class="topbar-right">
          <span class="user-name">{{usernameDisplay}}</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <el-main>
        <component :is="currentComponent" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import TeacherManagement from './TeacherManagement.vue'
  import StudentManagement from './StudentManagement.vue'
  import CourseAuthorityContent from './AuthorityCourseContent.vue'

  export default {
    name: 'AuthorityPage',
    components: { TeacherManagement, StudentManagement, CourseAuthorityContent },
    data() {
      const roleId = sessionStorage.getItem('roleId') || sessionStorage.getItem('role') || '';
      const flag = sessionStorage.getItem('flag') || '';
      const isAdmin = (roleId === '0' || flag === '0');
      let defaultComp = 'CourseAuthorityContent';
      let defaultMenu = 'course';
      if (isAdmin) {
        defaultComp = 'TeacherManagement';
        defaultMenu = 'teacher';
      }
      return {
        username: sessionStorage.getItem('username') || '',
        usernameDisplay: (sessionStorage.getItem('username') || '') + (sessionStorage.getItem('realName') ? (' / ' + sessionStorage.getItem('realName')) : ''),
        activeMenu: defaultMenu,
        currentComponent: defaultComp,
        isAdmin
      }
    },
    methods: {
      logout() {
        sessionStorage.clear();
        this.$router.push('/');
      },
      handleMenuSelect(key) {
        this.activeMenu = key;
        if (key === 'teacher') this.currentComponent = 'TeacherManagement'
        else if (key === 'student') this.currentComponent = 'StudentManagement'
        else if (key === 'course') this.currentComponent = 'CourseAuthorityContent'
      }
    }
  }
</script>

<style scoped>
  .el-container {
    height: 800px;
  }

  .el-header {
    background-color: #20a0ff;
    color: white;
    line-height: 60px;
  }

  .aside-header {
    text-align: center;
    font-size: 20px;
  }

  .topbar {
    text-align: right;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 16px;
  }

  .topbar-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .user-name {
    font-size: 12px;
  }

  .el-aside {
    color: #eef1f6;
    background-color: #eef1f6;
  }

  .el-menu-item-group {
    background-color: #eef1f6;
  }
</style>
