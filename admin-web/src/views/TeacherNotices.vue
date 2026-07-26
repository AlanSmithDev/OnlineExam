<template>
  <div class="teacher-notices-container">
    <el-card class="box-card">
      <div slot="header" class="header-row">
        <span>通知中心</span>
        <div class="header-actions">
          <el-button
            type="primary"
            size="mini"
            icon="el-icon-message"
            @click="openCreate('notice')"
            >发布通知</el-button
          >
          <el-button
            type="warning"
            size="mini"
            icon="el-icon-document"
            @click="openCreate('rule')"
            >发布考试规则</el-button
          >
          <el-button size="mini" icon="el-icon-refresh" @click="loadNotices"
            >刷新</el-button
          >
        </div>
      </div>

      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in activities"
          :key="activity.uniqueKey || `${activity.id || 'noid'}-${index}`"
          :timestamp="activity.timestamp"
          placement="top"
        >
          <el-card>
            <div class="item-title-row">
              <h4>{{ activity.title }}</h4>
              <div class="item-tags">
                <el-tag v-if="activity.isRule" type="warning" size="mini"
                  >考试规则</el-tag
                >
                <el-tag v-else type="info" size="mini">通知</el-tag>
              </div>
            </div>
            <p>{{ activity.content }}</p>
            <p class="footer-info">发布人：{{ activity.creator }}</p>
            <div class="item-actions">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="openEdit(activity)"
                >编辑</el-button
              >
              <el-button
                size="mini"
                type="text"
                class="danger"
                icon="el-icon-delete"
                @click="removeNotice(activity)"
                >删除</el-button
              >
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <div v-if="loading" class="loading-placeholder">
        <el-skeleton :loading="true" animated>
          <el-skeleton-item
            style="width: 100%; height: 20px"
          ></el-skeleton-item>
          <el-skeleton-item
            style="width: 100%; height: 20px"
          ></el-skeleton-item>
          <el-skeleton-item
            style="width: 100%; height: 20px"
          ></el-skeleton-item>
        </el-skeleton>
      </div>
    </el-card>

    <el-dialog
      :title="dialogTitle"
      :visible.sync="publishDialogVisible"
      width="560px"
    >
      <el-form
        :model="publishForm"
        :rules="publishRules"
        ref="publishForm"
        label-width="90px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="publishForm.title"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            type="textarea"
            :rows="6"
            v-model="publishForm.content"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="!publishForm.isRule" label="接收端">
          <el-select v-model="publishForm.targetRole" style="width: 100%">
            <el-option label="全体" :value="0" />
            <el-option label="学生" :value="1" />
            <el-option label="教师" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submitPublish"
          >保存</el-button
        >
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  fetchTeacherNotices,
  fetchExamRules,
  publishNotice,
  publishExamRule,
  updateNotice,
  deleteNotice,
} from "../api/api";

