<template>
  <el-card class="history-card">
    <el-table :data="papers" stripe v-loading="loading">
      <el-table-column type="expand">
        <template v-slot="props">
          <el-form label-position="left" inline class="paper-details">
            <el-form-item label="选择题得分">
              <span>{{ props.row.choiceScore || 0 }}</span>
            </el-form-item>
            <el-form-item label="主观题得分">
              <span>{{ props.row.subjectiveScore || 0 }}</span>
            </el-form-item>
          </el-form>
        </template>
      </el-table-column>
      <el-table-column prop="testName" label="考试名称"></el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="200"></el-table-column>
      <el-table-column prop="totalScore" label="我的总分" width="120"></el-table-column>
      <el-table-column label="操作" width="140">
        <template v-slot="scope">
          <el-button type="primary" size="mini" @click="viewPaper(scope.row)">查看试卷</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
export default {
  name: 'HistoricalPapers',
  data() {
    return {
      papers: [],
      loading: false
    };
  },
  methods: {
    normalizeStudentNo(v) {
      const raw = String(v || '').trim();
      if (!raw) return '';
      const idx = raw.indexOf(':');
      return idx > -1 ? raw.slice(idx + 1).trim() : raw;
    },
    async fetchHistoryPapers() {
      const username = (typeof window !== 'undefined' && sessionStorage)
        ? this.normalizeStudentNo(sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username'))
        : null;
      if (!username) {
        this.$message.warning('请先登录');
        return;
      }
      this.loading = true;
      try {
        const res = await this.$axios.get(`/backage-api/paper/student/history/${encodeURIComponent(username)}`);
        this.loading = false;
        if (res && res.data && res.data.code === '0') {
          const rows = Array.isArray(res.data.data) ? res.data.data : [];
          this.papers = rows.map(item => ({
            ...item,
            totalScore: item.totalScore != null ? item.totalScore : (item.score || 0)
          }));
        } else {
          this.papers = [];
          this.$message.error((res && res.data && res.data.message) || '获取历次试卷失败');
        }
      } catch (err) {
        this.loading = false;
        console.error('fetchHistoryPapers error', err);
        if (err && err.response && err.response.status === 404) {
          this.$message.warning('未找到历次试卷（后端接口可能不可用）');
        } else {
          this.$message.error('获取历次试卷失败，请检查网络或后端');
        }
        this.papers = [];
      }
    },

    viewPaper(row) {
      this.$alert(
        `考试：${row.testName || ''}\n总分：${row.totalScore || 0}\n客观题：${row.choiceScore || 0}\n主观题：${row.subjectiveScore || 0}`,
        '试卷结果',
        { confirmButtonText: '知道了' }
      );
    }
  },
  mounted() {
    this.fetchHistoryPapers();
  }
};
</script>

<style scoped>
.history-card {
  min-height: calc(100vh - 90px);
}
.paper-details {
  padding: 10px 20px;
  background-color: #f4f4f5;
}
</style>
