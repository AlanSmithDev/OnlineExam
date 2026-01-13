<template>
  <div class="exam-page" @mouseleave="handleMouseLeave">
    <div class="exam-header">
      <el-card class="info-card merged-info-card">
        <div class="exam-header-top">
          <div class="info-card__title exam-title-row">
            <span class="exam-title">{{ testName }}</span>
          </div>
          <el-tag v-if="meta.cameraRequired" type="danger" effect="dark" class="ai-flag">AI监控已开启</el-tag>
        </div>
        <div v-if="meta.examIntro" class="exam-intro">{{ meta.examIntro }}</div>
        <!-- 第一行：考试名称，居中显示 -->
        <!--
        <div class="info-card__title exam-title-row">
          <span class="exam-title">{{ testName }}</span>
        </div>
        -->
        <!-- 可选的摄像头状态提示与反作弊说明 -->
        <div class="anti-cheat-bar">
          <span class="anti-cheat-text">本场考试已开启防作弊监控：请保持摄像头开启，切换页面累计 3 次将自动交卷。</span>
        </div>
        <!-- 第二部分：考试信息；第三部分：考生信息 -->
        <div class="info-columns">
          <div class="info-block">
            <h4>考试信息</h4>
            <ul>
              <li><label>科目</label><span>{{ meta.courseName || '未提供' }}</span></li>
              <li><label>出卷人</label><span>{{ meta.paperCreator || meta.teacherName || '—' }}</span></li>
              <li><label>监考教师</label><span>{{ meta.teacherName || '—' }}</span></li>
              <li><label>学院</label><span>{{ meta.collegeName || candidate.collegeName || '—' }}</span></li>
              <li><label>专业</label><span>{{ meta.majorName || candidate.majorName || '—' }}</span></li>
              <li><label>班级</label><span>{{ meta.className || candidate.className || '—' }}</span></li>
              <li><label>考试时间</label><span>{{ formattedExamWindow }}</span></li>
              <li><label>考试时长</label><span>{{ meta.testTime ? meta.testTime + ' 分钟' : '—' }}</span></li>
              <li><label>总分</label><span>{{ meta.totalScore || '—' }} 分</span></li>
              <li v-if="meta.examNotice"><label>注意事项</label><span class="exam-notice">{{ meta.examNotice }}</span></li>
            </ul>
          </div>
          <div class="info-block">
            <h4>考生信息</h4>
            <ul>
              <li><label>姓名</label><span>{{ candidate.name }}</span></li>
              <li><label>学号</label><span>{{ candidate.studentNo || '—' }}</span></li>
              <li><label>班级</label><span>{{ candidate.className || meta.className || '—' }}</span></li>
              <li><label>专业/学院</label><span>{{ candidate.department || candidate.majorName || candidate.collegeName || '—' }}</span></li>
              <li><label>座位号</label><span>{{ candidate.seatNo || '—' }}</span></li>
            </ul>
          </div>
        </div>
      </el-card>
    </div>

    <div class="camera-preview" v-if="showCamera">
      <video ref="cameraVideo" autoplay playsinline muted></video>
      <div class="camera-badge">实时监控中</div>
    </div>

    <div v-if="questions && questions.length" class="exam-body">
      <div class="question-area">
        <el-card class="question-card">
          <div class="question-heading">
            <div>
              <p class="question-index">第 {{ currentIndex + 1 }} 题</p>
              <p class="question-type">{{ current.typeName || '题目' }} · {{ current.score || 0 }} 分</p>
            </div>
            <!-- 统一倒计时，只在这里显示一次 -->
            <div class="question-timer">剩余时间 <span>{{ displayTime }}</span></div>
          </div>
          <div class="question-content" v-html="current.question"></div>
          <div class="question-answers">
            <template v-if="current.type === '1'">
              <el-radio-group v-model="answers[currentIndex]" class="option-group">
                <el-radio v-for="(opt, i) in current.options" :key="i" :label="opt.value || opt">
                  <span class="option-label">{{ opt.label || opt }}</span>
                </el-radio>
              </el-radio-group>
            </template>
            <template v-else-if="current.type === '2'">
              <el-input v-model="answers[currentIndex]" size="large" class="input-blank" placeholder="请输入填空答案" />
            </template>
            <template v-else-if="current.type === '3'">
              <el-radio-group v-model="answers[currentIndex]" class="option-group option-group--judge">
                <el-radio label="正确">正确</el-radio>
                <el-radio label="错误">错误</el-radio>
              </el-radio-group>
            </template>
            <template v-else>
              <el-input
                v-model="answers[currentIndex]"
                type="textarea"
                :rows="8"
                class="subjective-input"
                placeholder="请完整作答"
              />
            </template>
          </div>
        </el-card>
      </div>

      <div class="answer-card">
        <el-card shadow="hover" class="answer-card__wrapper">
          <div class="answer-card__header">
            <span>答题卡</span>
            <!-- 移除这里的倒计时，避免重复显示 -->
          </div>
          <div class="answer-card__sections">
            <div v-for="section in cardSections" :key="section.code" class="answer-card__section">
              <div class="section-title">
                {{ section.label }}（每题 {{ section.scorePerQuestion }} 分 × {{ section.items.length }} = {{ section.scorePerQuestion * section.items.length }} 分）
              </div>
              <div class="section-grid">
                <div
                  v-for="item in section.items"
                  :key="item.index"
                  :class="['grid-item', answered(item.index) ? 'answered' : '', item.index === currentIndex ? 'active' : '']"
                  @click="jumpTo(item.index)"
                >
                  {{ item.number }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div v-if="questions && questions.length" class="exam-footer-actions">
      <el-button @click="prev" :disabled="currentIndex === 0">上一题</el-button>
      <div class="action-right">
        <el-button @click="submit">提交试卷</el-button>
        <!-- 预留退出考试按钮（后续与防作弊逻辑联动） -->
        <el-button type="danger" @click="handleExitExam">退出考试</el-button>
        <el-button type="primary" @click="next" :disabled="currentIndex >= questions.length - 1">下一题</el-button>
      </div>
    </div>

    <el-card v-else class="empty-card">
      <p>暂无试题</p>
    </el-card>
  </div>
</template>

<script>
import * as api from '../api/api';
export default {
  data() {
    return {
      testNo: this.$route.query.testNo || '',
      testName: this.$route.query.testName || '考试',
      questions: [],
      currentIndex: 0,
      answers: [],
      totalSeconds: 0,
      timerId: null,
      // 防作弊相关状态预留
      isExamStarted: false,
      leaveCount: 0,
      maxLeaveCount: 3,
      hasAutoSubmitted: false,
      cameraStream: null,
      showCamera: false,
      meta: {
        courseName: this.$route.query.courseName || '',
        teacherName: this.$route.query.teacherName || '',
        paperCreator: this.$route.query.paperCreator || this.$route.query.teacherName || '',
        startTime: this.$route.query.startTime || '',
        endTime: this.$route.query.endTime || '',
        totalScore: Number(this.$route.query.totalScore) || 0,
        testTime: Number(this.$route.query.testTime || this.$route.query.duration) || 0,
        examNotice: this.$route.query.examNotice || this.$route.query.exam_notice || '',
        examIntro: this.$route.query.examIntro || this.$route.query.exam_intro || '',
        cameraRequired: String(this.$route.query.cameraRequired || this.$route.query.camera_required || '1') === '1'
      },
      candidate: {
        name: this.$route.query.studentName || sessionStorage.getItem('USERNAME') || '考生',
        studentNo: this.$route.query.studentNo || sessionStorage.getItem('USERNAME') || '',
        className: this.$route.query.studentClassName || this.$route.query.className || '',
        collegeName: this.$route.query.studentCollegeName || this.$route.query.collegeName || '',
        majorName: this.$route.query.studentMajorName || this.$route.query.majorName || '',
        department: this.$route.query.department || this.$route.query.majorName || '',
        seatNo: this.$route.query.seatNo || ''
      }
    }
  },
  computed: {
    current() { return this.questions[this.currentIndex] || {}; },
    displayTime() {
      const s = Math.max(this.totalSeconds, 0);
      const h = Math.floor(s / 3600).toString().padStart(2, '0');
      const m = Math.floor((s % 3600) / 60).toString().padStart(2, '0');
      const ss = (s % 60).toString().padStart(2, '0');
      return `${h}:${m}:${ss}`;
    },
    formattedExamWindow() {
      if (!this.meta.startTime && !this.meta.endTime) {
        return '—';
      }
      if (!this.meta.endTime) {
        return this.formatDate(this.meta.startTime);
      }
      if (!this.meta.startTime) {
        return this.formatDate(this.meta.endTime);
      }
      return `${this.formatDate(this.meta.startTime)} ~ ${this.formatDate(this.meta.endTime)}`;
    },
    cardSections() {
      const definitions = [
        { code: '1', label: '单选题' },
        { code: '2', label: '填空题' },
        { code: '3', label: '判断题' },
        { code: '4', label: '主观题' }
      ];
      const groups = definitions.map(def => ({ ...def, items: [], scorePerQuestion: 0 }));
      (this.questions || []).forEach((q, index) => {
        const typeCode = q.type || q.questionType || 'other';
        let target = groups.find(group => group.code === typeCode);
        if (!target && typeCode === 'other') {
          target = { code: 'other', label: '其它题型', items: [], scorePerQuestion: 0 };
          groups.push(target);
        }
        if (target) {
          target.items.push({ index, number: index + 1 });
          // 取该题型第一道题的分值作为每题分值
          if (!target.scorePerQuestion && q.score) {
            target.scorePerQuestion = q.score;
          }
        }
      });
      return groups.filter(group => group.items.length);
    }
  },
  mounted() {
    const blocked = sessionStorage.getItem(`EXAM_BLOCK_${this.testNo}`);
    if (blocked === '1') {
      this.$alert('您已因多次离开页面被强制交卷，当前考试不可再次进入。', '提示', { type: 'warning' });
      this.$router.replace({ path: '/app/tests' });
      return;
    }
    this.loadQuestions();
    this.loadCandidateProfile();
    // 监听窗口失焦和可见性变化
    window.addEventListener('blur', this.handleWindowBlur);
    document.addEventListener('visibilitychange', this.handleVisibilityChange);
    // 强制开启摄像头（失败则提示并上报）
    this.openCamera();
  },
  beforeDestroy() {
    window.removeEventListener('blur', this.handleWindowBlur);
    document.removeEventListener('visibilitychange', this.handleVisibilityChange);
    this.closeCamera();
  },
  methods: {
    loadCandidateProfile() {
      const token = sessionStorage.getItem('AUTHORIZATION') || '';
      const username = this.candidate.studentNo || sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username') || '';
      if (!token || !username) return;
      this.$axios.get('/backage-api/profile/me', {
        params: { username },
        headers: { AUTHORIZATION: token }
      }).then(res => {
        if (res && res.data && res.data.code === '0') {
          const d = res.data.data || {};
          this.candidate.name = d.name || this.candidate.name;
          this.candidate.studentNo = d.username || this.candidate.studentNo;
          this.candidate.className = d.className || d.classNo || this.candidate.className;
          this.candidate.collegeName = d.collegeName || this.candidate.collegeName;
          this.candidate.majorName = d.majorName || this.candidate.majorName;
          this.candidate.department = [d.collegeName, d.majorName].filter(Boolean).join(' / ') || this.candidate.department;
        }
      }).catch(() => {});
    },
    formatDate(value) {
      if (!value) return '';
      return String(value).replace('T', ' ');
    },
    loadQuestions() {
      const raw = this.$route.query.questionList || this.$route.query.questions;
      if (raw) {
        try {
          const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
          const q = parsed || [];
          if (Array.isArray(q)) {
            this.applyQuestions(q);
            const minutes = parseInt(this.$route.query.testTime || this.meta.testTime || 60, 10);
            this.meta.testTime = minutes;
            this.totalSeconds = minutes * 60;
            this.isExamStarted = true;
            this.startTimer();
            return;
          }
        } catch (e) {
          console.warn('failed to parse route-provided questions, will fetch from server', e);
        }
      }

      api.getTestQuestions(this.testNo).then(res => {
        if (res.data && res.data.code === '0') {
          const q = res.data.data || [];
          this.applyQuestions(q);
          const minutes = parseInt(this.$route.query.testTime || this.meta.testTime || 60, 10);
          this.meta.testTime = minutes;
          this.totalSeconds = minutes * 60;
          this.isExamStarted = true;
          this.startTimer();
        }
      }).catch(() => this.$message.error('获取题目失败'));
    },
    startTimer() {
      if (this.timerId) clearInterval(this.timerId);
      this.timerId = setInterval(() => {
        if (this.totalSeconds <= 0) {
          clearInterval(this.timerId);
          this.submit();
          return;
        }
        this.totalSeconds--;
      }, 1000);
    },
    prev() { if (this.currentIndex > 0) this.currentIndex--; },
    next() { if (this.currentIndex < this.questions.length - 1) this.currentIndex++; },
    submit(force = false) {
      if (this.hasAutoSubmitted && !force) {
        return;
      }
      const payload = this.questions.map((q, idx) => ({ questionIndex: idx, answer: this.answers[idx], testNo: this.testNo }));
      const token = sessionStorage.getItem('AUTHORIZATION');
      api.submitAnswers(payload, token).then(res => {
        if (res.data && res.data.code === '0') {
          this.hasAutoSubmitted = true;
          const auto = this.leaveCount >= this.maxLeaveCount;
          if (auto) {
            sessionStorage.setItem(`EXAM_BLOCK_${this.testNo}`, '1');
          }
          this.$message.success('提交成功');
          this.reportBehavior('SUBMIT_OK', { auto });
          this.$router.push({ path: '/submit' });
        } else {
          this.$message.error(res.data.message || '提交失败');
        }
      }).catch(() => this.$message.error('网络错误'));
    },
    applyQuestions(q) {
      this.questions = q.map(item => {
        const type = item.type || item.questionType;
        const base = {
          question: item.question,
          type,
          typeName: (type === '1') ? '单选题' : (type === '2' ? '填空题' : (type === '3' ? '判断题' : '主观题')),
          score: item.score || item.questionScore || 0,
          questionType: type
        };
        // 组装选择题选项
        if (type === '1') {
          const options = [];
          if (item.choiceA || item.choicea) options.push({ label: `A. ${item.choiceA || item.choicea}`, value: 'A' });
          if (item.choiceB || item.choiceb) options.push({ label: `B. ${item.choiceB || item.choiceb}`, value: 'B' });
          if (item.choiceC || item.choicec) options.push({ label: `C. ${item.choiceC || item.choicec}`, value: 'C' });
          if (item.choiceD || item.choiced) options.push({ label: `D. ${item.choiceD || item.choiced}`, value: 'D' });
          base.options = options.length ? options : (item.choiceList || item.options || []);
        } else {
          base.options = item.choiceList || item.options || [];
        }
        return base;
      });
      this.answers = new Array(this.questions.length).fill('');
    },
    answered(index) {
      const ans = this.answers[index];
      if (Array.isArray(ans)) return ans.length > 0;
      return ans !== undefined && ans !== null && ans !== '';
    },
    jumpTo(index) {
      this.currentIndex = index;
    },
    handleWindowBlur() {
      this.handleLeaveEvent('blur');
    },
    handleVisibilityChange() {
      if (document.visibilityState === 'hidden') {
        this.handleLeaveEvent('visibility');
      }
    },
    handleMouseLeave() {
      // 鼠标移出页面也算一次离开
      this.handleLeaveEvent('mouse-leave');
    },
    handleLeaveEvent(reason) {
      if (!this.isExamStarted || this.hasAutoSubmitted) {
        return;
      }
      this.leaveCount += 1;
      this.reportBehavior('LEAVE_PAGE', { reason, leaveCount: this.leaveCount });
      this.$message.warning(`检测到您离开考试页面（${reason}），第 ${this.leaveCount}/${this.maxLeaveCount} 次，请遵守考试规则。`);
      if (this.leaveCount >= this.maxLeaveCount) {
        sessionStorage.setItem(`EXAM_BLOCK_${this.testNo}`, '1');
        this.$alert('因多次离开考试页面，系统已自动为您交卷。', '提示', { type: 'warning' });
        this.reportBehavior('AUTO_SUBMIT', { leaveCount: this.leaveCount });
        this.submit(true);
      }
    },
    handleExitExam() {
      this.$confirm('退出考试将视为一次离开行为，可能影响成绩。确定退出吗？', '提示', {
        confirmButtonText: '退出考试',
        cancelButtonText: '继续答题',
        type: 'warning'
      }).then(() => {
        this.handleLeaveEvent('exit');
        if (!this.hasAutoSubmitted) {
          this.submit(true);
        }
      }).catch(() => {});
    },
    async openCamera() {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        this.$message.warning('浏览器不支持摄像头，无法进入考试');
        this.showCamera = false;
        this.reportBehavior('CAMERA_ERROR', { message: 'not supported' });
        return;
      }
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        this.cameraStream = stream;
        this.showCamera = true;
        const video = this.$refs.cameraVideo;
        if (video) {
          video.srcObject = stream;
          video.play && video.play();
        }
        this.reportBehavior('CAMERA_OK', {});
      } catch (e) {
        this.showCamera = false;
        this.$message.error('摄像头开启失败，请检查设备与权限');
        this.reportBehavior('CAMERA_ERROR', { message: e && e.message });
      }
    },
    reportBehavior(eventType, extra = {}) {
      if (!api.reportExamBehavior) return;
      const payload = {
        testNo: this.testNo,
        studentNo: this.candidate.studentNo,
        eventType,
        extraInfo: JSON.stringify(extra)
      };
      api.reportExamBehavior(payload).catch(() => {});
    },
  }
}
</script>

