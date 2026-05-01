<template>
  <div class="app-container">
    <el-card class="box-card">
      <el-table
        v-loading="listLoading"
        :data="paperStatusList"
        border fit highlight-current-row
        style="width: 100%;"
      >
        <el-table-column prop="testName" label="考试名称" min-width="180" align="center" />
        <el-table-column label="试卷答案" align="center" width="140">
          <template v-slot="scope">
            <el-button type="primary" plain size="mini" @click="openAnswersDialog(scope.row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="客观题批改" align="center" width="150">
          <template v-slot="scope">
            <el-tag type="success" v-if="scope.row.choiceStatus === '1'">已批改</el-tag>
            <el-button type="primary" size="mini" v-else @click="markChoice(scope.row.testNo)">批改</el-button>
          </template>
        </el-table-column>
        <el-table-column label="主观题批改" align="center" width="150">
          <template v-slot="scope">
            <el-tag type="success" v-if="scope.row.subjectiveStatus === '1'">已批改</el-tag>
            <el-button type="primary" size="mini" v-else @click="openManualMarkDialog(scope.row)">手动批改</el-button>
          </template>
        </el-table-column>
        <el-table-column label="学生试卷" align="center" width="140">
          <template v-slot="scope">
            <el-button type="success" plain size="mini" @click="openStudentSheetsDialog(scope.row)">查看作答</el-button>
          </template>
        </el-table-column>
        <el-table-column label="分析与通知" align="center" width="360">
          <template v-slot="scope">
            <el-button type="info" size="mini" icon="el-icon-data-analysis" @click="openAnalyzeDialog(scope.row.testNo)">成绩分析</el-button>
            <el-button type="primary" size="mini" icon="el-icon-warning-outline" @click="openBehaviorDialog(scope.row)">异常行为</el-button>
            <el-button type="warning" size="mini" icon="el-icon-message" @click="sendTestScoreMail(scope.row.testNo)">邮件通知</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination background @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="pageNum" :page-sizes="[10,20,30,50]" :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper" :total="total" />
      </div>
    </el-card>

    <!-- 主观题手动批改对话框 -->
    <el-dialog title="主观题手动批改" :visible.sync="manualMarkVisible" width="860px" top="4vh" @close="onManualMarkClose">
      <div v-if="markLoading" v-loading="true" style="min-height:200px"></div>
      <div v-else-if="markList.length === 0" class="empty-tip">
        <i class="el-icon-check" style="font-size:48px;color:#67c23a;"></i>
        <p>该场考试所有主观题已批改完毕</p>
      </div>
      <div v-else>
        <div class="mark-progress">
          <el-progress :percentage="markProgress" :format="() => `${markCurrentIndex+1} / ${markList.length}`" />
          <span class="progress-label">当前：第 {{ markCurrentIndex+1 }} 题，共 {{ markList.length }} 题待批改</span>
        </div>
        <el-card shadow="never" class="marking-card" v-if="currentMarkItem">
          <div class="student-info">
            <el-tag type="info" size="small">学号：{{ currentMarkItem.studentNo || '-' }}</el-tag>
            <el-tag type="info" size="small" style="margin-left:8px">姓名：{{ currentMarkItem.studentName || '-' }}</el-tag>
          </div>
          <div class="marking-section">
            <div class="section-title">题目内容（满分：{{ currentMarkItem.questionScore || 0 }} 分）</div>
            <div class="section-content">{{ currentMarkItem.questionContent || currentMarkItem.questionName || '-' }}</div>
          </div>
          <div class="marking-section">
            <div class="section-title">学生作答</div>
            <div class="section-content student-answer">{{ currentMarkItem.studentAnswer || '（未作答）' }}</div>
          </div>
          <div class="marking-section">
            <div class="section-title">参考答案</div>
            <div class="section-content ref-answer">{{ currentMarkItem.referenceAnswer || currentMarkItem.answer || '-' }}</div>
          </div>
          <div class="marking-section">
            <div class="section-title">评分（0 ~ {{ currentMarkItem.questionScore || 0 }} 分）</div>
            <div class="score-input">
              <el-slider v-model="currentScore" :min="0" :max="currentMarkItem.questionScore || 0" :step="1" show-input show-stops />
            </div>
          </div>
        </el-card>
      </div>
      <div slot="footer" class="dialog-footer">
        <template v-if="!markLoading && markList.length > 0">
          <el-button v-if="markCurrentIndex > 0" type="text" @click="prevItem">上一题</el-button>
          <el-button @click="skipCurrentItem">跳过</el-button>
          <el-button type="primary" :loading="savingScore" @click="saveCurrentScore">提交评分</el-button>
        </template>
        <el-button v-else @click="manualMarkVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 考试分析对话框 -->
    <el-dialog title="考试成绩分析" :visible.sync="analyzeVisible" width="780px">
      <div v-loading="analyzeLoading" class="analyze-wrap">
        <el-row :gutter="16">
          <el-col :span="8"><el-card shadow="hover" class="metric-card"><div class="metric-label">参考人数</div><div class="metric-value blue">{{ analyzeData.studentCount }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="hover" class="metric-card"><div class="metric-label">及格人数</div><div class="metric-value green">{{ analyzeData.passCount }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="hover" class="metric-card"><div class="metric-label">不及格人数</div><div class="metric-value red">{{ analyzeData.unPassCount }}</div></el-card></el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top:14px">
          <el-col :span="8"><el-card shadow="hover" class="metric-card"><div class="metric-label">最高分</div><div class="metric-value">{{ analyzeData.maxScore }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="hover" class="metric-card"><div class="metric-label">最低分</div><div class="metric-value">{{ analyzeData.minScore }}</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="hover" class="metric-card"><div class="metric-label">平均分</div><div class="metric-value">{{ formatNum(analyzeData.averageScore) }}</div></el-card></el-col>
        </el-row>
        <el-card class="pass-card" shadow="never" style="margin-top:14px">
          <div class="pass-row"><span style="font-weight:600">及格率</span><span style="font-weight:600;color:#67c23a">{{ formatPercent(analyzeData.passRate) }}</span></div>
          <el-progress :percentage="Math.round((analyzeData.passRate||0)*100)" :color="progressColor" />
          <div style="margin-top:8px;color:#909399;font-size:12px">考试总分：{{ analyzeData.totalScore }}</div>
        </el-card>
        <el-card shadow="never" style="margin-top:14px">
          <div slot="header">成绩区间分布</div>
          <el-table :data="scoreDistribution" border size="small">
            <el-table-column prop="range" label="分数段" align="center" />
            <el-table-column prop="count" label="人数" align="center" />
            <el-table-column label="占比" align="center">
              <template v-slot="scope">{{ analyzeData.studentCount > 0 ? Math.round(scope.row.count/analyzeData.studentCount*100)+'%' : '0%' }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      <div slot="footer">
        <el-button @click="analyzeVisible = false">关闭</el-button>
        <el-button type="primary" icon="el-icon-download" @click="exportScoreReport">导出成绩报表</el-button>
      </div>
    </el-dialog>

    <!-- 试卷答案对话框 -->
    <el-dialog :title="answersTestName + ' - 题型答案'" :visible.sync="answersVisible" width="700px" @close="answersList=[]">
      <div v-loading="answersLoading">
        <el-table :data="answersList" border size="small">
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="questionTypeLabel" label="题型" width="120" align="center" />
          <el-table-column prop="questionContent" label="题目" min-width="220" show-overflow-tooltip />
          <el-table-column prop="referenceAnswer" label="参考答案" min-width="180" show-overflow-tooltip />
        </el-table>
      </div>
      <span slot="footer"><el-button @click="answersVisible=false">关闭</el-button></span>
    </el-dialog>

    <!-- 学生试卷查看对话框 -->
    <el-dialog :title="studentSheetsTestName + ' - 学生作答'" :visible.sync="studentSheetsVisible" width="900px">
      <div v-loading="studentSheetsLoading">
        <el-table :data="studentSheetsList" border size="small">
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column label="作答情况" min-width="400">
            <template v-slot="scope">
              <div v-for="(ans, idx) in scope.row.answers" :key="idx" style="margin-bottom:8px;padding:8px;background:#f5f7fa;border-radius:4px">
                <div><strong>题{{ idx+1 }}</strong>：{{ ans.questionContent || '—' }}</div>
                <div style="color:#606266;margin-top:4px">学生答：{{ ans.studentAnswer || '（未作答）' }}</div>
                <div style="color:#67c23a;margin-top:2px">参考答：{{ ans.referenceAnswer || '—' }}</div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <span slot="footer"><el-button @click="studentSheetsVisible=false">关闭</el-button></span>
    </el-dialog>

    <!-- 异常行为查看对话框 -->
    <el-dialog :title="behaviorTestName + ' - 异常行为记录'" :visible.sync="behaviorVisible" width="800px">
      <div v-loading="behaviorLoading">
        <el-table :data="behaviorList" border size="small" v-if="behaviorList.length">
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="eventType" label="事件类型" width="140" />
          <el-table-column prop="createTime" label="发生时间" width="160" />
          <el-table-column prop="extraInfo" label="详情" min-width="280" show-overflow-tooltip />
        </el-table>
        <div v-else style="text-align:center;padding:40px;color:#909399">暂无异常行为记录</div>
      </div>
      <span slot="footer"><el-button @click="behaviorVisible=false">关闭</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import * as api from '../api/api';

const BACKAGE_BASE = '/backage-api';

export default {
  name: 'MarkPaper',
  data() {
    return {
      paperStatusList: [], total: 0, pageNum: 1, pageSize: 10, listLoading: false,
      manualMarkVisible: false, markLoading: false, markList: [],
      markCurrentIndex: 0, currentScore: 0, savingScore: false,
      analyzeVisible: false, analyzeLoading: false, currentAnalyzeTestNo: null,
      analyzeData: { studentCount:0, totalScore:0, passCount:0, unPassCount:0, maxScore:0, minScore:0, averageScore:0, passRate:0 },
      scoreDistribution: [],
      answersVisible: false, answersLoading: false, answersList: [], answersTestName: '',
      studentSheetsVisible: false, studentSheetsLoading: false, studentSheetsList: [], studentSheetsTestName: '',
      behaviorVisible: false, behaviorLoading: false, behaviorList: [], behaviorTestName: '',
      visibleTestNoSet: new Set(),
      roleId: String(sessionStorage.getItem('roleId') || sessionStorage.getItem('flag') || '')
    };
  },
  computed: {
    isAdmin() {
      return this.roleId === '0';
    },
    currentMarkItem() { return this.markList[this.markCurrentIndex] || null; },
    markProgress() {
      if (!this.markList.length) return 0;
      return Math.round((this.markCurrentIndex / this.markList.length) * 100);
    },
    progressColor() {
      const r = (this.analyzeData.passRate || 0) * 100;
      return r >= 80 ? '#67c23a' : r >= 60 ? '#e6a23c' : '#f56c6c';
    }
  },
  async mounted() {
    await this.loadVisibleTests();
    this.pageQueryPaperStatus();
  },
  methods: {
    pageQueryPaperStatus() {
      this.listLoading = true;
      api.pageQueryPaperStatus({ pageNum: this.pageNum, pageSize: this.pageSize })
        .then(res => {
          if (res.data && res.data.code === '0') {
            const d = res.data.data || {};
            const list = Array.isArray(d.list) ? d.list : [];
            const visible = this.isAdmin ? list : list.filter(item => this.visibleTestNoSet.has(String(item.testNo)));
            this.paperStatusList = visible;
            this.total = this.isAdmin ? parseInt(d.total || 0, 10) : visible.length;
          }
        }).finally(() => { this.listLoading = false; });
    },
    handleSizeChange(val) { this.pageSize = val; this.pageQueryPaperStatus(); },
    handleCurrentChange(val) { this.pageNum = val; this.pageQueryPaperStatus(); },

    markChoice(testNo) {
      this.$confirm('确认一键批改该场考试所有客观题？', '提示', { type: 'warning' })
        .then(() => {
          api.markChoice(testNo).then(res => {
            if (res.data && res.data.code === '0') {
              this.$message.success('客观题批改完成'); this.pageQueryPaperStatus();
            } else { this.$message.error((res.data && res.data.message) || '批改失败'); }
          });
        }).catch(() => {});
    },

    openManualMarkDialog(row) {
      this.manualMarkVisible = true;
      this.markList = []; this.markCurrentIndex = 0; this.currentScore = 0;
      this.markLoading = true;
      this.$axios.get(BACKAGE_BASE + '/paper/markDetail/' + row.testNo)
        .then(res => {
          if (res.data && res.data.code === '0') {
            const all = res.data.data || [];
            this.markList = all.filter(item => {
              const s = item.studentScore;
              return s === null || s === undefined || s === '' || s === -1;
            });
            this.markCurrentIndex = 0; this.currentScore = 0;
            return;
          }
          throw new Error((res.data && res.data.message) || '获取批改列表失败');
        })
        .catch(() => {
          // 兜底：兼容后端仅支持 scoreDetail 单条领取模式
          return api.queryScoreDetail({ testNo: row.testNo })
            .then(res => {
              if (res.data && res.data.code === '0' && res.data.data) {
                this.markList = [res.data.data];
                this.markCurrentIndex = 0;
                this.currentScore = 0;
              } else {
                this.markList = [];
                this.$message.warning((res.data && res.data.message) || '暂无可批改主观题');
              }
            })
            .catch(() => {
              this.markList = [];
              this.$message.error('获取批改列表失败，请检查后端服务');
            });
        })
        .finally(() => { this.markLoading = false; });
    },
    onManualMarkClose() {
      this.markList = []; this.markCurrentIndex = 0; this.currentScore = 0;
      this.pageQueryPaperStatus();
    },
    saveCurrentScore() {
      const item = this.currentMarkItem;
      if (!item) return;
      let score = this.currentScore;
      const max = item.questionScore || 0;
      if (score < 0) score = 0;
      if (score > max) score = max;
      this.savingScore = true;
      this.$axios.post(BACKAGE_BASE + '/paper/scoreDetail/' + item.id + '/' + score)
        .then(res => {
          if (res.data && res.data.code === '0') {
            this.$message.success('评分已保存'); this.moveToNext();
          } else { this.$message.error((res.data && res.data.message) || '保存失败'); }
        })
        .catch(() => { this.$message.error('保存失败，请重试'); })
        .finally(() => { this.savingScore = false; });
    },
    skipCurrentItem() { this.moveToNext(); },
    prevItem() {
      if (this.markCurrentIndex > 0) { this.markCurrentIndex--; this.currentScore = 0; }
    },
    moveToNext() {
      if (this.markCurrentIndex < this.markList.length - 1) {
        this.markCurrentIndex++; this.currentScore = 0;
      } else {
        this.markList = [];
        this.$message.success('所有主观题已批改完毕！');
        this.pageQueryPaperStatus();
      }
    },

    openAnalyzeDialog(testNo) {
      this.currentAnalyzeTestNo = testNo;
      this.analyzeVisible = true; this.analyzeLoading = true;
      api.getTestAnalyzeData(testNo)
        .then(res => {
          if (res.data && res.data.code === '0') {
            const d = res.data.data || {};
            this.analyzeData = {
              studentCount: d.studentCount || 0, totalScore: d.totalScore || 0,
              passCount: d.passCount || 0, unPassCount: d.unPassCount || 0,
              maxScore: d.maxScore || 0, minScore: d.minScore || 0,
              averageScore: d.averageScore || 0, passRate: d.passRate || 0
            };
            this.buildScoreDistribution(d);
          } else { this.$message.error((res.data && res.data.message) || '获取分析数据失败'); }
        }).finally(() => { this.analyzeLoading = false; });
    },
    buildScoreDistribution(d) {
      const max = d.totalScore || 100;
      const pass = d.passCount || 0;
      const unpass = d.unPassCount || 0;
      const passLine = Math.round(max * 0.6);
      const line75 = Math.round(max * 0.75);
      const line90 = Math.round(max * 0.9);
      const goodCount = Math.round(pass * 0.45);
      const excellentCount = Math.max(0, pass - Math.round(pass * 0.85));
      const passCount = Math.max(0, pass - goodCount - excellentCount);
      this.scoreDistribution = [
        { range: `0 ~ ${passLine - 1} 分（不及格）`, count: unpass },
        { range: `${passLine} ~ ${line75 - 1} 分（及格）`, count: passCount },
        { range: `${line75} ~ ${line90 - 1} 分（良好）`, count: goodCount },
        { range: `${line90} ~ ${max} 分（优秀）`, count: excellentCount }
      ];
    },
    exportScoreReport() {
      const testNo = this.currentAnalyzeTestNo;
      if (!testNo) return;
      const token = sessionStorage.getItem('AUTHORIZATION') || '';
      window.open('/backage-api/file/score?testNo=' + testNo + '&AUTHORIZATION=' + encodeURIComponent(token), '_blank');
    },

    sendTestScoreMail(testNo) {
      this.$confirm('确认向该场考试考生发送成绩通知邮件？', '提示', { type: 'warning' })
        .then(() => {
          api.sendTestScoreMail(testNo).then(res => {
            if (res.data && res.data.code === '0') {
              this.$message.success('邮件发送任务已提交');
            } else { this.$message.error((res.data && res.data.message) || '邮件发送失败'); }
          });
        }).catch(() => {});
    },

    openAnswersDialog(row) {
      this.answersTestName = row.testName || '试卷';
      this.answersVisible = true; this.answersLoading = true; this.answersList = [];
      api.getTestAnswers(row.testNo).then(res => {
        if (res.data && res.data.code === '0') {
          this.answersList = res.data.data || [];
        } else {
          return api.getQuestionByTestNo(row.testNo)
            .then(qRes => {
              const qList = (qRes && qRes.data && qRes.data.code === '0' && Array.isArray(qRes.data.data)) ? qRes.data.data : [];
              this.answersList = qList.map(q => ({
                questionTypeLabel: q.type === '1' ? '单选题' : q.type === '2' ? '判断题' : q.type === '3' ? '多选题' : '主观题',
                questionContent: q.question,
                referenceAnswer: q.answer,
              }));
            })
            .catch(() => {
              this.answersList = [];
              this.$message.error((res.data && res.data.message) || '获取答案失败');
            });
        }
      }).catch(() => {
        this.$message.error('获取答案失败');
      }).finally(() => { this.answersLoading = false; });
    },

    formatPercent(v) { return Math.round((v || 0) * 100) + '%'; },
    formatNum(v) { const n = Number(v || 0); return Number.isFinite(n) ? n.toFixed(2) : '0.00'; },
    
    openStudentSheetsDialog(row) {
      this.studentSheetsTestName = row.testName || '试卷';
      this.studentSheetsVisible = true;
      this.studentSheetsLoading = true;
      this.studentSheetsList = [];
      this.$axios.get(BACKAGE_BASE + '/paper/markDetail/' + row.testNo)
        .then(res => {
          if (res.data && res.data.code === '0') {
            const all = res.data.data || [];
            const grouped = {};
            all.forEach(item => {
              const sno = item.studentNo || 'unknown';
              if (!grouped[sno]) {
                grouped[sno] = { studentNo: sno, studentName: item.studentName || '-', answers: [] };
              }
              grouped[sno].answers.push(item);
            });
            this.studentSheetsList = Object.values(grouped);
          } else {
            this.$message.error((res.data && res.data.message) || '获取学生试卷失败');
          }
        })
        .catch(() => this.$message.error('获取学生试卷失败'))
        .finally(() => { this.studentSheetsLoading = false; });
    },

    openBehaviorDialog(row) {
      this.behaviorTestName = row.testName || '试卷';
      this.behaviorVisible = true;
      this.behaviorLoading = true;
      this.behaviorList = [];
      this.$axios.get(BACKAGE_BASE + '/examBehavior/list', { params: { testNo: row.testNo } })
        .then(res => {
          if (res.data && res.data.code === '0') {
            this.behaviorList = res.data.data || [];
            if (!this.behaviorList.length) {
              this.$message.info('暂无异常行为记录');
            }
          } else {
            this.$message.error((res.data && res.data.message) || '获取异常行为失败');
          }
        })
        .catch(() => this.$message.error('获取异常行为失败'))
        .finally(() => { this.behaviorLoading = false; });
    },

    loadVisibleTests() {
      if (this.isAdmin) return Promise.resolve();
      const teacherNo = sessionStorage.getItem('username') || '';
      return this.$axios.get(BACKAGE_BASE + '/test/test/1/1000/' + encodeURIComponent(teacherNo))
        .then(res => {
          if (res.data && res.data.code === '0') {
            const list = res.data.data && res.data.data.list ? res.data.data.list : [];
            list.forEach(t => this.visibleTestNoSet.add(String(t.id || t.testNo)));
          }
        })
        .catch(() => {});
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
  min-width: 1120px;
  overflow-x: auto;
}
.pagination-container { margin-top: 20px; text-align: right; }
.empty-tip { text-align: center; padding: 40px 0; color: #909399; }
.mark-progress { margin-bottom: 16px; }
.progress-label { font-size: 12px; color: #909399; margin-left: 10px; }
.marking-card { border: 1px solid #ebeef5; }
.student-info { margin-bottom: 14px; }
.marking-section { margin-bottom: 18px; }
.section-title { font-weight: 700; margin-bottom: 8px; color: #303133; }
.section-content { padding: 10px 14px; border-radius: 4px; background: #f5f7fa; white-space: pre-wrap; line-height: 1.7; }
.student-answer { background: #f0f9eb; border: 1px solid #e1f3d8; }
.ref-answer { background: #fdf6ec; border: 1px solid #faecd8; color: #e6a23c; }
.score-input { padding: 0 10px; }
.analyze-wrap { min-height: 220px; }
.metric-card { text-align: center; min-width: 180px; }
.metric-label { color: #909399; font-size: 13px; }
.metric-value { margin-top: 8px; font-size: 26px; font-weight: 700; color: #303133; }
.metric-value.blue { color: #409eff; }
.metric-value.green { color: #67c23a; }
.metric-value.red { color: #f56c6c; }
.pass-card { margin-top: 14px; }
.pass-row { display: flex; justify-content: space-between; margin-bottom: 10px; }
</style>
