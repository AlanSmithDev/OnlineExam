<template>
  <div class="teacher-messages-container">
    <el-card class="box-card" shadow="hover">
      <div slot="header" class="clearfix card-header">
        <span>消息中心</span>
        <el-tag size="mini" :type="connected ? 'success' : 'info'">
          {{ connected ? '在线' : '离线' }}
        </el-tag>
      </div>
      <div class="history-toolbar">
        <el-button size="mini" icon="el-icon-refresh" @click="loadHistory" :loading="loadingHistory">
          刷新历史
        </el-button>
      </div>
      <div class="history-container" ref="history">
        <el-empty v-if="!loadingHistory && messages.length === 0" description="暂无消息" />
        <div
          v-for="msg in messages"
          :key="msg.id || msg._localId"
          :class="['message-item', { self: msg.senderNo === userNo }]"
        >
          <div class="message-meta">
            <span class="author">{{ msg.senderName || msg.senderNo }}</span>
            <span class="time">{{ formatTime(msg.createTime) }}</span>
          </div>
          <div class="message-content">{{ msg.content }}</div>
        </div>
      </div>
      <div class="input-area">
        <el-input
          type="textarea"
          :rows="3"
          v-model="input"
          placeholder="请输入要发送的内容，Enter 发送"
          @keyup.enter.native.exact.prevent="sendMessage"
        />
        <div class="input-actions">
          <el-button type="primary" :disabled="!canSend" @click="sendMessage">发送</el-button>
          <el-button type="text" size="mini" @click="clearInput">清空</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { fetchMessageHistory } from '../api/api';

export default {
  name: 'TeacherMessages',
  data() {
    const username = sessionStorage.getItem('username') || sessionStorage.getItem('USERNAME') || 'UNKNOWN';
    const displayName = sessionStorage.getItem('displayName') || sessionStorage.getItem('realName') || sessionStorage.getItem('name') || username;
    return {
      userNo: username,
      userName: displayName,
      ws: null,
      connected: false,
      loadingHistory: false,
      messages: [],
      input: '',
      _retryTimes: 0,
      _manualClose: false
    };
  },
  computed: {
    canSend() {
      return this.input.trim().length > 0 && this.connected;
    }
  },
  mounted() {
    this.loadHistory();
    this.connectSocket();
  },
  beforeDestroy() {
    this._manualClose = true;
    if (this.ws) {
      this.ws.close();
    }
  },
  methods: {
    loadHistory() {
      this.loadingHistory = true;
      fetchMessageHistory()
        .then((res) => {
          if (res.data && res.data.code === '0') {
            this.messages = res.data.data || [];
            this.scrollToBottom();
          }
        })
        .finally(() => {
          this.loadingHistory = false;
        });
    },
    connectSocket() {
      if (!this.userNo) {
        this.$message.error('无法识别当前用户，无法建立连接');
        return;
      }
      if (this.ws) {
        this.ws.close();
      }
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const host = `${window.location.hostname}:8083`;
      const url = `${protocol}//${host}/backage-api/ws/message/${encodeURIComponent(this.userNo)}`;
      this.ws = new WebSocket(url);
      this.ws.onopen = () => {
        this.connected = true;
        this._manualClose = false;
        this._retryTimes = 0;
        console.log('WebSocket connected:', this.userNo);
      };
      this.ws.onclose = () => {
        this.connected = false;
        if (this._manualClose) return;
        this._retryTimes = (this._retryTimes || 0) + 1;
        if (this._retryTimes > 5) return;
        setTimeout(() => this.connectSocket(), 5000);
      };
      this.ws.onerror = (err) => {
        this.connected = false;
        console.error('WebSocket error:', err);
      };
      this.ws.onmessage = (evt) => {
        try {
          const payload = JSON.parse(evt.data);
          this.appendMessage(payload);
        } catch (err) {
          console.error('消息解析失败', err);
        }
      };
    },
    sendMessage() {
      if (!this.canSend) {
        if (!this.connected) {
          this.$message.warning('连接已断开，正在尝试重连');
          this.connectSocket();
        }
        return;
      }
      const content = this.input.trim();
      const payload = {
        senderNo: this.userNo,
        senderName: this.userName,
        content
      };
      this.ws.send(JSON.stringify(payload));
      this.input = '';
    },
    appendMessage(message) {
      this.messages.push(message);
      this.scrollToBottom();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.history;
        if (el) {
          el.scrollTop = el.scrollHeight;
        }
      });
    },
    formatTime(value) {
      if (!value) return '';
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value;
      }
      const pad = (n) => (n < 10 ? `0${n}` : n);
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
    },
    clearInput() {
      this.input = '';
    }
  }
};
</script>

<style scoped>
.teacher-messages-container {
  padding: 20px;
  height: 100%;
}
.box-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.history-toolbar {
  text-align: right;
  margin-bottom: 10px;
}
.history-container {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  overflow-y: auto;
  background-color: #fafafa;
}
.message-item {
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.message-item.self {
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
}
.message-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.message-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  color: #303133;
}
.input-area {
  margin-top: 15px;
}
.input-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
</style>
