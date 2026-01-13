<template>
  <div class="main-page-wrapper">
    <el-container class="app-wrapper">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar-container">
      <div class="logo">
        <span class="logo-title">ONLINEEXAM</span>
        <span class="logo-tag">STUDENT</span>
      </div>
      <el-menu
        :default-openeds="['info', '1', '2']"
        :default-active="activeIndex"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleSelect"
        class="el-menu-vertical"
      >
        <!-- 信息中心 -->
        <el-submenu index="info">
          <template #title>
            <i class="el-icon-bell"></i>
            <span>信息中心</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="info-notices">
              <span>通知中心</span>
              <el-badge v-if="unreadNotice > 0" :value="unreadNotice" :max="99" class="menu-badge" />
            </el-menu-item>
            <el-menu-item index="info-rules">
              <span>考试规则</span>
              <el-badge v-if="unreadRule > 0" :value="unreadRule" :max="99" class="menu-badge" />
            </el-menu-item>
          </el-menu-item-group>
        </el-submenu>

        <!-- 考试中心 -->
        <el-submenu index="1">
          <template #title>
            <i class="el-icon-date"></i>
            <span>考试中心</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="1-1">我的考试</el-menu-item>
            <el-menu-item index="1-5">我的试卷</el-menu-item>
            <el-menu-item index="1-6">我的错题</el-menu-item>
          </el-menu-item-group>
        </el-submenu>

        <!-- 个人中心 -->
        <el-submenu index="2">
          <template #title>
            <i class="el-icon-s-custom"></i>
            <span>个人中心</span>
          </template>
          <el-menu-item-group>
            <el-menu-item index="1-2">
              <i class="el-icon-user"></i>
              个人信息
            </el-menu-item>
            <el-menu-item index="1-3">
              <i class="el-icon-document"></i>
              成绩查询
            </el-menu-item>
          </el-menu-item-group>
        </el-submenu>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <el-header class="navbar">
        <div class="right-menu">
          <div class="user-panel-top">
            <el-upload
              class="top-upload"
              :action="backageBase + '/profile/uploadAvatar'"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :before-upload="beforeUpload"
              :headers="uploadHeaders"
              :disabled="!backendAvailable"
            >
              <img
                v-if="avatarUrl"
                :src="avatarUrl"
                class="top-avatar-img"
                :alt="username"
              />
              <div v-else class="top-avatar">{{ avatarText }}</div>
            </el-upload>

            <div class="top-user-text">
              <div class="top-name-large">{{ displayUserNo }}</div>
              <div class="top-id-role">{{ displayNameWithTitle }}</div>
            </div>

            <el-dropdown
              trigger="click"
              placement="bottom-start"
              class="logout-dropdown"
              @command="handleUserCommand"
            >
              <span class="el-dropdown-link">
                <i class="el-icon-arrow-down dropdown-caret"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <div class="shell-divider"></div>

      <el-main class="app-main">
        <transition name="fade-transform" mode="out-in">
          <div class="main-content">
            <router-view></router-view>
          </div>
        </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { normalizeRoleValue, deriveRoleIdFromUsername } from '@/utils/role';
