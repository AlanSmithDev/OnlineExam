<template>
  <div class="score-analysis-page">
    <el-card class="filter-card" shadow="never">
      <div slot="header" class="filter-header">
        <span>成绩分析中心</span>
        <span class="header-tip">按专业、班级、学生检索并查看整体成绩概览</span>
      </div>

      <el-form :inline="true" :model="ScoreQueryTerm" class="score-filter-form">
        <el-form-item label="专业">
          <el-autocomplete
            v-model="ScoreQueryTerm.majorName"
            :fetch-suggestions="likeQueryMajorName"
            placeholder="请输入专业名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="班级">
          <el-input v-model="ScoreQueryTerm.classCo" placeholder="请输入班级" clearable />
        </el-form-item>

        <el-form-item label="姓名">
          <el-input v-model="ScoreQueryTerm.studentName" placeholder="请输入姓名" clearable />
        </el-form-item>

        <el-form-item label="学号">
          <el-input v-model="ScoreQueryTerm.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh-left" @click="resetFilters">重置</el-button>
        </el-form-item>

        <el-form-item class="export-item">
          <el-button type="success" icon="el-icon-download" @click="exportScoreExcel">导出成绩</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="summary-grid">
      <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card class="summary-card blue" shadow="hover">
          <div class="summary-label">记录总数</div>
          <div class="summary-value">{{ total }}</div>
          <div class="summary-desc">当前筛选条件下的成绩条目</div>
        </el-card>
      </el-col>
      <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card class="summary-card green" shadow="hover">
          <div class="summary-label">平均分</div>
          <div class="summary-value">{{ formatScore(summary.averageScore) }}</div>
          <div class="summary-desc">整体平均成绩水平</div>
        </el-card>
      </el-col>
      <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card class="summary-card orange" shadow="hover">
          <div class="summary-label">及格率</div>
          <div class="summary-value">{{ formatPercent(summary.passRate) }}</div>
          <div class="summary-desc">按 60 分及格线统计</div>
        </el-card>
      </el-col>
      <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card class="summary-card purple" shadow="hover">
          <div class="summary-label">最高 / 最低</div>
          <div class="summary-value dual">{{ summary.maxScore }} / {{ summary.minScore }}</div>
          <div class="summary-desc">快速了解成绩波动区间</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="analysis-grid">
      <el-col :xl="10" :lg="10" :md="24" :sm="24" :xs="24">
        <el-card class="analysis-card" shadow="never">
          <div slot="header" class="analysis-header">
            <span>分数段分布</span>
            <span class="analysis-tip">动态按当前结果集统计</span>
          </div>
          <div class="distribution-list">
            <div class="distribution-item" v-for="item in scoreBands" :key="item.label">
              <div class="distribution-top">
                <span>{{ item.label }}</span>
                <span>{{ item.count }} 人 · {{ formatPercent(item.ratio) }}</span>
              </div>
              <el-progress :percentage="Math.round(item.ratio * 100)" :stroke-width="14" :color="item.color" />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xl="14" :lg="14" :md="24" :sm="24" :xs="24">
        <el-card class="analysis-card" shadow="never">
          <div slot="header" class="analysis-header">
            <span>成绩明细</span>
            <span class="analysis-tip">支持横向滚动查看完整信息</span>
          </div>

          <el-table
            v-loading="listLoading"
            :data="ScoreList"
            stripe
            border
            style="width: 100%"
          >
            <el-table-column prop="testName" label="考试名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="studentNo" label="学号" width="150" />
            <el-table-column prop="studentName" label="姓名" width="120" />
            <el-table-column prop="majorName" label="专业" min-width="160" show-overflow-tooltip />
            <el-table-column prop="classCo" label="班级" min-width="140" show-overflow-tooltip />
            <el-table-column prop="totalScore" label="总分" width="100" align="center" />
            <el-table-column label="成绩等级" width="120" align="center">
              <template slot-scope="scope">
                <el-tag :type="scoreLevelType(scope.row.totalScore)">{{ scoreLevelLabel(scope.row.totalScore) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="ScoreQueryTerm.pageNum"
              :page-sizes="[10, 20, 30, 50]"
              :page-size="ScoreQueryTerm.pageSize"
              :total="total"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as api from '../api/api';

export default {
  name: 'StudentScore',
  data() {
    return {
      ScoreQueryTerm: {
        majorName: '',
        classCo: '',
        studentName: '',
        studentNo: '',
        pageSize: 10,
        pageNum: 1,
      },
      total: 0,
      ScoreList: [],
      allScoreRows: [],
      majorNameList: [],
      list: [],
      listLoading: false,
    };
  },
  computed: {
    summary() {
      const scores = this.allScoreRows
        .map(item => Number(item.totalScore || 0))
        .filter(item => Number.isFinite(item));
      if (!scores.length) {
        return {
          averageScore: 0,
          passRate: 0,
          maxScore: 0,
          minScore: 0,
          passCount: 0,
        };
      }
      const totalScore = scores.reduce((sum, cur) => sum + cur, 0);
      const passCount = scores.filter(score => score >= 60).length;
      return {
        averageScore: totalScore / scores.length,
        passRate: passCount / scores.length,
        maxScore: Math.max(...scores),
        minScore: Math.min(...scores),
        passCount,
      };
    },
    scoreBands() {
      const scores = this.allScoreRows
        .map(item => Number(item.totalScore || 0))
        .filter(item => Number.isFinite(item));
      const totalCount = scores.length || 1;
      const buildBand = (label, filter, color) => {
        const count = scores.filter(filter).length;
        return { label, count, ratio: count / totalCount, color };
      };
      return [
        buildBand('90 - 100 分（优秀）', score => score >= 90, '#67c23a'),
        buildBand('80 - 89 分（良好）', score => score >= 80 && score < 90, '#409eff'),
        buildBand('60 - 79 分（及格）', score => score >= 60 && score < 80, '#e6a23c'),
        buildBand('0 - 59 分（待提升）', score => score < 60, '#f56c6c'),
      ];
    },
  },
  mounted() {
    this.pageQueryStudentScore();
  },
  methods: {
    pageQueryStudentScore() {
      this.listLoading = true;
      api.pageQueryStudentScore(this.ScoreQueryTerm).then(res => {
        if (res.status === 200 && res.data['code'] === '0') {
          this.ScoreList = res.data['data']['list'] || [];
          this.total = parseInt(res.data.data['total'] || 0, 10);
          this.ScoreQueryTerm.pageNum = res.data.data['pageNum'] || this.ScoreQueryTerm.pageNum;
          this.ScoreQueryTerm.pageSize = res.data.data['pageSize'] || this.ScoreQueryTerm.pageSize;
        } else {
          this.ScoreList = [];
          this.total = 0;
          this.$alert((res.data && res.data.message) || '查询失败');
        }
      }).catch(() => {
        this.ScoreList = [];
        this.total = 0;
        this.$alert('查询失败，请检查后端服务');
      }).finally(() => {
        this.listLoading = false;
      });

      const summaryParams = {
        ...this.ScoreQueryTerm,
        pageNum: 1,
        pageSize: 1000,
      };
      api.pageQueryStudentScore(summaryParams).then(res => {
        if (res.status === 200 && res.data['code'] === '0') {
          this.allScoreRows = (res.data.data && res.data.data.list) || [];
        } else {
          this.allScoreRows = [];
        }
      }).catch(() => {
        this.allScoreRows = [];
      });
    },
    handleSearch() {
      this.ScoreQueryTerm.pageNum = 1;
      this.pageQueryStudentScore();
    },
    resetFilters() {
      this.ScoreQueryTerm = {
        majorName: '',
        classCo: '',
        studentName: '',
        studentNo: '',
        pageSize: 10,
        pageNum: 1,
      };
      this.pageQueryStudentScore();
    },
    likeQueryMajorName(queryString, cb) {
      const params = { name: queryString };
      api.likeQueryMajorName(params).then(res => {
        if (res.status === 200 && res.data['code'] === '0') {
          this.majorNameList = res.data['data'];
          this.list = this.majorNameList.map(item => ({ value: item, label: item }));
          cb(this.list);
        }
      });
    },
    handleCurrentChange(val) {
      this.ScoreQueryTerm.pageNum = val;
      this.pageQueryStudentScore();
    },
    handleSizeChange(val) {
      this.ScoreQueryTerm.pageSize = val;
      this.ScoreQueryTerm.pageNum = 1;
      this.pageQueryStudentScore();
    },
    exportScoreExcel() {
      const classCo = this.ScoreQueryTerm.classCo || 0;
      const studentNo = this.ScoreQueryTerm.studentNo || 0;
      const params = new URLSearchParams({
        majorName: this.ScoreQueryTerm.majorName || '',
        classCo,
        studentName: this.ScoreQueryTerm.studentName || '',
        studentNo,
      });
      const url = '/backage-api/file/score?' + params.toString();
      window.open(url, '_blank');
    },
    formatPercent(value) {
      return `${Math.round((Number(value || 0) || 0) * 100)}%`;
    },
    formatScore(value) {
      const num = Number(value || 0);
      return Number.isFinite(num) ? num.toFixed(2) : '0.00';
    },
    scoreLevelLabel(score) {
      const num = Number(score || 0);
      if (num >= 90) return '优秀';
      if (num >= 80) return '良好';
      if (num >= 60) return '及格';
      return '待提升';
    },
    scoreLevelType(score) {
      const num = Number(score || 0);
      if (num >= 90) return 'success';
      if (num >= 80) return '';
      if (num >= 60) return 'warning';
      return 'danger';
    },
  },
};
</script>

<style scoped>
.score-analysis-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card,
.analysis-card {
  border-radius: 12px;
}

.filter-header,
.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-tip,
.analysis-tip {
  color: #909399;
  font-size: 12px;
}

.score-filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 0;
}

.export-item {
  margin-left: auto;
}

.summary-grid,
.analysis-grid {
  margin: 0 !important;
}

.summary-card {
  border-radius: 12px;
  overflow: hidden;
}

.summary-label {
  color: #909399;
  font-size: 13px;
}

.summary-value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.summary-value.dual {
  font-size: 24px;
}

.summary-desc {
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}

.summary-card.blue { border-top: 4px solid #409eff; }
.summary-card.green { border-top: 4px solid #67c23a; }
.summary-card.orange { border-top: 4px solid #e6a23c; }
.summary-card.purple { border-top: 4px solid #8e44ad; }

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.distribution-item {
  padding: 12px 4px;
}

.distribution-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #303133;
  font-size: 14px;
}

.pagination-wrap {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .export-item {
    margin-left: 0;
  }
}
</style>
