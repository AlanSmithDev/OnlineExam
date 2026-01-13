<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-input
          v-model="listQuery.question"
          placeholder="题目关键字"
          style="width: 220px; margin-right: 10px;"
          @keyup.enter.native="handleFilter"
        />
        <el-input
          v-model="listQuery.courseName"
          placeholder="所属课程"
          style="width: 180px; margin-right: 10px;"
          @keyup.enter.native="handleFilter"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
        <el-button type="success" icon="el-icon-plus" @click="openCreate" style="margin-left: 8px;">新增</el-button>
      </div>

      <div class="table-shell">
        <el-table
          v-loading="listLoading"
          :data="list"
          border
          fit
          highlight-current-row
          style="width: 100%;"
        >
          <el-table-column type="expand">
            <template slot-scope="props">
              <div class="expand-content">
                <div class="detail-card">
                  <div class="detail-title">题目内容</div>
                  <div class="detail-block question-block">{{ props.row.question || '暂无题目内容' }}</div>
                </div>

                <div class="detail-card answer-card">
                  <div class="detail-title">参考答案</div>
                  <div class="detail-block answer-block">{{ props.row.answer || '暂无参考答案' }}</div>
                </div>

                <div class="meta-line">
                  <span class="meta-label">所属课程：</span>{{ props.row.courseName || '-' }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="ID" prop="id" width="90" align="center" />
          <el-table-column label="题目内容" prop="question" min-width="360" :show-overflow-tooltip="true" />
          <el-table-column label="所属课程" prop="courseName" width="170" align="center" />
          <el-table-column label="类型" width="110" align="center">
            <template>
              <el-tag type="warning">主观题</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300" align="center">
            <template slot-scope="scope">
              <div class="action-buttons">
                <el-button size="mini" type="info" @click="openAnswerSheetDialog(scope.row)">学生试卷</el-button>
                <el-button size="mini" type="primary" @click="openEdit(scope.row)">编辑</el-button>
                <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          background
          :current-page="listQuery.pageNum"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="listQuery.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog title="新增主观题" :visible.sync="createDialogVisible" width="760px">
      <el-form ref="createFormRef" :model="createForm" :rules="formRules" label-width="90px">
        <el-form-item label="题目" prop="question">
          <el-input type="textarea" :rows="5" v-model="createForm.question" />
        </el-form-item>
        <el-form-item label="参考答案" prop="answer">
          <el-input type="textarea" :rows="5" v-model="createForm.answer" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseName">
          <el-select
            v-model="createForm.courseName"
            filterable
            clearable
            placeholder="请选择课程"
            :loading="courseLoading"
            style="width: 100%;"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSubmitting" @click="submitCreate">创建</el-button>
      </span>
    </el-dialog>

    <el-dialog title="编辑主观题" :visible.sync="editDialogVisible" width="980px">
      <el-form ref="editFormRef" :model="editForm" :rules="formRules" label-width="90px">
        <el-form-item label="题目" prop="question">
          <el-input type="textarea" :rows="4" v-model="editForm.question" />
        </el-form-item>
        <el-form-item label="参考答案" prop="answer">
          <el-input type="textarea" :rows="4" v-model="editForm.answer" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseName">
          <el-select
            v-model="editForm.courseName"
            filterable
            clearable
            placeholder="请选择课程"
            :loading="courseLoading"
            style="width: 100%;"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="edit-answer-sheet-title">学生试卷（可直接修改得分）</div>
      <el-table :data="editAnswerSheetList" v-loading="editAnswerSheetLoading" border size="mini" max-height="320">
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="testName" label="考试" width="160" show-overflow-tooltip />
        <el-table-column prop="studentAnswer" label="学生作答" min-width="220" show-overflow-tooltip />
        <el-table-column prop="questionScore" label="满分" width="70" align="center" />
        <el-table-column label="得分" width="140" align="center">
          <template slot-scope="scope">
            <el-input-number
              v-model="scope.row._editScore"
              :min="0"
              :max="Number(scope.row.questionScore || 0)"
              :step="1"
              controls-position="right"
              size="mini"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="saveOneScore(scope.row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>

      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit">保存题目</el-button>
      </span>
    </el-dialog>

    <el-dialog title="学生试卷作答" :visible.sync="answerSheetVisible" width="900px">
      <el-table :data="answerSheetList" v-loading="answerSheetLoading" border>
        <el-table-column prop="studentNo" label="学号" width="150" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="testName" label="考试" width="180" />
        <el-table-column prop="studentAnswer" label="学生作答" min-width="260" show-overflow-tooltip />
        <el-table-column prop="questionScore" label="满分" width="80" align="center" />
        <el-table-column prop="studentScore" label="得分" width="80" align="center" />
        <el-table-column prop="flag" label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="String(scope.row.flag) === '1' ? 'success' : 'warning'">
              {{ String(scope.row.flag) === '1' ? '已批改' : '待批改' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="answerSheetVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import * as api from '../api/api';

export default {
  name: 'SubjectiveManager',
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
        type: '4'
      },
      courseLoading: false,
      courseOptions: [],
      createDialogVisible: false,
      createSubmitting: false,
      createForm: {
        question: '',
        answer: '',
        courseName: ''
      },
      editDialogVisible: false,
      editSubmitting: false,
      answerSheetVisible: false,
      answerSheetLoading: false,
      answerSheetList: [],
      editAnswerSheetLoading: false,
      editAnswerSheetList: [],
      editForm: {
        id: null,
        question: '',
        answer: '',
        courseName: ''
      },
      formRules: {
        question: [{ required: true, message: '请输入题目', trigger: 'blur' }],
        answer: [{ required: true, message: '请输入参考答案', trigger: 'blur' }],
        courseName: [{ required: true, message: '请选择所属课程', trigger: 'change' }]
      }
    };
  },
  mounted() {
    this.getList();
    this.loadCourseOptions();
  },
  methods: {
    getList() {
      this.listLoading = true;
      api.subjectivePageQuery(this.listQuery).then((res) => {
        if (res.data && res.data.code === '0') {
          const page = res.data.data || {};
          this.list = Array.isArray(page.list) ? page.list : [];
          this.total = parseInt(page.total || 0, 10);
          this.listQuery.pageNum = page.pageNum || this.listQuery.pageNum;
          this.listQuery.pageSize = page.pageSize || this.listQuery.pageSize;
        }
      }).finally(() => {
        this.listLoading = false;
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
      this.createForm = { question: '', answer: '', courseName: '' };
      this.$nextTick(() => {
        if (this.$refs.createFormRef) this.$refs.createFormRef.clearValidate();
      });
    },
    submitCreate() {
      this.$refs.createFormRef.validate((ok) => {
        if (!ok) return;
        this.createSubmitting = true;
        const payload = {
          question: this.createForm.question,
          answer: this.createForm.answer,
          courseName: this.createForm.courseName,
          type: '4'
        };
        api.insertSubjective(payload).then((res) => {
          if (res.data && res.data.code === '0') {
            this.$message.success('创建成功');
            this.createDialogVisible = false;
            this.getList();
          } else {
            this.$message.error((res.data && res.data.message) || '创建失败');
          }
        }).catch(() => {
          this.$message.error('创建失败');
        }).finally(() => {
          this.createSubmitting = false;
        });
      });
    },
    openEdit(row) {
      this.editDialogVisible = true;
      this.editSubmitting = false;
      this.editForm = {
        id: row.id,
        question: row.question || '',
        answer: row.answer || '',
        courseName: row.courseName || ''
      };
      this.loadEditAnswerSheets(row.id);
      this.$nextTick(() => {
        if (this.$refs.editFormRef) this.$refs.editFormRef.clearValidate();
      });
    },
    loadEditAnswerSheets(questionId) {
      this.editAnswerSheetLoading = true;
      this.editAnswerSheetList = [];
      api.listSubjectiveAnswerSheets(questionId).then((res) => {
        if (res.data && res.data.code === '0' && Array.isArray(res.data.data)) {
          this.editAnswerSheetList = res.data.data.map((item) => ({
            ...item,
            _editScore: Number(item.studentScore || 0)
          }));
        } else {
          this.editAnswerSheetList = [];
        }
      }).catch(() => {
        this.editAnswerSheetList = [];
      }).finally(() => {
        this.editAnswerSheetLoading = false;
      });
    },
    saveOneScore(row) {
      const maxScore = Number(row.questionScore || 0);
      let score = Number(row._editScore);
      if (Number.isNaN(score)) score = 0;
      if (score < 0) score = 0;
      if (score > maxScore) score = maxScore;
      api.saveStudentScoreDetail({ id: row.id, score: Math.round(score) }).then((res) => {
        if (res.data && res.data.code === '0') {
          row.studentScore = Math.round(score);
          row._editScore = Math.round(score);
          this.$message.success('该学生得分已更新');
        } else {
          this.$message.error((res.data && res.data.message) || '得分保存失败');
        }
      }).catch(() => {
        this.$message.error('得分保存失败');
      });
    },
    openAnswerSheetDialog(row) {
      this.answerSheetVisible = true;
      this.answerSheetLoading = true;
      this.answerSheetList = [];
      api.listSubjectiveAnswerSheets(row.id).then((res) => {
        if (res.data && res.data.code === '0' && Array.isArray(res.data.data)) {
          this.answerSheetList = res.data.data;
        } else {
          this.$message.warning((res.data && res.data.message) || '暂无学生作答数据');
        }
      }).catch(() => {
        this.$message.error('加载学生试卷失败');
      }).finally(() => {
        this.answerSheetLoading = false;
      });
    },
    submitEdit() {
      this.$refs.editFormRef.validate((ok) => {
        if (!ok) return;
        this.editSubmitting = true;
        const payload = {
          id: this.editForm.id,
          question: this.editForm.question,
          answer: this.editForm.answer,
          courseName: this.editForm.courseName,
          type: '4'
        };
        api.updateSubjective(payload).then((res) => {
          if (res.data && res.data.code === '0') {
            this.$message.success('修改成功');
            this.editDialogVisible = false;
            this.getList();
          } else {
            this.$message.error((res.data && res.data.message) || '修改失败');
          }
        }).catch(() => {
          this.$message.error('修改失败');
        }).finally(() => {
          this.editSubmitting = false;
        });
      });
    },
    handleDelete(row) {
      this.$confirm('确认删除该题目吗？', '提示', { type: 'warning' }).then(() => {
        api.deleteSubjectiveById(row.id).then((res) => {
          if (res.data && res.data.code === '0') {
            this.$message.success('删除成功');
            this.getList();
          } else {
            this.$message.error((res.data && res.data.message) || '删除失败');
          }
        }).catch(() => {
          this.$message.error('删除失败');
        });
      });
    }
  }
};
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

.table-shell {
  border-top: 2px solid #dfe6ec;
  border-bottom: 2px solid #dfe6ec;
  padding-top: 10px;
  padding-bottom: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.expand-content {
  padding: 14px 20px 18px;
  border: 1px solid #e8eef5;
  border-radius: 8px;
  background: #fafcff;
}

.detail-card {
  margin-bottom: 12px;
  border: 1px solid #dfe6ec;
  border-radius: 8px;
  background: #ffffff;
}

.answer-card {
  border-color: #d4ead5;
  background: #f6fbf6;
}

.detail-title {
  padding: 10px 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}

.detail-block {
  padding: 14px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  max-height: 220px;
  overflow-y: auto;
}

.question-block {
  font-size: 14px;
}

.answer-block {
  color: #2f7f36;
}

.meta-line {
  margin-top: 4px;
  font-size: 13px;
  color: #606266;
}

.meta-label {
  color: #909399;
}

.action-buttons {
  display: inline-flex;
  gap: 6px;
  white-space: nowrap;
}

.edit-answer-sheet-title {
  margin: 8px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
</style>