export default {
  name: "StudentMain",
  data() {
    return {
      username:
        (typeof window !== "undefined" && window.sessionStorage
          ? (window.sessionStorage.getItem("USERNAME") || window.sessionStorage.getItem("username"))
          : null) || "学生",
      activeIndex: "1-1",
      avatarUrl: "",
      backendAvailable: true,
      isCollapse: false,
      noticeWs: null,
      unreadNotice: Number((typeof window !== 'undefined' && window.sessionStorage) ? (window.sessionStorage.getItem('UNREAD_NOTICE') || 0) : 0),
      unreadRule: Number((typeof window !== 'undefined' && window.sessionStorage) ? (window.sessionStorage.getItem('UNREAD_RULE') || 0) : 0),
    };
  },
  computed: {
    displayUserNo() {
      return (
        sessionStorage.getItem('USERNAME') ||
        sessionStorage.getItem('username') ||
        this.username ||
        ''
      );
    },
    displayNameWithTitle() {
      const name =
        sessionStorage.getItem('displayName') ||
        sessionStorage.getItem('realName') ||
        sessionStorage.getItem('name') ||
        this.displayUserNo ||
        '';
      let roleId =
        sessionStorage.getItem('serverRoleId') ||
        sessionStorage.getItem('roleId') ||
        sessionStorage.getItem('flag');
      if (!roleId) roleId = deriveRoleIdFromUsername(this.displayUserNo);
      const roleMap = { 0: '管理员', 1: '学生', 2: '教师' };
      const roleLabel = roleMap[String(roleId)] || '学生';
      return `${name} ${roleLabel}`.trim();
    },
    shortName() {
      const s = this.username || "";
      if (!s) return "";
      if (/^\d+$/.test(s)) return s.slice(-4);
      return s.slice(0, 1).toUpperCase();
    },
    roleLabel() {
      const raw = (() => {
        try {
          return window.sessionStorage.getItem('roleId') || window.sessionStorage.getItem('flag');
        } catch (e) {
          return null;
        }
      })();
      const norm = normalizeRoleValue(raw);
      const map = { '0': "管理员", '1': "学生", '2': "教师" };
      if (norm !== null) return map[String(norm)];
      const derived = deriveRoleIdFromUsername(this.username);
      return map[String(derived)] || '学生';
    },
    displayNameLarge() {
      // show real name if available otherwise username
      const name = (typeof window !== 'undefined' && window.sessionStorage) ? (window.sessionStorage.getItem('realName') || window.sessionStorage.getItem('displayName') || window.sessionStorage.getItem('USERNAME')) : this.username;
      // Prefer showing name, if not present show username
      return name || this.username || '未命名';
    },
    avatarText() {
      return this.shortName || "U";
    },
    backageBase() {
      // Prefer a runtime-injected global BACKAGE_BASE, else fallback to proxy path used in dev
      if (typeof window !== 'undefined' && window.__BACKAGE_BASE__) return window.__BACKAGE_BASE__;
      // try to read from a global variable injected by api module (if present)
      if (typeof window !== 'undefined' && window.BACKAGE_BASE) return window.BACKAGE_BASE;
      // default to proxied path used in this project
      return '/backage-api';
    },
    uploadHeaders() {
      try {
        if (typeof window === "undefined" || !window.sessionStorage) return { AUTHORIZATION: "" };
        return { AUTHORIZATION: window.sessionStorage.getItem("AUTHORIZATION") || "" };
      } catch (e) {
        return { AUTHORIZATION: "" };
      }
    },
  },
  methods: {
    normalizeStudentNo(v) {
      const raw = String(v || '').trim();
      if (!raw) return '';
      const idx = raw.indexOf(':');
      return idx > -1 ? raw.slice(idx + 1).trim() : raw;
    },
    navigateTo(path) {
      if (this.$route && this.$route.path === path) {
        return;
      }
      this.$router.push(path).catch(err => {
        if (!err || err.name !== 'NavigationDuplicated') {
          throw err;
        }
      });
    },
    handleSelect(key) {
      if (key === 'info-notices') {
        this.unreadNotice = 0;
        try { sessionStorage.setItem('UNREAD_NOTICE', '0'); } catch (e) {}
      }
      if (key === 'info-rules') {
        this.unreadRule = 0;
        try { sessionStorage.setItem('UNREAD_RULE', '0'); } catch (e) {}
      }
      switch (key) {
        case 'info-notices':
          this.navigateTo('/app/info/notices')
          break
        case 'info-rules':
          this.navigateTo('/app/info/rules')
          break
        case '1-1':
          this.navigateTo('/app/tests');
          break;
        case '1-5':
          this.navigateTo('/app/papers');
          break;
        case '1-6':
          this.navigateTo('/app/wrong-questions');
          break;
        case '1-2':
          this.navigateTo('/app/profile');
          break;
        case '1-3':
          this.navigateTo('/app/scores');
          break;
      }
    },
    handleUserCommand(command) {
      if (command === 'profile') {
        this.openProfile();
        return;
      }
      if (command === 'logout') {
        this.logout();
      }
    },
    logout() {
      try {
        const auth = typeof window !== 'undefined' && window.sessionStorage ? window.sessionStorage.getItem('AUTHORIZATION') : '';
        if (auth) {
          this.$axios.post(this.backageBase + '/profile/logout', null, { headers: { AUTHORIZATION: auth } }).catch(()=>{});
        }
      } catch(e){}
      try { sessionStorage.clear(); } catch(e){}
      // hard redirect to root to ensure clean state
      // go to student login
      window.location.href = '/';
    },
    openProfile(){ this.navigateTo('/app/profile') },
    onUploadSuccess() {
      // After successful upload, reload avatar from backend
      this.loadAvatar();
    },
    beforeUpload(file) {
      const isImage = /image\/(jpeg|png|gif)/.test(file.type);
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImage) this.$message.error('上传头像只能是 JPG/PNG/GIF 格式!');
      if (!isLt2M) this.$message.error('上传头像大小不能超过 2MB!');
      return isImage && isLt2M;
    },
    async loadAvatar() {
      // Build avatar URL; prefer session student id if present
      let uname = (typeof window !== 'undefined' && window.sessionStorage) ? (window.sessionStorage.getItem('USERNAME') || window.sessionStorage.getItem('username')) : this.username;
      uname = this.normalizeStudentNo(uname);
      if (!uname) return;
      // Backend endpoint: GET {backageBase}/profile/avatar/{username}
      const url = `${this.backageBase}/profile/avatar/${encodeURIComponent(uname)}`;
      try {
        // Try to fetch as blob first (some backends require auth header)
        const headers = {};
        try { const t = window.sessionStorage.getItem('AUTHORIZATION'); if (t) headers.AUTHORIZATION = t; } catch (e) {}
        const resp = await this.$axios.get(url, { responseType: 'arraybuffer', headers });
        if (resp && resp.status === 200) {
          const contentType = resp.headers['content-type'] || 'image/png';
          const base64 = btoa(new Uint8Array(resp.data).reduce((data, byte) => data + String.fromCharCode(byte), ''));
          this.avatarUrl = `data:${contentType};base64,${base64}`;
        }
      } catch (e) {
        // If binary fetch failed (404 or CORS), fallback to direct URL so browser may load it
        // Note: if backend not proxied or secured, this may still 404; we tolerate it.
        try {
          this.avatarUrl = `${this.backageBase}/profile/avatar/${encodeURIComponent(uname)}`;
        } catch (e2) {
          this.avatarUrl = '';
        }
      }
    },
    connectNoticeSocket() {
      try {
        if (this.noticeWs) {
          this.noticeWs.close();
          this.noticeWs = null;
        }
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsHost = window.location.host;
        this.noticeWs = new WebSocket(`${protocol}//${wsHost}/backage-api/ws/notice/student`);
        this.noticeWs.onmessage = (evt) => {
          try {
            const payload = JSON.parse(evt.data || '{}');
            const action = payload.action || 'create';
            const msg = payload.notice || payload;
            if (!msg || !msg.id) return;
            if (action === 'delete') {
              return;
            }
            if (String(msg.targetRole) === '9') {
              this.unreadRule += 1;
              sessionStorage.setItem('UNREAD_RULE', String(this.unreadRule));
              this.$notify({ title: '考试规则更新', message: msg.title || '有新的考试规则', type: 'warning', duration: 3000 });
            } else {
              this.unreadNotice += 1;
              sessionStorage.setItem('UNREAD_NOTICE', String(this.unreadNotice));
              this.$notify({ title: '新通知', message: msg.title || '您收到一条通知', type: 'info', duration: 3000 });
            }
          } catch (e) {}
        };
      } catch (e) {}
    },
  },
  mounted() {
    // initialize username from session if backend provides
    if (typeof window !== 'undefined' && window.sessionStorage) {
      const u = this.normalizeStudentNo(window.sessionStorage.getItem('USERNAME') || window.sessionStorage.getItem('username'));
      if (u) this.username = u;
    }

    this.connectNoticeSocket();

    // probe backend
    const auth = typeof window !== 'undefined' && window.sessionStorage ? window.sessionStorage.getItem('AUTHORIZATION') : '';
    if (auth) {
      this.$axios
        .get(this.backageBase + '/profile/me', { params: { username: this.username }, headers: { AUTHORIZATION: auth } })
        .then((res) => {
          if (res.data && res.data.code === '0') {
            const d = res.data.data || {};
            if (d.username) {
              this.username = d.username;
              if (typeof window !== 'undefined' && window.sessionStorage)
                window.sessionStorage.setItem('USERNAME', d.username);
            }
            if (d.hasAvatar) {
              this.loadAvatar();
            }
            this.backendAvailable = true;
            // prefer backend role if provided
            if (d.roleId || d.flag) {
              if (typeof window !== 'undefined' && window.sessionStorage) {
                window.sessionStorage.setItem('roleId', String(d.roleId || d.flag));
              }
            }
          } else {
            this.backendAvailable = false;
          }
        })
        .catch(() => {
          this.backendAvailable = false;
        });
    } else {
      this.backendAvailable = false;
    }
    // Ensure roleId is available in sessionStorage for other components
    const currentRole = sessionStorage.getItem('roleId') || sessionStorage.getItem('flag');
    if (!currentRole) {
      const uname = this.normalizeStudentNo(sessionStorage.getItem('username') || this.username);
      const derived = deriveRoleIdFromUsername(uname);
      sessionStorage.setItem('roleId', String(derived));
    }
  },
  beforeDestroy() {
    if (this.noticeWs) {
      this.noticeWs.close();
      this.noticeWs = null;
    }
  },
};
</script>

