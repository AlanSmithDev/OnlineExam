<template>
  <div class="login-container">
    <el-card class="login-card">
      <div slot="header" class="clearfix">
        <span class="login-title">在线考试系统后台管理</span>
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
            placeholder="请输入用户名"
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
          {
            pattern: /^\d{12}$/,
            message: "账号需为12位数字",
            trigger: "blur",
          },
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
      loading: false,
    };
  },
  created() {
    this.loadDefaultUsername();
  },
  methods: {
    async loadDefaultUsername() {
      try {
        const res = await this.$axios.get("/auth-api/auth/user/first", {
          params: { portal: "admin" },
        });
        if (res && res.data && res.data.code === "0" && res.data.data) {
          const user = res.data.data;
          this.loginForm.username = user.username || "";
          return;
        }
      } catch (e) {
        console.warn("fetch default user failed via proxy", e && e.message);
      }
      // 不再写死默认管理员账号，避免覆盖教师登录
      this.loginForm.username = this.loginForm.username || "";
    },

    // Local fallback: robustly normalize various role representations to '0'|'1'|'2' or null
    normalizeRoleValue(raw) {
      if (raw === null || raw === undefined) return null;
      // if array, try first meaningful element
      if (Array.isArray(raw) && raw.length > 0) {
        for (const item of raw) {
          const v = this.normalizeRoleValue(item);
          if (v) return v;
        }
        return null;
      }
      // object: inspect common fields
      if (typeof raw === "object") {
        const candidates = [
          "roleId",
          "flag",
          "role",
          "role_id",
          "roleName",
          "role_name",
          "type",
          "authority",
          "authorities",
          "roles",
          "roleNameZh",
          "roleNameCN",
        ];
        for (const k of candidates) {
          if (Object.prototype.hasOwnProperty.call(raw, k) && raw[k] != null) {
            const v = raw[k];
            const nv = this.normalizeRoleValue(v);
            if (nv) return nv;
          }
        }
        // also check if object contains readable name fields
        if (raw.role_name || raw.roleName || raw.name) {
          return this.normalizeRoleValue(
            String(raw.role_name || raw.roleName || raw.name)
          );
        }
        return null;
      }
      // primitive: string or number
      const s = String(raw).trim();
      if (s === "") return null;
      // common textual roles
      const lower = s.toLowerCase();
      if (
        lower === "管理员" ||
        lower === "admin" ||
        lower === "administrator" ||
        lower === "教务处" ||
        lower === "0" ||
        lower === "00"
      )
        return "0";
      if (lower === "教师" || lower === "teacher" || lower === "2") return "2";
      if (lower === "学生" || lower === "student" || lower === "1") return "1";
      // numeric strings
      if (/^\d+$/.test(s)) {
        if (s === "0" || s === "00") return "0";
        if (s === "1") return "1";
        if (s === "2") return "2";
      }
      // role names in Chinese
      if (lower.indexOf("管理员") !== -1 || lower.indexOf("管理") !== -1)
        return "0";
      if (lower.indexOf("教师") !== -1 || lower.indexOf("老师") !== -1)
        return "2";
      if (lower.indexOf("学生") !== -1) return "1";
      return null;
    },

    // 根据账号推导角色
    deriveRoleIdFromUsername(username) {
      if (!username || typeof username !== "string") return null;
      const s = username.trim();
      const digits = s.replace(/\D/g, ""); // 只保留数字

      console.log(
        "[deriveRoleIdFromUsername] 原始账号:",
        username,
        "纯数字:",
        digits,
        "长度:",
        digits.length
      );

      // 检查第5-6位（角色编码）
      // 12位编号规则: YYYY RR CC MM SS
      // RR = 10 管理员, 20 教师, 30 学生
      if (digits.length >= 6) {
        const roleSeg = digits.substring(4, 6); // 第5-6位
        console.log("[deriveRoleIdFromUsername] 第5-6位角色编码:", roleSeg);
        if (roleSeg === "10") {
          console.log("[deriveRoleIdFromUsername] 识别为管理员");
          return "0"; // 管理员
        }
        if (roleSeg === "20") {
          console.log("[deriveRoleIdFromUsername] 识别为教师");
          return "2"; // 教师
        }
        if (roleSeg === "30") {
          console.log("[deriveRoleIdFromUsername] 识别为学生");
          return "1"; // 学生
        }
      }

      console.log("[deriveRoleIdFromUsername] 无法推导角色，返回null");
      return null;
    },

    async submit() {
      // wrap validate in a Promise to use await
      const valid = await new Promise((resolve) => {
        this.$refs.loginForm.validate((v) => resolve(v));
      });

      if (!valid) return;

      // Clear previous auth/role data to avoid stale values causing misclassification
      try {
        sessionStorage.removeItem("roleId");
        sessionStorage.removeItem("flag");
        sessionStorage.removeItem("serverRoleId");
        sessionStorage.removeItem("AUTHORIZATION");
        sessionStorage.removeItem("teacherNo");
        sessionStorage.removeItem("username");
        sessionStorage.removeItem("USERNAME");
        sessionStorage.removeItem("displayName");
        sessionStorage.removeItem("realName");
        sessionStorage.removeItem("name");
      } catch (e) {
        /* ignore */
      }

      this.loading = true;
      try {
        const postData = this.$qs.stringify({
          username: this.loginForm.username,
          password: this.loginForm.password,
        });
        console.debug("Attempt login for", this.loginForm.username);

        let res;
        try {
          res = await this.$axios.post("/auth-api/auth/login", postData);
        } catch (proxyErr) {
          const status =
            proxyErr && proxyErr.response && proxyErr.response.status;
          if (status === 404 || !proxyErr.response) {
            // 代理不可用时直连 auth 服务
            res = await this.$axios.post("/auth-api/auth/login", postData, {
              headers: { "Content-Type": "application/x-www-form-urlencoded" },
            });
          } else {
            throw proxyErr;
          }
        }
        this.loading = false;

        if (!(res && res.status === 200 && res.data && res.data.code === "0")) {
          this.$message.error(
            (res && res.data && res.data.message) || "登录失败"
          );
          return;
        }

        let data = res.data.data || {};
        const rawUsername = this.loginForm.username;

        // store token if returned (string or object.token)
        let token = null;
        if (typeof data === "string") {
          token = data;
        } else if (typeof data === "object" && data.token) {
          token = data.token;
        }
        if (token) {
          sessionStorage.setItem("AUTHORIZATION", token);
          // 同时兼容 auth 服务的 Authorization 头和业务服务的 AUTHORIZATION 头
          this.$axios.defaults.headers.common["AUTHORIZATION"] = token;
          this.$axios.defaults.headers.common["Authorization"] = token;
        }

        // 仅做资料补充，不再依赖 auth-api/profile/me，避免出现 no-session 误判
        let profile = null;
        try {
          const profileRes = await this.$axios.get("/backage-api/profile/me", {
            params: { username: rawUsername },
          });
          if (profileRes && profileRes.data && profileRes.data.code === "0") {
            profile = profileRes.data.data || null;
          } else {
            console.warn(
              "profile fetch returned non-ok",
              profileRes && profileRes.data
            );
          }
        } catch (err) {
          console.warn("fetch profile failed after login", err);
        }

        // 优先保留登录接口返回的角色信息，仅用 profile 补充姓名等资料
        const authoritative = typeof data === "object" ? data : null;
        const mergedProfile = Object.assign({}, authoritative || {}, profile || {});

        // store username/displayName if provided
        const storeName =
          mergedProfile && mergedProfile.username
            ? mergedProfile.username
            : rawUsername;
        sessionStorage.setItem("username", storeName);
        sessionStorage.setItem("USERNAME", storeName);
        if (mergedProfile && mergedProfile.name) {
          sessionStorage.setItem("displayName", mergedProfile.name);
          sessionStorage.setItem("realName", mergedProfile.name);
          sessionStorage.setItem("name", mergedProfile.name);
        }

        // extract role info
        function extractRoleRaw(obj) {
          if (!obj) return null;
          const keys = [
            "roleId",
            "flag",
            "role",
            "role_id",
            "roleName",
            "role_name",
            "type",
            "authority",
            "authorities",
            "roles",
          ];
          for (const k of keys) {
            if (
              Object.prototype.hasOwnProperty.call(obj, k) &&
              obj[k] !== undefined &&
              obj[k] !== null
            ) {
              return obj[k];
            }
          }
          // fallback: maybe server returns an array of authorities
          if (Array.isArray(obj) && obj.length > 0) return obj[0];
          return null;
        }

        const returnedRaw = extractRoleRaw(authoritative);
        let returnedRole = null;
        try {
          returnedRole = this.normalizeRoleValue(returnedRaw);
        } catch (e) {
          console.warn("normalizeRoleValue failed", e);
          returnedRole = null;
        }

        // derived role from username using shared util (returns '0'|'2'|'1' or null)
        const derivedRole = this.deriveRoleIdFromUsername(rawUsername);

        console.debug("Login response roles:", {
          returnedRaw,
          returnedRole,
          derivedRole,
          authoritative,
        });

        console.log(
          "角色判断 - 服务器角色:",
          returnedRole,
          "推导角色:",
          derivedRole,
          "账号:",
          rawUsername
        );

        // 登录角色以 SQL 编号规则为准：如果服务器返回角色与编号推导不一致，优先使用编号规则
        let finalRole = derivedRole || returnedRole;

        if (returnedRole && derivedRole && String(returnedRole) !== String(derivedRole)) {
          console.warn(
            "服务器角色与编号规则不一致，按编号规则修正角色:",
            returnedRole,
            "->",
            derivedRole,
            "账号:",
            rawUsername
          );
          finalRole = derivedRole;
        }

        // 如果服务器返回学生角色，检查账号是否真的是学生
        if (finalRole === "1") {
          // 检查账号是否是真正的学生账号（30开头）
          const digits = rawUsername.replace(/\D/g, "");
          if (
            digits.startsWith("10") ||
            digits.startsWith("11") ||
            digits.startsWith("20")
          ) {
            // 账号是管理员或教师，但服务器返回学生角色，使用推导的角色
            finalRole = derivedRole;
            console.log(
              "服务器返回学生角色，但账号是管理员/教师，使用推导角色:",
              finalRole
            );
          } else {
            // 确实是学生账号，拒绝登录
            this.$message.error("该账号为学生账号，请使用学生端登录");
            sessionStorage.removeItem("AUTHORIZATION");
            return;
          }
        }

        // 如果服务器没有返回角色，使用推导的角色
        if (!finalRole) {
          finalRole = derivedRole;
        }

        // 如果仍然没有角色，根据账号前缀判断
        if (!finalRole) {
          const digits = rawUsername.replace(/\D/g, "");
          if (digits.startsWith("10") || digits.startsWith("11")) {
            finalRole = "0"; // 管理员
          } else if (digits.startsWith("20")) {
            finalRole = "2"; // 教师
          } else if (digits.startsWith("30")) {
            finalRole = "1"; // 学生
          } else {
            // 无法确定角色，拒绝登录
            this.$message.error("账号角色无法识别，请联系管理员");
            sessionStorage.removeItem("AUTHORIZATION");
            return;
          }
        }

        // 最终检查：如果是学生角色，拒绝登录管理端
        if (finalRole === "1") {
          this.$message.error("该账号为学生账号，请使用学生端登录");
          sessionStorage.removeItem("AUTHORIZATION");
          return;
        }

        // 记录最终角色
        sessionStorage.setItem("serverRoleId", String(finalRole));
        sessionStorage.setItem("roleId", String(finalRole));
        sessionStorage.setItem("flag", String(finalRole));

        console.log("Final role set to:", finalRole);

        // 管理员查看全部考试，教师按自己的工号查询
        sessionStorage.setItem(
          "teacherNo",
          finalRole === "0" ? "all" : rawUsername
        );

        this.$message.success("登录成功");

        // 跳转逻辑：管理员跳转到教师管理页，教师跳转到考试列表页
        console.log("跳转角色:", finalRole);

        if (finalRole === "0") {
          // 管理员跳转到教师管理页
          this.$router.push("/main/teacherManagement");
        } else if (finalRole === "2") {
          // 教师跳转到考试列表页
          this.$router.push("/main/tests");
        } else {
          // 其他情况默认跳转到考试列表页
          this.$router.push("/main/tests");
        }
      } catch (err) {
        this.loading = false;
        console.error(err);
        this.$message.error("系统错误，请稍后重试");
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
