<template>
  <div class="teacher-management">
    <el-card>
      <div slot="header" class="clearfix">
        <span>教师管理</span>
        <el-button style="float:right" size="small" type="primary" @click="openCreate">新增教师</el-button>
      </div>

      <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center;">
        <el-input v-model="queryText" placeholder="按教师编号/姓名/专业查询" clearable style="width: 320px;" @keyup.enter.native="handleQuery" />
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" style="width: 100%">
        <el-table-column prop="teacher_no" label="教师编号" width="200" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="institute_name" label="院系" width="180" />
        <el-table-column prop="major_name" label="专业" width="180" />
        <el-table-column prop="teach_class_scope" label="授课班级" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="mini" type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:12px;float:right" :total="total" :page-size="pageSize" @current-change="handleCurrentChange" />
    </el-card>

    <el-dialog title="编辑教师" :visible.sync="editVisible" width="640px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="教师编号">
          <el-input v-model="form.teacher_no" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="院系">
          <el-select v-model="form.institute_no" filterable placeholder="请选择院系" style="width: 100%" @change="onInstituteChange">
            <el-option v-for="item in instituteOptions" :key="item.instituteNo" :label="item.instituteName" :value="item.instituteNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="form.major_no" filterable placeholder="请选择专业" style="width: 100%" @change="onMajorChange">
            <el-option v-for="item in majorOptions" :key="item.majorNo" :label="item.majorName" :value="item.majorNo" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课班级">
          <el-select v-model="selectedClassNos" multiple filterable collapse-tags placeholder="请选择授课班级" style="width: 100%" @change="onClassScopeChange">
            <el-option v-for="item in classOptions" :key="item.classNo" :label="item.className" :value="item.classNo" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as api from '../api/api';

export default {
  data() {
    return {
      list: [],
      loading: false,
      total: 0,
      pageSize: 10,
      pageNum: 1,
      editVisible: false,
      form: {},
      instituteOptions: [],
      majorOptions: [],
      classOptions: [],
      selectedClassNos: [],
      queryText: ''
    };
  },
  mounted() {
    this.loadList();
    this.loadInstitutes();
  },
  methods: {
    loadList() {
      this.loading = true;
      if (api.pageQueryTeachers) {
        api.pageQueryTeachers({ pageNum: this.pageNum, pageSize: this.pageSize, keyword: this.queryText }).then(res => {
          if (res.data && res.data.code === '0') {
            const d = res.data.data || {};
            this.list = d.list || [];
            this.total = d.total || 0;
          } else {
            this.$message.warning('获取教师列表失败');
          }
        }).catch(() => this.$message.error('网络错误')).finally(() => this.loading = false);
      } else {
        this.loading = false;
        this.$message.info('后端未提供教师列表接口（pageQueryTeachers）');
      }
    },
    loadInstitutes() {
      api.listInstitutes().then(res => {
        this.instituteOptions = (res.data && res.data.code === '0' && Array.isArray(res.data.data)) ? res.data.data : [];
      }).catch(() => {
        this.instituteOptions = [];
      });
    },
    loadMajors(instituteNo) {
      if (!instituteNo) {
        this.majorOptions = [];
        return Promise.resolve();
      }
      return api.listMajors(instituteNo).then(res => {
        this.majorOptions = (res.data && res.data.code === '0' && Array.isArray(res.data.data)) ? res.data.data : [];
      }).catch(() => {
        this.majorOptions = [];
      });
    },
    loadClasses(majorNo) {
      if (!majorNo) {
        this.classOptions = [];
        return Promise.resolve();
      }
      return api.listClasses(majorNo).then(res => {
        this.classOptions = (res.data && res.data.code === '0' && Array.isArray(res.data.data)) ? res.data.data : [];
      }).catch(() => {
        this.classOptions = [];
      });
    },
    onInstituteChange(v) {
      const row = this.instituteOptions.find(i => i.instituteNo === v) || {};
      this.form.institute_name = row.instituteName || '';
      this.form.college_no = row.collegeNo || this.form.college_no || '2026';
      this.form.major_no = '';
      this.form.major_name = '';
      this.selectedClassNos = [];
      this.form.teach_class_scope = '';
      this.majorOptions = [];
      this.classOptions = [];
      this.loadMajors(v);
    },
    onMajorChange(v) {
      const row = this.majorOptions.find(i => i.majorNo === v) || {};
      this.form.major_name = row.majorName || '';
      this.form.institute_no = row.instituteNo || this.form.institute_no || '';
      this.form.college_no = row.collegeNo || this.form.college_no || '2026';
      this.selectedClassNos = [];
      this.form.teach_class_scope = '';
      this.classOptions = [];
      this.loadClasses(v);
    },
    onClassScopeChange(vals) {
      const names = vals.map(no => {
        const row = this.classOptions.find(i => i.classNo === no) || {};
        return row.className || no;
      });
      this.form.teach_class_scope = names.join(',');
    },
    parseScopeToSelected(scope) {
      if (!scope) return [];
      const arr = String(scope).split(',').map(s => s.trim()).filter(Boolean);
      const result = [];
      arr.forEach(nameOrNo => {
        const byNo = this.classOptions.find(i => i.classNo === nameOrNo);
        if (byNo) {
          result.push(byNo.classNo);
          return;
        }
        const byName = this.classOptions.find(i => i.className === nameOrNo);
        if (byName) {
          result.push(byName.classNo);
        }
      });
      return result;
    },
    handleCurrentChange(p) { this.pageNum = p; this.loadList(); },
    handleQuery() {
      this.pageNum = 1;
      this.loadList();
    },
    resetQuery() {
      this.queryText = '';
      this.pageNum = 1;
      this.loadList();
    },
    openCreate() {
      this.form = { college_no: '2026' };
      this.majorOptions = [];
      this.classOptions = [];
      this.selectedClassNos = [];
      this.editVisible = true;
    },
    openEdit(row) {
      this.form = Object.assign({}, row);
      this.form.college_no = this.form.college_no || '2026';
      this.editVisible = true;
      this.loadMajors(this.form.institute_no)
        .then(() => this.loadClasses(this.form.major_no))
        .then(() => {
          this.selectedClassNos = this.parseScopeToSelected(this.form.teach_class_scope);
        });
    },
    submitEdit() {
      if (api.saveTeacher) {
        api.saveTeacher(this.form).then(res => {
          if (res.data && res.data.code === '0') {
            this.$message.success('保存成功');
            this.editVisible = false;
            this.loadList();
          } else {
            this.$message.error('保存失败');
          }
        }).catch(() => this.$message.error('网络错误'));
      } else {
        this.$message.info('后端未提供保存教师接口（saveTeacher）');
        this.editVisible = false;
      }
    },
    handleDelete(row) {
      this.$confirm('确认删除该教师？', '提示').then(() => {
        if (api.deleteTeacher) {
          api.deleteTeacher(row.teacher_no).then(res => {
            if (res.data && res.data.code === '0') {
              this.$message.success('删除成功');
              this.loadList();
            } else {
              this.$message.error('删除失败');
            }
          }).catch(() => this.$message.error('网络错误'));
        } else {
          this.$message.info('后端未提供删除教师接口（deleteTeacher）');
        }
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.teacher-management { padding: 16px; }
</style>
