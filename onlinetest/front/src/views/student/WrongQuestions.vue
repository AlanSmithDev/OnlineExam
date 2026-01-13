<template>
  <el-card v-loading="loading">
    <div v-if="wrongs.length === 0 && !loading" class="empty-state">暂无错题记录</div>
    <div v-for="(item, index) in wrongs" :key="index" class="wrong-question-item">
      <div class="question-header">
        <strong>题目{{ index + 1 }}:</strong>
        <span class="question-source">来源: {{ item.testName }}</span>
      </div>
      <div class="question-content" v-html="item.questionContent"></div>
      <div class="answer-section">
        <p><strong>你的答案:</strong> <span class="wrong-answer">{{ item.studentAnswer }}</span></p>
        <p><strong>正确答案:</strong> <span class="correct-answer">{{ item.correctAnswer }}</span></p>
        <p v-if="item.analysis"><strong>解析:</strong> <span v-html="item.analysis"></span></p>
      </div>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'WrongQuestions',
  data() {
    return {
      wrongs: [],
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
    async fetchWrongQuestions() {
      const username = (typeof window !== 'undefined' && sessionStorage)
        ? this.normalizeStudentNo(sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username'))
        : null;
      if (!username) {
        this.$message.warning('请先登录');
        return;
      }
      this.loading = true;
      try {
        const res = await this.$axios.get(`/backage-api/paper/student/wrong/${encodeURIComponent(username)}`);
        this.loading = false;
        if (res && res.data && res.data.code === '0') {
          const rows = Array.isArray(res.data.data) ? res.data.data : [];
          this.wrongs = rows.map(item => ({
            testName: item.testName || '',
            questionContent: item.questionContent || '',
            studentAnswer: item.studentAnswer || '未作答',
            correctAnswer: item.correctAnswer || '无',
            analysis: item.analysis || ''
          }));
        } else {
          this.wrongs = [];
          this.$message.error((res && res.data && res.data.message) || '获取错题失败');
        }
      } catch (err) {
        this.loading = false;
        console.error('fetchWrongQuestions error', err);
        if (err && err.response && err.response.status === 404) {
          this.$message.warning('未找到错题数据（后端可能无该接口）');
        } else {
          this.$message.error('获取错题失败，请检查网络或后端');
        }
        this.wrongs = [];
      }
    }
  },
  mounted() {
    this.fetchWrongQuestions();
  }
};
</script>

<style scoped>
.el-card {
  min-height: calc(100vh - 90px);
}
.wrong-question-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}
.question-header {
  margin-bottom: 10px;
  font-size: 16px;
}
.question-source {
  float: right;
  color: #909399;
  font-size: 12px;
}
.answer-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #dcdfe6;
}
.wrong-answer {
  color: #F56C6C;
  font-weight: bold;
}
.correct-answer {
  color: #67C23A;
  font-weight: bold;
}
.empty-state {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
