<template>
  <div class="app-container">
    <el-card class="box-card">
      <div class="filter-container">
        <el-input
          v-model="filters.name"
          placeholder="请输入考试名称"
          style="width: 240px;"
          class="filter-item"
          @keyup.enter.native="handleFilter"
        />
        <el-button class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
        <el-button class="filter-item" style="margin-left: 10px;" type="primary" icon="el-icon-plus" @click="openCreate">
          新建考试
        </el-button>
        <el-button class="filter-item" type="danger" icon="el-icon-delete" @click="batchRemove" :disabled="sels.length === 0">
          批量删除
        </el-button>
      </div>

      <el-table
        v-loading="listLoading"
        :data="testVoList"
        border
        fit
        highlight-current-row
        style="width: 100%;"
        @selection-change="selsChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="name" label="考试名称" min-width="160" align="center" />
        <el-table-column prop="courseName" label="所属课程" width="180" align="center" />
        <el-table-column prop="teacherName" label="创建教师" width="140" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row)">{{ statusLabelText(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开考时间" width="180" align="center">
          <template slot-scope="scope">{{ formatDateValue(scope.row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="180" align="center">
          <template slot-scope="scope">{{ formatDateValue(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="testTime" label="时长(分钟)" width="110" align="center" />
        <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="handlePreview(scope.row)">预览</el-button>
            <el-button v-if="String(scope.row.status) !== '1'" size="mini" type="success" @click="openEdit(scope.row)">编辑</el-button>
            <el-button v-if="String(scope.row.status) !== '1'" size="mini" type="danger" @click="handleDel(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="page"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <el-dialog title="新建考试" :visible.sync="dialogCreateVisible" width="720px" top="6vh">
      <el-form :model="createForm" :rules="createRules" ref="createForm" label-width="120px">
        <el-form-item label="考试名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="createForm.courseId" placeholder="请选择课程" style="width: 100%;">
            <el-option v-for="c in allCourse" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="参考班级" prop="classNoList">
          <el-select
            v-model="createForm.classNoList"
            multiple
            filterable
            collapse-tags
            :loading="classLoading"
            placeholder="请选择参考班级（可多选）"
            style="width: 100%;"
          >
            <el-option v-for="c in classOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开考时间" prop="startTime">
          <el-date-picker type="datetime" v-model="createForm.startTime" style="width: 100%;" placeholder="请选择开考时间" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="testTime">
          <el-input-number v-model="createForm.testTime" :min="1" :max="600" style="width: 200px;" />
        </el-form-item>

        <el-form-item label="开头信息" prop="examIntro">
          <el-input type="textarea" :rows="3" v-model="createForm.examIntro" placeholder="如：考试须知、答题格式、工具限制等" />
        </el-form-item>
        <el-form-item label="注意事项" prop="examNotice">
          <el-input type="textarea" :rows="4" v-model="createForm.examNotice" placeholder="逐条填写注意事项，换行分隔" />
        </el-form-item>
        <el-form-item label="摄像头/AI监控" prop="cameraRequired">
          <el-switch v-model="createForm.cameraRequired" active-text="开启" inactive-text="关闭" />
        </el-form-item>

        <el-divider>题目配置（随机抽题）</el-divider>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="单选题数量" prop="singleRandomCount">
              <el-input-number v-model="createForm.singleRandomCount" :min="0" :max="200" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单选题分值" prop="singleScore">
              <el-input-number v-model="createForm.singleScore" :min="0" :max="50" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="判断题数量" prop="judgeRandomCount">
              <el-input-number v-model="createForm.judgeRandomCount" :min="0" :max="200" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="判断题分值" prop="judgeScore">
              <el-input-number v-model="createForm.judgeScore" :min="0" :max="50" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="多选题数量" prop="multipleRandomCount">
              <el-input-number v-model="createForm.multipleRandomCount" :min="0" :max="200" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="多选题分值" prop="multipleScore">
              <el-input-number v-model="createForm.multipleScore" :min="0" :max="50" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="主观题数量" prop="subjectiveRandomCount">
              <el-input-number v-model="createForm.subjectiveRandomCount" :min="0" :max="200" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主观题分值" prop="subjectiveScore">
              <el-input-number v-model="createForm.subjectiveScore" :min="0" :max="100" style="width: 200px;" @change="recalcTotal" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="总分">
          <el-input :value="createForm.totalScore" disabled style="width: 200px;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogCreateVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">提交</el-button>
      </div>
    </el-dialog>

    <el-dialog title="考试预览" :visible.sync="dialogPreviewVisible" width="900px" top="5vh">
      <el-skeleton v-if="previewLoading" :rows="8" animated />
      <div v-else class="preview-dialog-body">
        <div class="preview-exam-layout">
          <div class="exam-main">
            <div class="exam-info-card">
              <div class="exam-title">{{ previewData.testInfo.name || '未命名考试' }}</div>
              <div class="exam-meta-grid">
                <div>
                  <span class="meta-label">所属学科</span>
                  <span class="meta-value">{{ resolveCourseName(previewData.testInfo.courseId) }}</span>
                </div>
                <div>
                  <span class="meta-label">出题人</span>
                  <span class="meta-value">{{ previewTeacherDisplay(previewData.testInfo) }}</span>
                </div>
                <div>
                  <span class="meta-label">考试时间</span>
                  <span class="meta-value time-value">{{ previewTimeRange(previewData.testInfo) }}</span>
                </div>
                <div>
                  <span class="meta-label">考试时长</span>
                  <span class="meta-value">{{ previewData.testInfo.testTime ? previewData.testInfo.testTime + ' 分钟' : '—' }}</span>
                </div>
                <div>
                  <span class="meta-label">考试总分</span>
                  <span class="meta-value">{{ previewData.testInfo.totalScore != null ? previewData.testInfo.totalScore : '—' }}</span>
                </div>
                <div>
                  <span class="meta-label">考试状态</span>
                  <span class="meta-value">{{ statusLabelText(previewData.testInfo) }}</span>
                </div>
                <div>
                  <span class="meta-label">参考班级</span>
                  <span class="meta-value">
                    <template v-if="previewData.classNoList && previewData.classNoList.length">
                      <el-tag
                        v-for="c in previewData.classNoList"
                        :key="c"
                        size="mini"
                        type="info"
                        effect="plain"
                        style="margin-right: 6px; margin-bottom: 4px;"
                      >{{ c }}</el-tag>
                    </template>
                    <span v-else class="text-muted">无</span>
                  </span>
                </div>
                <div>
                  <span class="meta-label">所属学院</span>
                  <span class="meta-value">{{ previewInstituteName() }}</span>
                </div>
                <div>
                  <span class="meta-label">所属专业</span>
                  <span class="meta-value">{{ previewMajorName() }}</span>
                </div>
                <div>
                  <span class="meta-label">摄像头/AI监控</span>
                  <el-tag :type="previewData.testInfo.cameraRequired ? 'danger' : 'info'" effect="dark">
                    {{ previewData.testInfo.cameraRequired ? '已开启' : '未开启' }}
                  </el-tag>
                </div>
              </div>
              <div v-if="previewData.testInfo.examIntro" class="exam-intro">
                <div class="notice-title">开头信息</div>
                <div class="notice-content">
                  <p v-for="(line, idx) in splitNotice(previewData.testInfo.examIntro)" :key="'intro-'+idx">{{ line }}</p>
                </div>
              </div>
              <div v-if="previewData.testInfo.examNotice" class="exam-notice">
                <div class="notice-title">注意事项</div>
                <div class="notice-content">
                  <p v-for="(line, idx) in splitNotice(previewData.testInfo.examNotice)" :key="idx">{{ line }}</p>
                </div>
              </div>
            </div>

            <div class="paper-section">
              <div class="section-title">考试试题</div>
              <el-table :data="previewData.questions || []" border fit style="width: 100%;">
                <el-table-column type="index" label="#" width="50" align="center" />
                <el-table-column prop="question" label="题目" min-width="260" />
                <el-table-column label="选项" min-width="260">
                  <template slot-scope="scope">
                    <div v-if="scope.row.choiceA">A. {{ scope.row.choiceA }}</div>
                    <div v-if="scope.row.choiceB">B. {{ scope.row.choiceB }}</div>
                    <div v-if="scope.row.choiceC">C. {{ scope.row.choiceC }}</div>
                    <div v-if="scope.row.choiceD">D. {{ scope.row.choiceD }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="answer" label="答案" width="120" />
                <el-table-column prop="type" label="类型" width="90" align="center" />
                <el-table-column prop="score" label="分值" width="80" align="center" />
              </el-table>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogPreviewVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="编辑考试" :visible.sync="dialogEditVisible" width="650px" top="8vh">
      <el-form :model="editForm" :rules="editRules" ref="editForm" label-width="110px">
        <el-form-item label="考试名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="editForm.courseId" placeholder="请选择课程" style="width: 100%;">
            <el-option v-for="c in allCourse" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="参考班级" prop="classNoList">
          <el-select
            v-model="editForm.classNoList"
            multiple
            filterable
            collapse-tags
            :loading="classLoading"
            placeholder="请选择参考班级（可多选）"
            style="width: 100%;"
          >
            <el-option v-for="c in classOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师编号" prop="teacherNo">
          <el-input v-model="editForm.teacherNo" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input v-model.number="editForm.totalScore" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker type="datetime" v-model="editForm.startTime" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="testTime">
          <el-input-number v-model="editForm.testTime" :min="1" :max="600" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="开头信息" prop="examIntro">
          <el-input type="textarea" :rows="3" v-model="editForm.examIntro" placeholder="如：考试须知、答题格式、工具限制等" />
        </el-form-item>
        <el-form-item label="注意事项" prop="examNotice">
          <el-input type="textarea" :rows="4" v-model="editForm.examNotice" placeholder="逐条填写注意事项，换行分隔" />
        </el-form-item>
        <el-form-item label="摄像头/AI监控" prop="cameraRequired">
          <el-switch v-model="editForm.cameraRequired" active-text="开启" inactive-text="关闭" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as api from '../api/api'
import moment from 'moment'

export default {
  name: "Tests",
  data() {
    return {
      testVoList: [],
      total: 0,
      page: 1,
      pageSize: 10,
      listLoading: false,
      sels: [],
      filters: {
        name: ""
      },
      allCourse: [],
      classOptions: [],
      classLoading: false,
      classMetaMap: {},
      dialogCreateVisible: false,
      createLoading: false,
      createForm: this.newCreateForm(),
      createRules: {
        name: [
          { required: true, message: "请输入考试名称", trigger: "blur" }
        ],
        courseId: [
          { required: true, message: "请选择课程", trigger: "change" }
        ],
        classNoList: [
          { required: true, message: "请选择参考班级", trigger: "change" }
        ],
        startTime: [
          { required: true, message: "请选择开考时间", trigger: "change" }
        ],
        testTime: [
          { required: true, message: "请输入考试时长", trigger: "change" }
        ]
      },
      dialogPreviewVisible: false,
      previewLoading: false,
      previewData: {
        testInfo: {},
        classNoList: [],
        questions: []
      },
      dialogEditVisible: false,
      editLoading: false,
      editForm: {
        id: null,
        name: "",
        courseId: null,
        teacherNo: "",
        totalScore: 0,
        startTime: "",
        testTime: 60
      },
      editRules: {
        name: [
          { required: true, message: "请输入考试名称", trigger: "blur" }
        ],
        courseId: [
          { required: true, message: "请选择课程", trigger: "change" }
        ],
        classNoList: [
          { required: true, message: "请选择参考班级", trigger: "change" }
        ],
        teacherNo: [
          { required: true, message: "请输入教师编号", trigger: "blur" }
        ],
        totalScore: [
          { required: true, message: "请输入总分", trigger: "blur" }
        ],
        startTime: [
          { required: true, message: "请选择开始时间", trigger: "change" }
        ],
        testTime: [
          { required: true, message: "请输入考试时长", trigger: "change" }
        ]
      }
    }
  },
  mounted() {
    this.getAllCourse()
    this.loadAllClasses()
    this.pageQueryTest()
  },
  methods: {
    statusTagType(row) {
      const s = this.resolveStatus(row)
      if (s === "0") return "info"
      if (s === "1") return "success"
      return "danger"
    },
    statusLabelText(row) {
      const s = this.resolveStatus(row)
      if (s === "0") return "未开始"
      if (s === "1") return "进行中"
      return "已结束"
    },
    resolveStatus(row) {
      const raw = row && row.status != null ? String(row.status) : ''
      const start = this.safeMoment(row && row.startTime)
      const end = this.safeMoment(row && row.endTime)
      const now = moment()

      if (start && end) {
        if (now.isBefore(start)) return '0'
        if (now.isAfter(end)) return '2'
        return '1'
      }

      return (raw === '0' || raw === '1' || raw === '2') ? raw : '0'
    },
    safeMoment(v) {
      if (!v) return null
      const m = moment(v)
      return m.isValid() ? m : null
    },
    formatDateValue(date) {
      return date ? moment(date).format("YYYY-MM-DD HH:mm") : ""
    },
    newCreateForm() {
      return {
        name: "",
        courseId: null,
        startTime: "",
        testTime: 60,
        teacherNo: "",
        totalScore: 0,
        status: "0",
        endTime: "",
        singleRedisId: 0,
        judgeRedisId: 0,
        multipleRedisId: 0,
        subjectiveRedisId: 0,
        singleScore: 2,
        judgeScore: 2,
        multipleScore: 4,
        subjectiveScore: 10,
        classNoList: [],
        singleRandomCount: 0,
        judgeRandomCount: 0,
        multipleRandomCount: 0,
        subjectiveRandomCount: 0,
        examIntro: '',
        examNotice: '',
        cameraRequired: false
      }
    },
    resolveQueryTeacherNo() {
      const roleId = String(sessionStorage.getItem("serverRoleId") || sessionStorage.getItem("roleId") || sessionStorage.getItem("flag") || "")
      const username = sessionStorage.getItem("username") || ""
      if (roleId === "0") {
        return "all"
      }
      return sessionStorage.getItem("teacherNo") || username || ""
    },
    resolveCreatorTeacherNo() {
      const roleId = String(sessionStorage.getItem("serverRoleId") || sessionStorage.getItem("roleId") || sessionStorage.getItem("flag") || "")
      const username = sessionStorage.getItem("username") || ""
      if (roleId === "0") {
        return username || "202610000001"
      }
      const teacherNo = sessionStorage.getItem("teacherNo") || ""
      return teacherNo && teacherNo !== "all" ? teacherNo : username || ""
    },
    handleFilter() {
      this.page = 1
      this.pageQueryTest()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.pageQueryTest()
    },
    handleCurrentChange(page) {
      this.page = page
      this.pageQueryTest()
    },
    selsChange(selection) {
      this.sels = selection
    },
    pageQueryTest() {
      this.listLoading = true
      api.pageQueryTest({
        pageNum: this.page,
        pageSize: this.pageSize,
        username: this.resolveQueryTeacherNo(),
        name: this.filters.name || ""
      }).then(res => {
        if (res && res.data && res.data.code === "0") {
          const data = res.data.data || {}
          this.testVoList = data.list || []
          this.total = parseInt(data.total || 0, 10)
        } else {
          this.testVoList = []
          this.total = 0
        }
      }).catch(() => {
        this.testVoList = []
        this.total = 0
        this.$message.error("请求失败，请检查网络连接或稍后重试。")
      }).finally(() => {
        this.listLoading = false
      })
    },
    getAllCourse() {
      api.getAllCourse().then(res => {
        if (res && res.data && res.data.code === '0') {
          this.allCourse = res.data.data || []
        }
      })
    },
    async loadAllClasses() {
      this.classLoading = true
      try {
        const classRes = await api.listClasses()
        const classes = (classRes && classRes.data && classRes.data.code === '0') ? (classRes.data.data || []) : []
        const all = []
        const metaMap = {}

        for (const c of classes) {
          const classNo = c.classNo || c.classCo || ''
          if (!classNo) continue
          const label = c.className || classNo
          all.push({ value: classNo, label })
          metaMap[classNo] = {
            classNo,
            majorName: c.majorName || '',
            instituteName: c.instituteName || '',
            collegeNo: c.collegeNo || ''
          }
        }

        const map = new Map()
        for (const item of all) {
          if (!map.has(item.value)) map.set(item.value, item)
        }
        this.classOptions = Array.from(map.values()).sort((a, b) => String(a.value).localeCompare(String(b.value)))
        this.classMetaMap = metaMap
      } catch (e) {
        this.classOptions = []
      } finally {
        this.classLoading = false
      }
    },
    openCreate() {
      this.createForm = this.newCreateForm()
      this.dialogCreateVisible = true
      this.$nextTick(() => {
        if (this.$refs.createForm) this.$refs.createForm.clearValidate()
      })
    },
    recalcTotal() {
      const f = this.createForm
      this.createForm.totalScore =
        (Number(f.singleRandomCount) || 0) * (Number(f.singleScore) || 0) +
        (Number(f.judgeRandomCount) || 0) * (Number(f.judgeScore) || 0) +
        (Number(f.multipleRandomCount) || 0) * (Number(f.multipleScore) || 0) +
        (Number(f.subjectiveRandomCount) || 0) * (Number(f.subjectiveScore) || 0)
    },
    submitCreate() {
      this.$refs.createForm.validate(valid => {
        if (!valid) return
        this.recalcTotal()
        if (!this.createForm.totalScore || this.createForm.totalScore <= 0) {
          this.$message.warning('题目总分必须大于0')
          return
        }

        const payload = { ...this.createForm }
        payload.teacherNo = this.resolveCreatorTeacherNo()
        payload.startTime = moment(payload.startTime).format('YYYY-MM-DD HH:mm:ss')
        payload.cameraRequired = Boolean(payload.cameraRequired)

        this.createLoading = true
        this.$confirm('确认提交考试配置？发布后不可修改。', '提示', { type: 'warning' })
          .then(() => api.createTest(payload))
          .then(res => {
            if (res && res.data && res.data.code === '0') {
              this.$message.success('创建成功')
              this.dialogCreateVisible = false
              this.pageQueryTest()
            } else {
              this.$message.error((res && res.data && res.data.message) || '创建失败')
            }
          })
          .finally(() => {
            this.createLoading = false
          })
      })
    },
    batchRemove() {
      const ids = this.sels.map(item => item.id).filter(Boolean).toString()
      if (!ids) return
      this.$confirm('确认删除选中的考试吗？', '提示', { type: 'warning' }).then(() => {
        this.listLoading = true
        api.deleteTestByIds(ids)
          .then(() => {
            this.$message.success('删除成功')
            this.pageQueryTest()
          })
          .finally(() => {
            this.listLoading = false
          })
      })
    },
    handleDel(row) {
      this.$confirm('确认删除该考试吗？', '提示', { type: 'warning' }).then(() => {
        api.deleteTestByIds(row.id).then(res => {
          if (res && res.data && res.data.code === '0') {
            this.$message.success('删除成功')
            this.pageQueryTest()
          } else {
            this.$message.error('删除失败')
          }
        })
      })
    },
    handlePreview(row) {
      this.dialogPreviewVisible = true
      this.previewLoading = true

      const baseInfo = {
        testInfo: {
          id: row.id,
          name: row.name,
          courseId: row.courseId,
          teacherNo: row.teacherNo,
          teacherName: row.teacherName,
          totalScore: row.totalScore,
          status: row.status,
          startTime: row.startTime,
          endTime: row.endTime,
          testTime: row.testTime,
          examIntro: row.examIntro || '',
          examNotice: row.examNotice || '',
          cameraRequired: row.cameraRequired,
        },
        classNoList: row.classNoList || [],
        questions: [],
      }

      api.previewTest(row.id)
        .then(res => {
          if (res && res.data && res.data.code === '0') {
            this.previewData = res.data.data || { testInfo: {}, classNoList: [], questions: [] }
          } else {
            return api.getQuestionByTestNo(row.id)
              .then(qRes => {
                const qList = (qRes && qRes.data && qRes.data.code === '0' && Array.isArray(qRes.data.data)) ? qRes.data.data : []
                this.previewData = { ...baseInfo, questions: qList }
              })
              .catch(() => {
                this.previewData = baseInfo
              })
              .finally(() => {
                this.$message.warning((res && res.data && res.data.message) || '预览题目加载失败，已显示基础信息')
              })
          }
        })
        .catch(() => {
          this.previewData = baseInfo
          this.$message.warning('预览题目加载失败，已显示基础信息')
        })
        .finally(() => {
          this.previewLoading = false
        })
    },
    previewTeacherDisplay(info = {}) {
      const displayName = info.teacherRealName || info.teacherName
      const teacherNo = info.teacherNo || ''
      const title = info.teacherTitle || info.title || info.roleName || info.teacherRole || '教师'
      const main = displayName || teacherNo || '未知教师'
      const noPart = teacherNo && teacherNo !== displayName ? ` / ${teacherNo}` : ''
      return `${main}${noPart}（${title}）`
    },
    resolveCourseName(courseId) {
      if (!courseId) return '—'
      const target = this.allCourse.find(c => String(c.id) === String(courseId))
      return (target && target.name) || '—'
    },
    previewTimeRange(info = {}) {
      const start = this.formatDateValue(info.startTime)
      const end = this.formatDateValue(info.endTime)
      if (start && end) return `${start} ~ ${end}`
      return start || end || '—'
    },
    splitNotice(text) {
      if (!text) return []
      return String(text)
        .split(/\r?\n/)
        .map(line => line.trim())
        .filter(Boolean)
    },
    previewInstituteName() {
      const info = this.previewData.testInfo || {}
      if (info.instituteName) return info.instituteName
      if (info.collegeName) return info.collegeName
      const classes = this.previewData.classNoList || []
      if (!classes.length) return '—'
      const meta = this.classMetaMap[classes[0]]
      return (meta && meta.instituteName) || '—'
    },
    previewMajorName() {
      const info = this.previewData.testInfo || {}
      if (info.majorName) return info.majorName
      const classes = this.previewData.classNoList || []
      if (!classes.length) return '—'
      const meta = this.classMetaMap[classes[0]]
      return (meta && meta.majorName) || '—'
    },
    openEdit(row) {
      this.dialogEditVisible = true
      this.editLoading = true
      api.previewTest(row.id)
        .then(res => {
          if (res && res.data && res.data.code === '0') {
            const data = res.data.data || { testInfo: {}, classNoList: [] }
            const info = data.testInfo || {}
            this.editForm = {
              id: info.id || row.id,
              name: info.name || row.name,
              courseId: info.courseId || row.courseId || null,
              teacherNo: info.teacherNo || row.teacherNo || this.resolveCreatorTeacherNo(),
              totalScore: parseInt(info.totalScore != null ? info.totalScore : (row.totalScore || 0), 10) || 0,
              startTime: info.startTime ? new Date(info.startTime) : (row.startTime ? new Date(row.startTime) : ''),
              testTime: parseInt(info.testTime != null ? info.testTime : (row.testTime || 60), 10),
              examIntro: info.examIntro || info.exam_intro || '',
              examNotice: info.examNotice || info.exam_notice || '',
              cameraRequired: info.cameraRequired != null ? Boolean(info.cameraRequired) : true,
              status: info.status || row.status || '0',
              classNoList: Array.isArray(data.classNoList) ? data.classNoList : (row.classNoList || [])
            }
          } else {
            this.editForm = {
              id: row.id,
              name: row.name || '',
              courseId: row.courseId || null,
              teacherNo: row.teacherNo || this.resolveCreatorTeacherNo(),
              totalScore: parseInt(row.totalScore || 0, 10) || 0,
              startTime: row.startTime ? new Date(row.startTime) : '',
              testTime: parseInt(row.testTime || 60, 10),
              examIntro: row.examIntro || '',
              examNotice: row.examNotice || '',
              cameraRequired: row.cameraRequired != null ? Boolean(row.cameraRequired) : true,
              status: row.status || '0',
              classNoList: Array.isArray(row.classNoList) ? row.classNoList : []
            }
            this.$message.warning((res && res.data && res.data.message) || '加载题目详情失败，已切换基础编辑模式')
          }
        })
        .catch(() => {
          this.editForm = {
            id: row.id,
            name: row.name || '',
            courseId: row.courseId || null,
            teacherNo: row.teacherNo || this.resolveCreatorTeacherNo(),
            totalScore: parseInt(row.totalScore || 0, 10) || 0,
            startTime: row.startTime ? new Date(row.startTime) : '',
            testTime: parseInt(row.testTime || 60, 10),
            examIntro: row.examIntro || '',
            examNotice: row.examNotice || '',
            cameraRequired: row.cameraRequired != null ? Boolean(row.cameraRequired) : true,
            status: row.status || '0',
            classNoList: Array.isArray(row.classNoList) ? row.classNoList : []
          }
          this.$message.warning('加载题目详情失败，已切换基础编辑模式')
        })
        .finally(() => {
          this.editLoading = false
          this.$nextTick(() => {
            if (this.$refs.editForm) this.$refs.editForm.clearValidate()
          })
        })
    },
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (!valid) return

        const payload = {
          id: this.editForm.id,
          name: this.editForm.name,
          courseId: this.editForm.courseId,
          teacherNo: this.editForm.teacherNo,
          totalScore: Number(this.editForm.totalScore),
          startTime: moment(this.editForm.startTime).format('YYYY-MM-DD HH:mm:ss'),
          testTime: Number(this.editForm.testTime),
          examIntro: this.editForm.examIntro || '',
          examNotice: this.editForm.examNotice || '',
          cameraRequired: Boolean(this.editForm.cameraRequired),
          classNoList: Array.isArray(this.editForm.classNoList) ? this.editForm.classNoList : []
        }

        this.editLoading = true
        api.updateTest(payload.id, payload)
          .then(res => {
            if (res && res.data && res.data.code === '0') {
              this.$message.success('更新成功')
              this.dialogEditVisible = false
              this.pageQueryTest()
            } else {
              this.$message.error((res && res.data && res.data.message) || '更新失败')
            }
          })
          .finally(() => {
            this.editLoading = false
          })
      })
    }
  }
}
</script>

<style scoped>
.app-container { padding: 20px; }
.box-card { margin-bottom: 20px; }
.filter-container { padding-bottom: 20px; }
.filter-item { display: inline-block; vertical-align: middle; margin-bottom: 10px; }
.pagination-container { margin-top: 20px; text-align: right; }
.section-title { font-weight: 600; color: #303133; margin-bottom: 8px; }
.text-muted { color: #909399; font-size: 12px; }
.preview-dialog-body { max-height: 70vh; overflow-y: auto; }
.preview-exam-layout { display: flex; flex-direction: column; gap: 16px; }
.exam-info-card { padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; background: #fdfdfd; }
.exam-title { font-size: 20px; font-weight: 600; margin-bottom: 12px; color: #1d1d1d; }
.exam-meta-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 8px 14px; margin-bottom: 12px; }
.exam-meta-grid > div { display: flex; align-items: flex-start; line-height: 1.5; white-space: normal; overflow: hidden; text-overflow: ellipsis; }
.meta-label { color: #909399; margin-right: 6px; flex-shrink: 0; }
.meta-label::after { content: '：'; }
.meta-value { color: #303133; font-weight: 500; min-width: 0; overflow: hidden; text-overflow: ellipsis; line-height: 1.5; }
.time-value { white-space: normal; line-height: 1.5; }
.exam-notice { margin-top: 8px; padding: 12px; border-radius: 6px; background: #fff6e5; border: 1px solid #ffe2b7; }
.notice-title { font-weight: 600; color: #d48806; margin-bottom: 8px; }
.notice-content p { margin: 0 0 6px; line-height: 1.6; color: #6b4a13; }
.exam-intro { margin-top: 8px; padding: 12px; border-radius: 6px; background: #eef5ff; border: 1px solid #c8dcff; }
</style>