<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="clearfix">
        <span class="login-title">在线考试系统登录</span>
      </div>
      <el-form
        :model="loginForm"
        :rules="rules"
        ref="loginForm"
        label-position="left"
        label-width="0px"
      >
        <el-form-item prop="username">
          <el-input
            type="text"
            v-model="loginForm.username"
            auto-complete="off"
            placeholder="请输入学号/账号"
            prefix-icon="el-icon-user"
          >
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            type="password"
            v-model="loginForm.password"
            auto-complete="off"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            @keyup.enter.native="submit"
          >
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            @click="submit"
            :loading="loading"
            >登录</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import qs from "qs"; // 引入 qs
import { normalizeRoleValue, deriveRoleIdFromUsername } from "@/utils/role";

export default {
  name: "Login",
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
      },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
      loading: false,
    };
  },
  created() {
    // 不自动覆盖用户输入账号，避免固定为演示账号
  },
  methods: {
    async loadDefaultUsername() {
      try {
        // Request student example username from auth service through proxy
        const res = await this.$axios.get("/auth-api/auth/user/first", {
          params: { portal: "student" },
        });
        if (res && res.data && res.data.code === "0" && res.data.data) {
          const user = res.data.data;
          this.loginForm.username = user.username || "202630010101";
          return;
        }
      } catch (e) {
        console.warn("fetch default user failed via proxy", e && e.message);
      }
      // Fallback to fixed student sample id
      this.loginForm.username = "202630010101";
    },

    async submit() {
      const valid = await new Promise((resolve) => {
        this.$refs.loginForm.validate((v) => resolve(v));
      });
      if (!valid) return;

      let username = String(this.loginForm.username || "").trim();
      const password = String(this.loginForm.password || "");

      if (!username) {
        this.$message.warning("用户名不能为空");
        return;
      }
      if (!password) {
        this.$message.warning("密码不能为空");
        return;
      }

      const digits = username.replace(/\D/g, "");
      if (digits.length < 6 || digits.length > 12) {
        this.$message.error("账号格式不正确：建议为12位数字的学号/工号");
        return;
      }
      username = digits;

      const postData = qs.stringify({ username, password });

      this.loading = true;
      try {
        const res = await this.$axios.post("/auth-api/auth/login", postData);

        if (!(res && res.status === 200 && res.data && res.data.code === "0")) {
          this.$message.error((res && res.data && res.data.message) || "登录失败");
          return;
        }

        const data = res.data.data || {};
        const token = data.token || data || "";
        if (token) {
          sessionStorage.setItem("AUTHORIZATION", token);
          if (this.$axios && this.$axios.defaults) {
            this.$axios.defaults.headers.common["Authorization"] = token;
          }
        }

        const usernameResp = username;
        sessionStorage.setItem("USERNAME", usernameResp);
        sessionStorage.setItem("username", usernameResp);

        const rawRole = data.roleId != null ? data.roleId : data.flag;
        const roleFromServer = normalizeRoleValue(rawRole);
        const derivedRole = deriveRoleIdFromUsername(username);

        // 仅当“服务端明确非学生”且“账号推导也不是学生”时才拦截
        if (
          (roleFromServer === "0" || roleFromServer === "2") &&
          derivedRole !== "1"
        ) {
          this.$message.error("该账号不是学生账号，请使用管理端登录");
          sessionStorage.removeItem("AUTHORIZATION");
          return;
        }

        // 若账号规则可推导为学生，则优先按学生放行，避免后端脏数据误判
        const finalRole = derivedRole === "1" ? "1" : (roleFromServer || derivedRole || "1");
        sessionStorage.setItem("roleId", String(finalRole));
        sessionStorage.setItem("flag", String(finalRole));

        this.$router.push({ path: "/app/tests", query: { username: usernameResp } });
      } catch (err) {
        console.error(err);
        this.$message.error("系统错误，请稍后重试");
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  margin: 0;
  padding: 0;
  background-color: #2d3a4b;
  background-image: linear-gradient(135deg, #1f2d3d, #324057);
}

.login-card {
  width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.login-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  display: block;
  text-align: center;
}
</style>
