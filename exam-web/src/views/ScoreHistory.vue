<template>
  <div class="page">
    <el-card>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <h2>成绩查询</h2>
      </div>
      <div v-if="scores && scores.length">
        <div class="chart" ref="chart" style="height:320px"></div>
        <el-table :data="scores" style="margin-top:16px">
          <el-table-column prop="testName" label="考试名称"></el-table-column>
          <el-table-column prop="totalScore" label="成绩"></el-table-column>
          <el-table-column prop="createTime" label="时间"></el-table-column>
        </el-table>
      </div>
      <div v-else>
        <p>暂无成绩</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as api from '../api/api';
import * as echarts from 'echarts';
export default {
  data() {
    return { scores: [] }
  },
  mounted() {
    const studentNo = this.normalizeStudentNo(sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username'));
    if (!studentNo) {
      this.$message.warning('请先登录');
      return;
    }
    api.getMyScores(studentNo).then(res => {
      if (res.data && res.data.code === '0') {
        const raw = res.data.data && (res.data.data.list ? res.data.data.list : res.data.data);
        const arr = Array.isArray(raw) ? raw : (raw ? [raw] : []);
        this.scores = arr.map(item => ({
          ...item,
          totalScore: item.totalScore != null ? item.totalScore : (item.total || 0)
        }));
        this.$nextTick(() => this.drawChart());
      } else {
        this.$message.error((res.data && res.data.message) || '获取成绩失败');
      }
    }).catch(() => this.$message.error('获取成绩失败'));
  },
  methods: {
    normalizeStudentNo(v) {
      const raw = String(v || '').trim();
      if (!raw) return '';
      const idx = raw.indexOf(':');
      return idx > -1 ? raw.slice(idx + 1).trim() : raw;
    },
    drawChart() {
      const ctx = this.$refs.chart;
      if (!ctx) return;
      const chart = echarts.init(ctx);
      const names = this.scores.map(s => s.testName || s.name || '考试');
      const vals = this.scores.map(s => Number(s.totalScore || 0));
      const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: names },
        yAxis: { type: 'value' },
        series: [{ data: vals, type: 'line', smooth: true, areaStyle: {} }]
      };
      chart.setOption(option);
    }
  }
}
</script>

<style scoped>
.page{padding:20px}
.chart{width:100%}
</style>