<style scoped>
.exam-page {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}
.exam-header {
  margin-bottom: 20px;
}
.info-card__title.exam-title-row {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}
.exam-title {
  font-size: 24px;
  font-weight: 700;
}
.info-columns {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
}
.info-block {
  flex: 1;
  min-width: 220px;
}
.info-block h4 {
  font-weight: 600;
  margin-bottom: 8px;
}
.info-block ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.info-block li {
  margin-bottom: 6px;
}
.info-block label {
  display: inline-block;
  width: 80px;
  font-size: 14px;
  color: #909399;
}
.info-block span {
  font-size: 14px;
  color: #303133;
}
.anti-cheat-bar {
  margin-bottom: 12px;
  padding: 8px 12px;
  background-color: #fff7e6;
  border-radius: 6px;
  border: 1px solid #ffe58f;
}
.anti-cheat-text {
  font-size: 13px;
  color: #d48806;
}
.camera-preview {
  position: fixed;
  right: 24px;
  bottom: 96px;
  width: 220px;
  height: 150px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  z-index: 10;
}
.camera-preview video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.exam-body {
  display: flex;
  gap: 24px;
  align-items: stretch;
}
.question-area {
  flex: 1;
}
.question-card {
  min-height: 520px;
  display: flex;
  flex-direction: column;
}
.question-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.question-index {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}
.question-type {
  margin: 4px 0 0;
  color: #909399;
}
.question-timer {
  font-weight: 600;
  color: #409eff;
}
.question-timer span {
  font-size: 18px;
  margin-left: 6px;
}
.question-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  font-size: 16px;
  line-height: 1.7;
  color: #303133;
}
.question-answers {
  margin-top: 20px;
}
.option-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.option-group .el-radio {
  padding: 16px 20px;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  width: 100%;
  margin: 0;
}
.option-group .el-radio.is-checked {
  border-color: #409eff;
  background: #ecf5ff;
}
.option-label {
  font-size: 16px;
}
.option-group--judge {
  flex-direction: row;
}
.input-blank >>> .el-input__inner {
  height: 48px;
  font-size: 16px;
}
.subjective-input >>> textarea {
  min-height: 200px;
  font-size: 15px;
  line-height: 1.7;
}
.exam-footer-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  position: sticky;
  bottom: 0;
  z-index: 5;
}
.action-right {
  display: flex;
  gap: 12px;
}
.answer-card {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-self: stretch;
}
.answer-card__wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.answer-card__header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 12px;
}
.countdown {
  color: #409eff;
}
.answer-card__section + .answer-card__section {
  margin-top: 18px;
}
.section-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.section-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}
.grid-item {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  text-align: center;
  padding: 8px 0;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.grid-item:hover {
  border-color: #409eff;
  color: #409eff;
}
.grid-item.answered {
  background: #ecf5ff;
  border-color: #c6e2ff;
}
.grid-item.active {
  border-color: #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
}
.empty-card {
  text-align: center;
}
@media (max-width: 1200px) {
  .exam-body {
    flex-direction: column;
  }
  .answer-card {
    width: 100%;
  }
  .exam-footer-actions {
    position: static;
  }
}
.exam-notice {
  display: inline-block;
  max-width: 460px;
  white-space: pre-wrap;
}
.exam-header-top {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}
.ai-flag {
  position: absolute;
  right: 16px;
  top: 0;
}
.exam-intro {
  margin: 8px 0 12px;
  padding: 8px 12px;
  border-radius: 6px;
  background: #eef5ff;
  color: #1f2d3d;
  line-height: 1.6;
}
</style>
