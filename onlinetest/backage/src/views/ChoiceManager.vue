<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <!-- 搜索表单 -->
        <el-input placeholder="题目关键字" v-model="listQuery.question" style="width: 200px; margin-right: 10px;" class="filter-item" @keyup.enter.native="handleFilter" />
        <el-input placeholder="所属课程" v-model="listQuery.courseName" style="width: 150px; margin-right: 10px;" class="filter-item" @keyup.enter.native="handleFilter" />
        <el-select v-model="listQuery.type" placeholder="题目类型" clearable class="filter-item" style="width: 130px; margin-right: 10px;">
          <el-option label="单选题" value="1" />
          <el-option label="判断题" value="2" />
          <el-option label="多选题" value="3" />
        </el-select>
        <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
          搜索
        </el-button>
        <el-button class="filter-item" type="success" icon="el-icon-plus" style="margin-left: 8px;" @click="openCreate">
          新增
        </el-button>
      </div>
      <el-table
        ref="choiceTable"
        v-loading="listLoading"
        :data="list"
        border
        fit
        highlight-current-row
        style="width: 100%;">
        <el-table-column type="expand">
          <template slot-scope="props">
            <div class="expand-content">
              <!-- 编辑模式: 切换 -->
              <div v-if="!props.row.editMode" class="detail-view">
                 <div class="detail-item" style="margin-bottom: 8px;"><strong>[题目]: </strong> {{ props.row.question }}</div>
                 <div class="detail-item" style="margin-bottom: 12px;"><strong>[答案]: </strong> <span class="answer-text">{{ props.row.answer }}</span></div>
                 <div class="option-box" v-if="props.row.type!='4'"><strong>A</strong><span>{{ props.row.choiceA }}</span></div>
                 <div class="option-box" v-if="props.row.type!='4'"><strong>B</strong><span>{{ props.row.choiceB }}</span></div>
                 <div class="option-box" v-if="props.row.type=='1' || props.row.type=='3'"><strong>C</strong><span>{{ props.row.choiceC }}</span></div>
                 <div class="option-box" v-if="props.row.type=='1' || props.row.type=='3'"><strong>D</strong><span>{{ props.row.choiceD }}</span></div>
                 <div style="margin-top: 15px;">
                    <el-button size="mini" type="primary" icon="el-icon-edit" @click="enableEdit(props.row)">编辑</el-button>
                 </div>
              </div>
              <div v-else class="edit-view">
                 <!-- 编辑表单 -->
                 <el-form label-width="80px" label-position="left" class="edit-form">
                    <el-form-item label="题目">
                      <el-input type="textarea" :rows="2" v-model="props.row.question"></el-input>
                    </el-form-item>
                    <el-form-item label="题型">
                      <el-select v-model="props.row.type" style="width: 160px;">
                        <el-option label="单选题" value="1" />
                        <el-option label="判断题" value="2" />
                        <el-option label="多选题" value="3" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="答案">
                      <el-select v-model="props.row.answer" v-if="props.row.type=='1' || props.row.type=='2'">
                         <el-option label="A" value="A"></el-option>
                         <el-option label="B" value="B"></el-option>
                         <el-option label="C" value="C" v-if="props.row.type=='1'"></el-option>
                         <el-option label="D" value="D" v-if="props.row.type=='1'"></el-option>
                      </el-select>
                      <el-select v-model="props.row._answerList" v-else-if="props.row.type=='3'" multiple collapse-tags>
                         <el-option label="A" value="A"></el-option>
                         <el-option label="B" value="B"></el-option>
                         <el-option label="C" value="C"></el-option>
                         <el-option label="D" value="D"></el-option>
                      </el-select>
                      <el-input v-model="props.row.answer" v-else></el-input>
                    </el-form-item>
                    <!-- Options -->
                    <el-form-item label="选项A" v-if="props.row.type!='4'">
                      <el-input v-model="props.row.choiceA"></el-input>
                    </el-form-item>
                    <el-form-item label="选项B" v-if="props.row.type!='4'">
                      <el-input v-model="props.row.choiceB"></el-input>
                    </el-form-item>
                    <el-form-item label="选项C" v-if="props.row.type=='1' || props.row.type=='3'">
                      <el-input v-model="props.row.choiceC"></el-input>
                    </el-form-item>
                    <el-form-item label="选项D" v-if="props.row.type=='1' || props.row.type=='3'">
                      <el-input v-model="props.row.choiceD"></el-input>
                    </el-form-item>
                    <el-form-item>
                       <el-button type="success" size="mini" @click="saveEdit(props.row)">保存</el-button>
                       <el-button size="mini" @click="cancelEdit(props.row)">取消</el-button>
                    </el-form-item>
                 </el-form>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="ID" prop="id" width="80" align="center"></el-table-column>
        <el-table-column label="题目内容" prop="question" min-width="300" :show-overflow-tooltip="true"></el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag>{{ scope.row.type | typeFilter }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属课程" prop="courseName" width="150" align="center"></el-table-column>
        <el-table-column label="参考答案" prop="answer" width="100" align="center"></el-table-column>
        <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button size="mini" type="primary" icon="el-icon-edit" @click="startEdit(scope.row)">
                编辑
              </el-button>
              <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="listQuery.pageNum"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="listQuery.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="新增选择题" :visible.sync="createDialogVisible" width="760px">
      <el-form ref="createFormRef" :model="createForm" label-width="90px">
        <el-form-item label="题目">
          <el-input type="textarea" :rows="3" v-model="createForm.question"></el-input>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="createForm.type" style="width: 180px;">
            <el-option label="单选题" value="1" />
            <el-option label="判断题" value="2" />
            <el-option label="多选题" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="答案">
          <el-select v-model="createForm.answer" v-if="createForm.type==='1' || createForm.type==='2'" style="width: 180px;">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" v-if="createForm.type==='1'" />
            <el-option label="D" value="D" v-if="createForm.type==='1'" />
          </el-select>
          <el-select v-model="createForm._answerList" v-else multiple collapse-tags style="width: 280px;">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
        </el-form-item>
        <el-form-item label="选项A">
          <el-input v-model="createForm.choiceA"></el-input>
        </el-form-item>
        <el-form-item label="选项B">
          <el-input v-model="createForm.choiceB"></el-input>
        </el-form-item>
        <el-form-item label="选项C" v-if="createForm.type==='1' || createForm.type==='3'">
          <el-input v-model="createForm.choiceC"></el-input>
        </el-form-item>
        <el-form-item label="选项D" v-if="createForm.type==='1' || createForm.type==='3'">
          <el-input v-model="createForm.choiceD"></el-input>
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="createForm.courseName" filterable clearable placeholder="请选择课程" :loading="courseLoading" style="width: 100%;">
            <el-option v-for="item in courseOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="submitCreate">创建</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
  import * as api from '../api/api';
  export default {
    name: "ChoiceManager",
    filters: {
      typeFilter(type) {
        const typeMap = {
          '1': '单选题',
          '2': '判断题',
          '3': '多选题',
          '4': '主观题'
        }
        return typeMap[type] || '未知'
      }
    },
    data() {
      return {
        list: [],
        total: 0,
        listLoading: true,
        listQuery: {
          pageNum: 1,
          pageSize: 10,
          question: '',
          courseName: '',
          type: ''
        },
        // Backup for edit cancellation
        originalRow: {},
        courseLoading: false,
        courseOptions: [],
        createDialogVisible: false,
        createSubmitting: false,
        createForm: {
          question: '',
          answer: '',
          courseName: '',
          type: '1',
          choiceA: '',
          choiceB: '',
          choiceC: '',
          choiceD: '',
          _answerList: []
        }
      }
    },
    mounted() {
      this.getList();
      this.loadCourseOptions();
    },
    methods: {
      getList() {
        this.listLoading = true;
        api.choicePageQuery(this.listQuery).then(res => {
          if (res && res.data && res.data.code === '0') {
              const page = res.data.data || {};
              const rows = Array.isArray(page.list) ? page.list : [];
              this.list = rows.map(v => {
                 this.$set(v, 'editMode', false);
                 return v;
              });
              this.total = parseInt(page.total || 0, 10);
              this.listQuery.pageNum = page.pageNum || this.listQuery.pageNum;
              this.listQuery.pageSize = page.pageSize || this.listQuery.pageSize;
          } else {
              this.list = [];
              this.total = 0;
              this.$message.error((res && res.data && res.data.message) || '选择题列表加载失败');
          }
          this.listLoading = false;
        }).catch(() => {
          this.list = [];
          this.total = 0;
          this.listLoading = false;
          this.$message.error('选择题列表加载失败');
        });
      },
      loadCourseOptions() {
        this.courseLoading = true;
        api.getAllCourse().then((res) => {
          if (res && res.data && res.data.code === '0') {
            const rows = Array.isArray(res.data.data) ? res.data.data : [];
            const map = new Map();
            rows.forEach((row) => {
              const courseName = row.courseName || row.name || '';
              if (courseName && !map.has(courseName)) {
                map.set(courseName, { value: courseName, label: courseName });
              }
            });
            this.courseOptions = Array.from(map.values());
          } else {
            this.courseOptions = [];
          }
        }).catch(() => {
          this.courseOptions = [];
        }).finally(() => {
          this.courseLoading = false;
        });
      },
      handleFilter() {
        this.listQuery.pageNum = 1;
        this.getList();
      },
      handleSizeChange(val) {
        this.listQuery.pageSize = val;
        this.getList();
      },
      handleCurrentChange(val) {
        this.listQuery.pageNum = val;
        this.getList();
      },
      openCreate() {
        this.createDialogVisible = true;
        this.createSubmitting = false;
        this.createForm = {
          question: '',
          answer: '',
          courseName: '',
          type: '1',
          choiceA: '',
          choiceB: '',
          choiceC: '',
          choiceD: '',
          _answerList: []
        };
      },
      submitCreate() {
        const payload = this.buildCreateChoicePayload();
        if (!payload.question) {
          this.$message.warning('请输入题目');
          return;
        }
        if (!payload.courseName) {
          this.$message.warning('请选择所属课程');
          return;
        }
        this.createSubmitting = true;
        api.insertChoice(payload).then((res) => {
          if (res && res.data && res.data.code === '0') {
            this.$message.success('创建成功');
            this.createDialogVisible = false;
            this.getList();
          } else {
            this.$message.error((res && res.data && res.data.message) || '创建失败');
          }
        }).catch(() => {
          this.$message.error('创建失败');
        }).finally(() => {
          this.createSubmitting = false;
        });
      },
      buildCreateChoicePayload() {
        const payload = {
          question: this.createForm.question,
          type: this.createForm.type,
          answer: this.createForm.answer,
          choiceA: this.createForm.choiceA,
          choiceB: this.createForm.choiceB,
          choiceC: this.createForm.choiceC,
          choiceD: this.createForm.choiceD,
          courseName: this.createForm.courseName
        };
        if (payload.type === '2') {
          payload.choiceC = '';
          payload.choiceD = '';
          if (!['A', 'B'].includes(payload.answer)) payload.answer = 'A';
        }
        if (payload.type === '3') {
          const list = Array.isArray(this.createForm._answerList) ? this.createForm._answerList : [];
          payload.answer = list.join(',');
        }
        if (payload.type === '1' && !['A', 'B', 'C', 'D'].includes(payload.answer)) {
          payload.answer = 'A';
        }
        return payload;
      },
      enableEdit(row) {
        // Deep clone for backup
        this.originalRow[row.id] = JSON.parse(JSON.stringify(row));
        // prepare multi-answer list for type=3
        if (row.type === '3') {
          const raw = (row.answer || '').replace(/\s+/g, '');
          row._answerList = raw ? raw.split(',') : [];
        } else {
          row._answerList = [];
        }
        row.editMode = true;
      },
      startEdit(row) {
        this.enableEdit(row);
        if (this.$refs.choiceTable) {
          this.$refs.choiceTable.toggleRowExpansion(row, true);
        }
      },
      cancelEdit(row) {
        // Restore
        let origin = this.originalRow[row.id];
        if (origin) {
          Object.assign(row, origin);
        }
        row.editMode = false;
      },
      saveEdit(row) {
        const payload = this.buildChoicePayload(row);
        api.updateChoice(payload).then(res => {
          if (res.data.code === '0') {
            this.$message.success('修改成功');
            row.editMode = false;
            // Optionally refresh list or just keep local update
          } else {
            this.$message.error('修改失败');
          }
        });
      },
      buildChoicePayload(row) {
        const payload = {
          id: row.id,
          question: row.question,
          choiceA: row.choiceA,
          choiceB: row.choiceB,
          choiceC: row.choiceC,
          choiceD: row.choiceD,
          answer: row.answer,
          courseName: row.courseName,
          type: row.type
        };
        // normalize by type
        if (payload.type === '2') {
          payload.choiceC = '';
          payload.choiceD = '';
          if (payload.answer !== 'A' && payload.answer !== 'B') {
            payload.answer = 'A';
          }
        }
        if (payload.type === '3') {
          const list = Array.isArray(row._answerList) ? row._answerList : [];
          payload.answer = list.join(',');
        }
        if (payload.type === '1') {
          if (!['A', 'B', 'C', 'D'].includes(payload.answer)) {
            payload.answer = 'A';
          }
        }
        return payload;
      },
      handleDelete(row) {
        this.$confirm('确认删除该题目吗？', '提示', { type: 'warning' }).then(() => {
          api.deleteChoiceById(row.id).then(res => {
             if (res.data.code === '0') {
                this.$message.success('删除成功');
                this.getList();
             } else {
                this.$message.error(res.data.message || '删除失败');
             }
          });
        });
      }
    }
  }
</script>
<style scoped>
.app-container {
  padding: 20px;
  height: calc(100vh - 40px);
  overflow-y: auto;
}
.filter-container {
  padding-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
.detail-view {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
}
.expand-content {
  padding: 12px 24px 20px;
  max-height: 420px;
  overflow: auto;
}
.detail-view,
.edit-view {
  min-width: 720px;
}
.answer-text {
  color: #67C23A;
  font-weight: 700;
}
.option-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 10px;
  border: 1px solid #3a3f4b;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
}
.option-box strong {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: rgba(64, 158, 255, 0.15);
  color: #8fc0ff;
  font-weight: 700;
}
.edit-form .el-form-item {
  display: flex;
  align-items: center;
}
.edit-form .el-form-item__label {
  flex: 0 0 80px;
  line-height: 32px;
}
.edit-form .el-form-item__content {
  flex: 1;
  margin-left: 0 !important;
}
.edit-form .el-input,
.edit-form .el-select,
.edit-form .el-textarea {
  width: 100%;
}
.edit-form .el-input__inner,
.edit-form .el-textarea__inner {
  height: 44px;
  line-height: 44px;
}
.edit-form .el-textarea__inner {
  min-height: 44px;
}
.edit-form .el-input__inner,
.edit-form .el-textarea__inner,
.edit-form .el-select .el-input__inner {
  border-width: 2px;
  border-radius: 8px;
  padding-left: 12px;
}
.action-buttons {
  display: inline-flex;
  gap: 6px;
  white-space: nowrap;
}
</style>
