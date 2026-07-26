<template>
  <div class="page">
    <el-card>
      <h2>通知</h2>
      <el-table :data="messages">
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="content" label="内容"></el-table-column>
        <el-table-column prop="reply" label="回复"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  data() { return { messages: [] } },
  mounted() {
    const studentNo = sessionStorage.getItem('USERNAME');
    // Directly call backend notice endpoint to avoid missing api export
    axios.get('/backage-api/notice/list', { params: { userNo: studentNo } })
      .then(res => {
        if (res.data && res.data.code === '0') this.messages = res.data.data || [];
        else this.messages = res.data && res.data.data ? res.data.data : [];
      }).catch(()=>this.$message.error('获取消息失败'));
  }
}
</script>

<style scoped>
.page{padding:20px}
</style>
