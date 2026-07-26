<template>
  <div class="page">
    <el-card>
      <h2>个人信息</h2>
      <el-form :model="profile" label-width="100px">
        <el-form-item label="学号">
          <el-input v-model="profile.studentNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="profile.name"></el-input>
        </el-form-item>
        <el-form-item label="班级编号">
          <el-input v-model="profile.classNo"></el-input>
        </el-form-item>
        <el-form-item label="班级名称">
          <el-input v-model="profile.className"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profile.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profile.email"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  data() {
    return {
      loading: false,
      profile: {
        studentNo: sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username') || '',
        name: '',
        classNo: '',
        className: '',
        phone: '',
        email: ''
      }
    }
  },
  mounted() {
    this.loadProfile();
  },
  methods: {
    loadProfile() {
      const username = this.profile.studentNo;
      if (!username) return;
      const token = sessionStorage.getItem('AUTHORIZATION') || '';
      axios.get('/backage-api/profile/me', {
        params: { username },
        headers: token ? { AUTHORIZATION: token } : {}
      }).then(res => {
        if (res.data && res.data.code === '0') {
          const data = res.data.data || {};
          this.profile.studentNo = data.username || this.profile.studentNo;
          this.profile.name = data.name || '';
          this.profile.classNo = data.classNo || '';
          this.profile.className = data.className || '';
          this.profile.phone = data.phone || '';
          this.profile.email = data.email || '';
          if (data.name) {
            sessionStorage.setItem('realName', data.name);
          }
        }
      }).catch(() => {
        this.$message.warning('个人信息读取失败，请稍后重试');
      });
    },
    save() {
      this.loading = true;
      const payload = {
        studentNo: this.profile.studentNo,
        name: this.profile.name,
        classNo: this.profile.classNo,
        className: this.profile.className,
        phone: this.profile.phone,
        email: this.profile.email
      };
      const token = sessionStorage.getItem('AUTHORIZATION') || '';
      axios.post('/backage-api/profile/update', payload, {
        headers: token ? { AUTHORIZATION: token } : {}
      })
        .then(res => {
          if (res.data && res.data.code === '0') {
            if (this.profile.name) {
              sessionStorage.setItem('realName', this.profile.name);
            }
            this.$message.success('保存成功');
          } else {
            this.$message.error((res.data && res.data.message) || '保存失败');
          }
        }).catch(() => this.$message.error('网络错误'))
        .finally(() => {
          this.loading = false;
        });
    }
  }
}
</script>

<style scoped>
.page { padding: 20px; }
</style>
