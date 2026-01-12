<template>
  <div class="main-page-wrapper">
    <el-container class="app-wrapper">
      <!-- Sidebar -->
      <el-aside width="220px" class="sidebar-container">
        <div class="logo">
          <span class="logo-title" v-if="!isCollapse">ONLINEEXAM</span>
          <span class="logo-tag" v-if="!isCollapse">ADMIN</span>
        </div>
        <el-menu
          :default-openeds="['0', '1', '2', '3', '4']"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          @select="handleSelect"
          class="el-menu-vertical"
        >
          <el-submenu index="0">
            <template v-slot:title>
              <i class="el-icon-bell"></i>
              <span>信息中心</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="0-1">
                <i class="el-icon-message"></i>通知中心
              </el-menu-item>
              <el-menu-item index="0-2">
                <i class="el-icon-chat-line-round"></i>消息中心
              </el-menu-item>
            </el-menu-item-group>
          </el-submenu>

          <el-submenu index="1">
            <template v-slot:title>
              <i class="el-icon-s-cooperation"></i>
              <span>考试管理</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="1-1">
                <i class="el-icon-date"></i>所有考试
              </el-menu-item>
            </el-menu-item-group>
          </el-submenu>

          <el-submenu index="2">
            <template v-slot:title>
              <i class="el-icon-question"></i>
              <span>题目管理</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="2-1">选择题管理</el-menu-item>
              <el-menu-item index="2-2">主观题管理</el-menu-item>
              <el-menu-item index="2-3">题目上传</el-menu-item>
            </el-menu-item-group>
          </el-submenu>

          <el-submenu index="3">
            <template v-slot:title>
              <i class="el-icon-edit-outline"></i>
              <span>阅卷管理</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="3-1">批改试卷</el-menu-item>
            </el-menu-item-group>
          </el-submenu>

          <el-submenu index="4">
            <template v-slot:title>
              <i class="el-icon-s-data"></i>
              <span>数据分析</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="4-1">分数查询</el-menu-item>
            </el-menu-item-group>
          </el-submenu>

          <!-- 用户管理：学生列表对所有登录角色可见，教师列表仅管理员可见 -->
          <el-submenu index="5">
            <template v-slot:title>
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </template>
            <el-menu-item-group>
              <!-- 学生管理：对管理员和教师都可见 -->
              <el-menu-item index="5-1">学生列表</el-menu-item>
              <!-- 教师管理：仅管理员可见 -->
              <el-menu-item v-if="isAdmin" index="5-2">教师列表</el-menu-item>
            </el-menu-item-group>
          </el-submenu>
        </el-menu>
      </el-aside>

      <!-- Main Container -->
      <el-container class="main-container">
        <!-- Navbar -->
        <el-header class="navbar">
          <div class="right-menu">
            <div class="user-panel-top">
              <!-- 1. 头像上传 -->
              <el-upload
                class="top-upload"
                :action="BACKAGE_BASE + '/profile/uploadAvatar'"
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
                  alt="avatar"
                />
                <div v-else class="top-avatar">{{ avatarText }}</div>
              </el-upload>

              <!-- 2. 中间文字: 第一层显示编号，第二层显示姓名+职称 -->
              <div class="top-user-text">
                <div class="top-name-large">{{ displayUserNo }}</div>
                <div class="top-id-role">{{ displayNameWithTitle }}</div>
              </div>

              <!-- 3. 下拉箭头 -->
              <el-dropdown
                trigger="click"
                placement="bottom-start"
                class="logout-dropdown"
              >
                <span class="el-dropdown-link">
                  <i class="el-icon-arrow-down dropdown-caret"></i>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.native="openProfile"
                      >个人信息</el-dropdown-item
                    >
                    <el-dropdown-item @click.native="logout"
                      >退出登录</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <!-- App Main -->
        <el-main class="app-main">
          <transition name="fade-transform" mode="out-in">
            <div class="main-content">
              <router-view></router-view>
            </div>
          </transition>
        </el-main>
      </el-container>
    </el-container>

    <!-- Marking Dialog - Modified to show a table of questions -->
    <el-dialog
      title="主观题批改"
      :visible.sync="dialogMarkSubjectiveVisible"
      width="80%"
      @close="closeMarkDialog"
    >
      <el-card shadow="never" class="marking-card">
        <div class="marking-section">
          <div class="section-title">试卷题目</div>
          <!-- New Table for Questions -->
          <el-table :data="questionList" border style="width: 100%">
            <el-table-column
              prop="questionContent"
              label="题目"
              min-width="260"
            />
            <el-table-column
              prop="studentAnswer"
              label="学生答案"
              min-width="160"
            />
            <el-table-column
              prop="referenceAnswer"
              label="参考答案"
              min-width="160"
            />
            <el-table-column label="操作" align="center" width="150">
              <template v-slot="scope">
                <el-tag type="success" v-if="scope.row.subjectiveStatus === '1'"
                  >已批改</el-tag
                >
                <el-button
                  type="primary"
                  size="mini"
                  v-else
                  @click="markSubjective(scope.row.testNo)"
                  >批改</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </el-dialog>
  </div>
