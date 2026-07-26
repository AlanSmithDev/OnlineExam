<template>
  <div class="notices">
    <el-card>
      <div class="title-row">
        <h3>通知中心</h3>
        <el-button size="mini" icon="el-icon-refresh" @click="loadNotices" :loading="loading">刷新</el-button>
      </div>

      <el-empty v-if="!loading && noticeList.length === 0" description="暂无通知" />

      <el-timeline v-else>
        <el-timeline-item
          v-for="item in noticeList"
          :key="item.id"
          :timestamp="item.time"
        >
          <h4>{{ item.title }}</h4>
          <p>{{ item.content }}</p>
          <p class="publisher">发布人：{{ item.publisher }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script>
import { fetchStudentNotices } from '@/api/api'

export default {
  name: 'Notices',
  data() {
    return {
      loading: false,
      noticeList: [],
      ws: null
    }
  },
  mounted() {
    this.loadNotices()
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
            if (!msg || !msg.id || String(msg.targetRole) === '9') return
            if (action === 'delete') {
              this.noticeList = this.noticeList.filter(x => x.id !== msg.id)
              return
            }
            this.noticeList = [{
              id: msg.id,
              title: msg.title || '未命名通知',
              content: msg.content || '',
              time: msg.createTime || '',
              publisher: msg.publisherName || msg.publisherNo || '系统'
            }, ...this.noticeList.filter(x => x.id !== msg.id)]
          } catch (e) {}
        }
      } catch (e) {}
    },
    async loadNotices() {
      this.loading = true
      try {
        const res = await fetchStudentNotices(20)
        if (res && res.data && res.data.code === '0') {
          const list = Array.isArray(res.data.data) ? res.data.data : []
          this.noticeList = list.map(item => ({
            id: item.id,
            title: item.title || '未命名通知',
            content: item.content || '',
            time: item.createTime || '',
            publisher: item.publisherName || item.publisherNo || '系统'
          }))
        } else {
          this.noticeList = []
          this.$message.error((res && res.data && res.data.message) || '加载通知失败')
        }
      } catch (e) {
        this.noticeList = []
        this.$message.error('加载通知失败，请稍后重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.notices h3 {
  margin: 0;
}
.title-row {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.publisher {
  color: #909399;
  font-size: 12px;
}
</style>
