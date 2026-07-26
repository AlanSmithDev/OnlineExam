<template>
  <div>
    <!-- 原 Authority.vue 中的课程权限内容迁移到这里 -->
    <el-form :inline="true" :model="QuestionQueryTerm" class="demo-form-inline">
      <el-form-item label="教师编号">
        <el-input v-model="QuestionQueryTerm.teacherNo"></el-input>
      </el-form-item>
      <el-form-item label="教师姓名">
        <el-input v-model="QuestionQueryTerm.teacherName"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="queryByConditions">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="TeacherCourseVoList"
      style="width: 100%"
      v-loading="listLoading">
      <el-table-column
        prop="teacherNo"
        label="教师编号"
        width="300">
      </el-table-column>
      <el-table-column
        prop="teacherName"
        label="教师姓名"
        width="300">
      </el-table-column>
      <el-table-column
        label="课程权限"
        width="300">
        <template #default="scope">
          <el-select v-model="scope.row.courseIds" multiple placeholder="请选择">
            <el-option
              v-for="item in allCourse"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column
        label="操作">
        <template #default="scope">
          <el-button type="danger" size="small" @click="saveOption(scope.row)">确认</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-col :span="24" class="toolbar">
      <el-pagination layout="total, prev, pager, next" @current-change="handleCurrentChange"
                     :page-size="QuestionQueryTerm.pageSize"
                     :total="total"
                     style="float:right;">
      </el-pagination>
    </el-col>
  </div>
</template>

<script>
import * as api from '../api/api';
export default {
  data() {
    return {
      TeacherCourseVoList: [],//列表数据
      QuestionQueryTerm: {//查询参数
        teacherNo: '',//教师编号
        teacherName: '',//教师姓名
        pageSize: 10,//分页参数-每页显示条数
        pageNum: 1,//分页参数-显示第几页
      },
      total: 0,//总条数，
      allCourse: [],//所有课程
      listLoading: true
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.queryByConditions()
      this.getAllCourse()
    },
    //多条件分页查询
    queryByConditions() {
      this.listLoading = true
      api.pageQueryCourseAuthority(this.QuestionQueryTerm).then(res => {
        if (res && res.status === 200 && res.data && res.data.code === '0') {
          this.TeacherCourseVoList = res.data.data.list || []
          this.total = parseInt(res.data.data.total || 0)
        } else {
          this.$message.warning('查询操作失败')
        }
        this.listLoading = false
      }).catch(()=>{this.listLoading=false;this.$message.error('网络错误')})
    },

    //分页查询
    handleCurrentChange(val) {
      this.QuestionQueryTerm.pageNum = val;
      this.queryByConditions();
    },

    //获取所有课程
    getAllCourse() {
      api.getAllCourse().then(res => {
        this.allCourse = res.data['data']
        console.log('所有课程：：' + JSON.stringify(this.allCourse))
      }).catch(()=>this.$message.error('获取课程失败'))
    },

    //执行修改权限
    saveOption(teacherCourseVo) {
      api.editCourseAuthority(teacherCourseVo).then(res => {
        if (res.status == 200 && res.data['code'] == '0') {
          this.queryByConditions()//刷新列表
          this.$message.success('操作成功')
        } else {
          this.$message.error('操作失败')
        }
      }).catch(()=>this.$message.error('操作失败'))
    },
  }
}
</script>

<style scoped>
.toolbar { margin-top: 12px; }
</style>