</template>

<script>
import { normalizeRoleValue, deriveRoleIdFromUsername } from "@/utils/role";
const BACKAGE_BASE = "/backage-api";
export default {
  name: "AdminMain",
  data() {
    return {
      username: sessionStorage.getItem("username") || "Admin",
      isCollapse: false,
      roleLabel: this.getRoleLabel(
        sessionStorage.getItem("roleId") || sessionStorage.getItem("flag")
      ),
      // avatarText computed below; keep avatarUrl only here
      avatarUrl: "",
      backendAvailable: true,
      dialogMarkSubjectiveVisible: false,
      questionList: [], // New data property for question list
    };
  },
  computed: {
    BACKAGE_BASE() {
      return BACKAGE_BASE;
    },
    displayUserNo() {
      return (
        sessionStorage.getItem("username") ||
        sessionStorage.getItem("USERNAME") ||
        this.username ||
        ""
      );
    },
    displayNameWithTitle() {
      const name =
        sessionStorage.getItem("displayName") ||
        sessionStorage.getItem("realName") ||
        sessionStorage.getItem("name") ||
        this.displayUserNo ||
        "";
      let roleId =
        sessionStorage.getItem("serverRoleId") ||
        sessionStorage.getItem("roleId") ||
        sessionStorage.getItem("flag");
      if (!roleId) roleId = deriveRoleIdFromUsername(this.displayUserNo);
      const roleMap = { 0: "管理员", 1: "学生", 2: "教师" };
      const roleLabel = roleMap[String(roleId)] || this.roleLabel || "教师";
      return `${name} ${roleLabel}`.trim();
    },
    displayNameTop() {
      return this.displayUserNo;
    },
    displayIdRole() {
      return this.displayNameWithTitle;
    },
    avatarText() {
      const s = this.username || "";
      if (!s) return "U";
      if (/^\d+$/.test(s)) return s.slice(-4);
      return s.slice(0, 1).toUpperCase();
    },
    uploadHeaders() {
      return {
        AUTHORIZATION:
          typeof window !== "undefined" && window.sessionStorage
            ? window.sessionStorage.getItem("AUTHORIZATION") || ""
            : "",
      };
    },
    isAdmin() {
      // 优先使用服务器返回的角色ID，然后使用存储的角色ID，最后从用户名推导
      const serverRid = sessionStorage.getItem("serverRoleId");
      const storedRid =
        sessionStorage.getItem("roleId") || sessionStorage.getItem("flag");
      let rid = serverRid || storedRid;

      // 如果没有存储的角色ID，尝试从用户名推导
      if (!rid) {
        const username = sessionStorage.getItem("username") || this.username;
        rid = deriveRoleIdFromUsername(username);
        console.log("从用户名推导角色:", username, "->", rid);
      }

      // 确保rid是字符串类型
      if (rid) {
        rid = String(rid);
      }

      const isAdmin = rid === "0";
      console.log("isAdmin check:", {
        serverRid,
        storedRid,
        finalRid: rid,
        isAdmin,
      });
      return isAdmin;
    },
    userDisplay() {
      // kept for compatibility but not used in template
      const uname = sessionStorage.getItem("username") || this.username || "";
      const name = sessionStorage.getItem("displayName") || "";
      let roleId =
        sessionStorage.getItem("roleId") || sessionStorage.getItem("flag");
      if (!roleId) roleId = deriveRoleIdFromUsername(uname);
      const roleMap = { 0: "管理员", 1: "学生", 2: "教师" };
      const roleLabel = roleMap[String(roleId)] || this.roleLabel || "教师";
      return `${uname} ${name} ${roleLabel}`.trim();
    },
  },
  methods: {
    handleSelect(key) {
      const routeMap = {
        "0-1": "/main/teacher/info/notices",
        "0-2": "/main/teacher/info/messages",
        "1-1": "/main/tests",
        "2-1": "/main/choiceManager",
        "2-2": "/main/subjectiveManager",
        "2-3": "/main/uploadQuestion",
        "3-1": "/main/markPaper",
        "4-1": "/main/studentScore",
        // 5-1 -> 学生管理 (所有角色可见)
        "5-1": "/main/studentManagement",
        // 5-2 -> 教师管理 (仅管理员可见)
        "5-2": "/main/teacherManagement",
      };
      const target = routeMap[key];
      if (!target) return;
      if (this.$route && this.$route.path === target) return;
      this.$router.push(target).catch(() => {});
    },
    logout() {
      sessionStorage.clear();
      this.$router.push("/");
    },
    openProfile() {
      // open a profile page or a modal (not implemented yet)
      this.$message.info("个人信息功能正在完善");
    },
    getRoleLabel(roleId) {
      if (roleId) {
        const roles = { 0: "管理员", 1: "学生", 2: "教师" };
        return roles[String(roleId)] || "未知职称";
      }
      const uname = sessionStorage.getItem("username") || this.username || "";
      const derived = deriveRoleIdFromUsername(uname);
      const roles = { 0: "管理员", 1: "学生", 2: "教师" };
      return roles[String(derived)] || "未知职称";
    },
    markSubjective(testNo) {
      // Fetch and show the list of questions for marking
      this.$axios
        .get(BACKAGE_BASE + "/paper/markDetail/" + testNo)
        .then((res) => {
          if (res.data && res.data.code === "0") {
            this.questionList = res.data.data || [];
            this.dialogMarkSubjectiveVisible = true;
          } else {
            this.$message.error("获取题目列表失败");
          }
        })
        .catch(() => {
          this.$message.error("获取题目列表失败");
        });
    },
    closeMarkDialog() {
      this.dialogMarkSubjectiveVisible = false;
      this.questionList = []; // Clear the question list on dialog close
    },
    onUploadSuccess() {
      // After successful upload, reload avatar from backend
      this.loadAvatar();
    },
    beforeUpload(file) {
      const isImage = /image\/(jpeg|png|gif)/.test(file.type);
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImage) this.$message.error("上传头像只能是 JPG/PNG/GIF 格式!");
      if (!isLt2M) this.$message.error("上传头像大小不能超过 2MB!");
      return isImage && isLt2M;
    },
  },
  mounted() {
    // If roleId missing in sessionStorage, try to derive from username and store it so other pages can use it
    const currentRole =
      sessionStorage.getItem("roleId") || sessionStorage.getItem("flag");
    if (!currentRole) {
      const uname = sessionStorage.getItem("username") || this.username;
      const derived = deriveRoleIdFromUsername(uname);
      if (derived) {
        sessionStorage.setItem("roleId", derived);
        console.log("从用户名推导出角色并存储:", derived, "用户名:", uname);
      } else {
        console.warn("无法从用户名推导角色:", uname);
      }
    } else {
      // 如果缓存角色与编号规则不一致，按编号规则修正，避免沿用上一次管理员缓存
      try {
        const uname = sessionStorage.getItem("username") || this.username;
        const derived = deriveRoleIdFromUsername(uname);
        const normStored = normalizeRoleValue(currentRole) || currentRole;
        if (derived && String(normStored) !== String(derived)) {
          console.warn(
            "Role mismatch detected: stored=",
            normStored,
            "derived=",
            derived,
            "username=",
            uname,
            "-> overwrite stored roleId by numbering rule"
          );
          sessionStorage.setItem("serverRoleId", String(derived));
          sessionStorage.setItem("roleId", String(derived));
          sessionStorage.setItem("flag", String(derived));
          sessionStorage.setItem("teacherNo", String(derived) === "0" ? "all" : uname);
        }
      } catch (e) {
        console.warn("role consistency check failed", e);
      }
    }

    // probe backend, set backendAvailable
    const auth =
      typeof window !== "undefined" && window.sessionStorage
        ? window.sessionStorage.getItem("AUTHORIZATION")
        : "";
    this.$axios
      .get(BACKAGE_BASE + "/profile/me", {
        headers: { AUTHORIZATION: auth, Authorization: auth },
        params: { username: sessionStorage.getItem("username") || this.username || "" },
      })
      .then((res) => {
        if (res.data && res.data.code === "0") {
          const d = res.data.data || {};
          if (d.username) {
            this.username = d.username;
            sessionStorage.setItem("username", d.username);
            sessionStorage.setItem("USERNAME", d.username);
          }
          if (d.name) {
            sessionStorage.setItem("displayName", d.name);
            sessionStorage.setItem("realName", d.name);
            sessionStorage.setItem("name", d.name);
          }
          if (d.roleId != null || d.username) {
            const derivedRole = deriveRoleIdFromUsername(d.username || this.username);
            const resolvedRole = derivedRole || String(d.roleId);
            if (resolvedRole != null) {
              sessionStorage.setItem("serverRoleId", String(resolvedRole));
              sessionStorage.setItem("roleId", String(resolvedRole));
              sessionStorage.setItem("flag", String(resolvedRole));
              sessionStorage.setItem("teacherNo", String(resolvedRole) === "0" ? "all" : (d.username || this.username));
            }
          }
          if (d.hasAvatar) this.loadAvatar();
          this.backendAvailable = true;
        } else {
          this.backendAvailable = false;
        }
      })
      .catch(() => {
        this.backendAvailable = false;
      });
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

.logo-title {
  letter-spacing: 1px;
}

.logo-tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  line-height: 1;
}

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
  padding: 0;
  flex-shrink: 0;
}

.right-menu {
  padding-right: 16px;
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
  min-height: calc(100vh - 90px);
  min-width: 1120px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* Transistion */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.5s;
}

.fade-transform-enter {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 顶部右侧用户信息区域（压缩 + 单行名字职称） */
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

/* Marking Dialog Styles */
.marking-card {
  padding: 20px;
}

.marking-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.section-content {
  font-size: 14px;
  color: #666;
}

.ref-answer {
  color: #409eff;
  font-weight: 500;
}
</style>
