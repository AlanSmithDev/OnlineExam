<template>
  <div class="exam-rules">
    <el-card>
      <div class="title-row">
        <h3>考试规则</h3>
        <el-button size="mini" icon="el-icon-refresh" @click="loadRules" :loading="loading">刷新</el-button>
      </div>
      <el-divider></el-divider>

      <el-empty v-if="!loading && rules.length === 0" description="暂无考试规则" />

      <el-timeline v-else>
        <el-timeline-item
          v-for="item in rules"
          :key="item.id"
          :timestamp="item.time"
        >
          <h4>{{ item.title }}</h4>
          <p class="rule-content">{{ item.content }}</p>
          <p class="publisher">发布人：{{ item.publisher }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script>
import { fetchExamRules } from '@/api/api'

export default {
  name: 'ExamRules',
  data() {
    return {
      loading: false,
      rules: [],
      ws: null
    }
  },
  mounted() {
    this.loadRules()
    this.connectSocket()
  },
  beforeDestroy() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  },
  methods: {
    connectSocket() {
      try {
        const wsHost = window.location.host
        this.ws = new WebSocket(`${protocol}//${wsHost}/backage-api/ws/notice/student`)
        this.ws.onmessage = (evt) => {
          try {
            const payload = JSON.parse(evt.data || '{}')
            const action = payload.action || 'create'
            const msg = payload.notice || payload
            if (!msg || !msg.id || String(msg.targetRole) !== '9') return
            if (action === 'delete') {
              this.rules = this.rules.filter(x => x.id !== msg.id)
              return
            }
            this.rules = [{
              id: msg.id,
              title: msg.title || '考试规则',
              content: msg.content || '',
              time: msg.createTime || '',
              publisher: msg.publisherName || msg.publisherNo || '系统'
            }, ...this.rules.filter(x => x.id !== msg.id)]
          } catch (e) {}
        }
      } catch (e) {}
    },
    async loadRules() {
      this.loading = true
      try {
        const res = await fetchExamRules(20)
        if (res && res.data && res.data.code === '0') {
          const list = Array.isArray(res.data.data) ? res.data.data : []
          this.rules = list.map(item => ({
            id: item.id,
            title: item.title || '考试规则',
            content: item.content || '',
            time: item.createTime || '',
            publisher: item.publisherName || item.publisherNo || '系统'
          }))
        } else {
          this.rules = []
          this.$message.error((res && res.data && res.data.message) || '加载考试规则失败')
        }
      } catch (e) {
        this.rules = []
        this.$message.error('加载考试规则失败，请稍后重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.exam-rules h3 {
  margin: 0;
}
.title-row {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rule-content {
  line-height: 1.8;
  white-space: pre-wrap;
}
.publisher {
  color: #909399;
  font-size: 12px;
}
</style>