export default {
  name: "TeacherNotices",
  data() {
    return {
      activities: [],
      loading: false,
      publishDialogVisible: false,
      publishing: false,
      ws: null,
      publishForm: {
        id: null,
        title: "",
        content: "",
        targetRole: 1,
        isRule: false,
        mode: "create",
      },
      publishRules: {
        title: [{ required: true, message: "请输入标题", trigger: "blur" }],
        content: [{ required: true, message: "请输入内容", trigger: "blur" }],
      },
    };
  },
  computed: {
    dialogTitle() {
      const prefix = this.publishForm.mode === "edit" ? "编辑" : "发布";
      return this.publishForm.isRule ? `${prefix}考试规则` : `${prefix}通知`;
    },
  },
  mounted() {
    this.loadNotices();
    this.connectSocket();
  },
  beforeDestroy() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  },
  methods: {
    connectSocket() {
      try {
        const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const host = `${window.location.hostname}:8083`;
        this.ws = new WebSocket(
          `${protocol}//${host}/backage-api/ws/notice/teacher`
        );
        this.ws.onmessage = (evt) => {
          try {
            const payload = JSON.parse(evt.data || "{}");
            const action = payload.action || "create";
            const msg = payload.notice || payload;
            if (!msg || !msg.id) return;
            const next = this.toActivity(msg, String(msg.targetRole) === "9");
            if (action === "delete") {
              this.activities = this.activities.filter(
                (item) => item.uniqueKey !== next.uniqueKey
              );
              return;
            }
            this.activities = [
              next,
              ...this.activities.filter((item) => item.uniqueKey !== next.uniqueKey),
            ];
          } catch (e) {}
        };
      } catch (e) {}
    },
    toActivity(item, isRule = false) {
      const targetRole = item.targetRole === undefined || item.targetRole === null ? 0 : Number(item.targetRole);
      const rawTimestamp = item.createTime === undefined || item.createTime === null ? "" : item.createTime;
      const timestamp = String(rawTimestamp);
      const id = item.id;
      return {
        id,
        uniqueKey: `${isRule ? "rule" : "notice"}-${targetRole}-${id || timestamp || item.title || "item"}`,
        title: item.title,
        content: item.content,
        timestamp,
        creator: item.publisherName || item.creator || "系统",
        isRule,
        targetRole,
        publisherNo: item.publisherNo,
        publisherName: item.publisherName,
        publisherRoleName: item.publisherRoleName,
      };
    },
    async loadNotices() {
      this.loading = true;
      try {
        const [noticeRes, ruleRes] = await Promise.all([
          fetchTeacherNotices({ limit: 20 }).catch(() => null),
          fetchExamRules(20).catch(() => null),
        ]);
        const notices =
          noticeRes && noticeRes.data && noticeRes.data.code === "0"
            ? (noticeRes.data.data || []).map((item) =>
                this.toActivity(item, false)
              )
            : [];
        const rules =
          ruleRes && ruleRes.data && ruleRes.data.code === "0"
            ? (ruleRes.data.data || []).map((item) =>
                this.toActivity(item, true)
              )
            : [];

        this.activities = [...notices, ...rules]
          .filter((item) => item && item.uniqueKey)
          .reduce((acc, item) => {
            if (!acc.some((x) => x.uniqueKey === item.uniqueKey)) {
              acc.push(item);
            }
            return acc;
          }, [])
          .sort((a, b) => {
            const ta = new Date(a.timestamp || 0).getTime();
            const tb = new Date(b.timestamp || 0).getTime();
            return tb - ta;
          });
      } finally {
        this.loading = false;
      }
    },
    openCreate(type) {
      this.publishDialogVisible = true;
      this.publishForm = {
        id: null,
        title: "",
        content: "",
        targetRole: 1,
        isRule: type === "rule",
        mode: "create",
      };
      this.$nextTick(() => {
        this.$refs.publishForm && this.$refs.publishForm.clearValidate();
      });
    },
    openEdit(item) {
      this.publishDialogVisible = true;
      this.publishForm = {
        id: item.id,
        title: item.title,
        content: item.content,
        targetRole: item.targetRole || 0,
        isRule: !!item.isRule,
        mode: "edit",
        publisherNo: item.publisherNo,
        publisherName: item.publisherName,
        publisherRoleName: item.publisherRoleName,
      };
      this.$nextTick(() => {
        this.$refs.publishForm && this.$refs.publishForm.clearValidate();
      });
    },
    submitPublish() {
      this.$refs.publishForm.validate(async (valid) => {
        if (!valid) return;
        this.publishing = true;
        try {
          const username =
            sessionStorage.getItem("username") ||
            sessionStorage.getItem("USERNAME") ||
            "";
          const displayName =
            sessionStorage.getItem("displayName") ||
            sessionStorage.getItem("realName") ||
            username ||
            "管理员";
          const payload = {
            id: this.publishForm.id,
            title: this.publishForm.title,
            content: this.publishForm.content,
            targetRole: this.publishForm.isRule
              ? 9
              : this.publishForm.targetRole,
            publisherNo: this.publishForm.publisherNo || username,
            publisherName: this.publishForm.publisherName || displayName,
            publisherRoleName: this.publishForm.publisherRoleName || "管理员",
          };
          let res;
          if (this.publishForm.mode === "edit") {
            res = await updateNotice(payload);
          } else {
            res = await (this.publishForm.isRule
              ? publishExamRule(payload)
              : publishNotice(payload));
          }
          if (res && res.data && res.data.code === "0") {
            this.$message.success(
              this.publishForm.mode === "edit" ? "更新成功" : "发布成功"
            );
            this.publishDialogVisible = false;
            this.loadNotices();
            return;
          }
          this.$message.error(
            (res && res.data && res.data.message) || "操作失败"
          );
        } catch (e) {
          this.$message.error("操作失败，请稍后重试");
        } finally {
          this.publishing = false;
        }
      });
    },
    async removeNotice(item) {
      try {
        await this.$confirm("确认删除该条记录？", "提示", { type: "warning" });
      } catch (e) {
        return;
      }
      try {
        const res = await deleteNotice(item.id);
        if (res && res.data && res.data.code === "0") {
          this.$message.success("删除成功");
          this.activities = this.activities.filter((x) => x.uniqueKey !== item.uniqueKey);
          return;
        }
        this.$message.error(
          (res && res.data && res.data.message) || "删除失败"
        );
      } catch (e) {
        this.$message.error("删除失败，请稍后重试");
      }
    },
  },
};
</script>

<style scoped>
.teacher-notices-container {
  padding: 20px;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-actions > .el-button + .el-button {
  margin-left: 8px;
}
.item-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.item-tags {
  display: flex;
  gap: 8px;
}
.footer-info {
  text-align: right;
  color: #909399;
  font-size: 12px;
  margin-top: 10px;
}
.item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.item-actions .danger {
  color: #f56c6c;
}
.loading-placeholder {
  padding: 20px;
}
</style>