<style scoped>
.main-page-wrapper {
  min-height: 100vh;
  overflow: hidden;
}

.app-wrapper {
  height: 100vh;
  width: 100%;
  min-width: 1360px;
  overflow: hidden;
}

.sidebar-container {
  background-color: #304156;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.logo {
  height: 50px;
  line-height: 50px;
  text-align: center;
  color: #fff;
  font-weight: 600;
  font-size: 20px;
  background-color: #2b2f3a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.logo-tag { font-size: 12px; padding: 2px 6px; border-radius: 10px; border: 1px solid rgba(255,255,255,0.6); }
.el-menu {
  border: none;
}

.main-container {
  min-height: 100%;
  min-width: 0;
  transition: margin-left 0.28s;
  background-color: #f0f2f5;
  overflow: hidden;
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 16px;
  flex-shrink: 0;
}

.right-menu {
  padding-right: 16px;
}

.shell-divider {
  height: 14px;
  background: linear-gradient(180deg, rgba(48, 65, 86, 0.08) 0%, rgba(240, 242, 245, 0.96) 100%);
  border-top: 1px solid rgba(48, 65, 86, 0.12);
  border-bottom: 1px solid rgba(255, 255, 255, 0.75);
  flex-shrink: 0;
}

.user-panel-top {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.top-upload {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.top-avatar,
.top-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.top-avatar {
  background-color: #409eff;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
}

.top-user-text {
  margin: 0 8px;
  overflow: hidden;
}

.top-name-large {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.top-id-role {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.logout-dropdown {
  display: flex;
  align-items: center;
}

.dropdown-caret {
  color: #555;
  font-size: 16px;
  cursor: pointer;
}

.app-main {
  padding: 20px;
  position: relative;
  overflow: auto;
  min-width: 0;
}

.main-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100vh - 104px);
  min-width: 1120px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
