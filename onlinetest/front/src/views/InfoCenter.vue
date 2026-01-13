<template>
  <div class="info-center">
    <el-card class="info-card">
      <el-tabs v-model="activeTab" @tab-click="onTabClick">
        <el-tab-pane label="通知中心" name="notices">
          <router-view v-if="activeTab === 'notices'"></router-view>
        </el-tab-pane>
        <el-tab-pane label="考试规则" name="rules">
          <router-view v-if="activeTab === 'rules'"></router-view>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'InfoCenter',
  data() {
    return {
      activeTab: 'notices'
    }
  },
  created() {
    // 根据当前路由初始化选中的 tab
    if (this.$route.path.indexOf('/app/info/rules') === 0) {
      this.activeTab = 'rules'
    } else {
      this.activeTab = 'notices'
    }
  },
  watch: {
    $route(to) {
      if (to.path.indexOf('/app/info/rules') === 0) {
        this.activeTab = 'rules'
      } else if (to.path.indexOf('/app/info/notices') === 0) {
        this.activeTab = 'notices'
      }
    }
  },
  methods: {
    onTabClick(tab) {
      if (tab.name === 'rules') {
        this.$router.push('/app/info/rules')
      } else {
        this.$router.push('/app/info/notices')
      }
    }
  }
}
</script>

<style scoped>
.info-center {
  padding: 20px;
}
.info-card {
  min-height: 400px;
}
</style>
