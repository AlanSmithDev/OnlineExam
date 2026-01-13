<template>
  <div class="student-page-container">
    <el-card class="student-page-card">
      <div class="debug-bar">
        当前查询学号：<span class="debug-val">{{ username || '未识别' }}</span>
        <span class="debug-sep">|</span>
        请求地址：<span class="debug-val">{{ lastRequestUrl || '未请求' }}</span>
        <span class="debug-sep">|</span>
        返回条数：<span class="debug-val">{{ responseCount }}</span>
      </div>
      <el-table :data="dataList" height="350" stripe style="width: 100%">
        <el-table-column prop="name" label="考试名称" width="180">
        </el-table-column>
        <el-table-column prop="courseName" label="所属课程" width="180">
        </el-table-column>
        <el-table-column prop="teacherName" label="任课教师"> </el-table-column>
        <el-table-column prop="totalScore" label="总分"> </el-table-column>
        <el-table-column prop="startTime" label="开考时间"> </el-table-column>
        <el-table-column prop="testTime" label="考试时长"> </el-table-column>
        <el-table-column label="考试状态">
          <template v-slot:default="scope">
            <el-button
              size="mini"
              type="info"
              disabled
              v-if="scope.row.statusStr === '0'"
            >
              未开始
            </el-button>
            <el-button
              size="mini"
              type="primary"
              @click="handleStartExam(scope.row)"
              v-else-if="scope.row.statusStr === '1'"
              >进入考试
            </el-button>
            <el-button
              size="mini"
              type="success"
              disabled
              v-else-if="scope.row.statusStr === '2'"
              >已结束
            </el-button>
            <el-button size="mini" type="info" disabled v-else
              >不可参加</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="js">
export default {
  name: "ShowTests",
  data() {
    return {
      dataList: [],
      username: '',
      lastRequestUrl: '',
      responseCount: 0
    }
  },
  mounted() {
    this.loadData();//拿到上一个页面传的值
  },
  methods: {
    loadData() {
      const token = sessionStorage.getItem('AUTHORIZATION') || sessionStorage.getItem('authorization')

      const sessionUsername = this.normalizeStudentNo(
        sessionStorage.getItem('USERNAME')
        || sessionStorage.getItem('username')
        || sessionStorage.getItem('userNo')
        || ''
      )
      const queryUsername = this.normalizeStudentNo(this.$route.query.username || '')
      this.username = sessionUsername || queryUsername
      if (!sessionUsername && queryUsername) {
        sessionStorage.setItem('USERNAME', queryUsername)
        sessionStorage.setItem('username', queryUsername)
      }

      if (!this.username) {
        console.warn('Tests.loadData missing username', { username: this.username })
        this.responseCount = 0
        this.$router.replace({ path: '/' })
        return
      }

      const url = '/front-api/test/student/' + encodeURIComponent(this.username)
      this.lastRequestUrl = url
      console.debug('Tests.loadData requesting', url)

      // Try a single request, but provide better error messages
      this.$axios.get(url, { headers: token ? { 'AUTHORIZATION': token } : {} })
        .then(response => {
          console.log('loadData response:', response)
          if (response.status === 200) {
            const respData = response.data
            // backend sometimes wraps data differently; handle both
            const raw = (respData && respData.data !== undefined) ? respData.data : respData
            if (respData && respData.code && respData.code !== '0') {
              // Specific backend error codes: show meaningful message
              const msg = respData.message || '加载考试信息失败（服务端返回错误）'
              this.$message.error(msg)
              console.error('Backend returned error code on tests list', respData)
              this.dataList = []
              this.responseCount = 0
              return
            }

            const arr = Array.isArray(raw) ? raw : (raw ? [raw] : [])
            // normalize status on each item and set dataList directly
            this.dataList = arr.map(item => ({ ...item, statusStr: this.normalizeStatus(item) }))
            this.responseCount = this.dataList.length
            this.sortTestsLikeAdmin()
            if (!this.dataList.length) {
              this.$message.warning('暂时没有可参加的考试')
            }
          } else {
            this.$message.error('加载考试信息失败：非 200 返回')
            console.error('Unexpected HTTP status when loading tests:', response.status, response)
          }
        })
        .catch(err => {
          console.error('loadData error', err)
          if (err && err.response) {
            const code = err.response.status
            if (code === 403 || code === 401) {
              this.$message.error('未授权或会话已过期，请重新登录')
              sessionStorage.clear()
              this.$router.replace({ path: '/' })
            } else {
              this.$message.error('加载考试信息失败：服务器错误 ' + code)
            }
          } else {
            this.$message.error('加载考试信息失败：网络异常或后端未启动')
          }
          this.dataList = []
          this.responseCount = 0
        })
    },
     logout() {
       sessionStorage.removeItem('AUTHORIZATION')
       sessionStorage.removeItem('USERNAME')
       sessionStorage.removeItem('username')
       this.$router.replace({ path: '/' })
     },
     sortTestsLikeAdmin() {
       this.dataList.sort((a, b) => {
         const t1 = this.toMillis(a.startTime)
         const t2 = this.toMillis(b.startTime)
         if (t1 !== t2) return t2 - t1
         return Number(b.id || 0) - Number(a.id || 0)
       })
     },
     toMillis(v) {
       if (!v) return 0
       if (typeof v === 'number') return v
       const raw = String(v)
       const ts = Date.parse(raw.replace(/-/g, '/'))
       if (!Number.isNaN(ts)) return ts
       const d = new Date(raw)
       const t = d.getTime()
       return Number.isNaN(t) ? 0 : t
     },
     async handleStartExam(row) {
       // 入口：确保摄像头开启
       const ok = await this.ensureCamera();
       if (!ok) {
         return;
       }
       this.goExam(row);
     },
     async ensureCamera() {
       if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
         this.$alert('当前浏览器不支持摄像头访问，无法参加考试。', '提示', { type: 'warning' });
         return false;
       }
       try {
         const stream = await navigator.mediaDevices.getUserMedia({ video: true });
         // 立即停止流，避免一直占用摄像头；考试页可再次打开
         stream.getTracks().forEach(t => t.stop());
         return true;
       } catch (e) {
         this.$alert('未授予摄像头访问权限，无法参加考试，请允许浏览器访问摄像头后重试。', '提示', { type: 'warning' });
         return false;
       }
     },
     goExam(row) {
       // 统一前往考试页面的路由，保留足够字段以便考试页显示
       this.$router.push({
         path: '/exam/take',
         query: {
           testNo: row.id,
           testName: row.name,
           courseName: row.courseName,
           teacherName: row.teacherName,
           startTime: row.startTime,
           endTime: row.endTime,
           totalScore: row.totalScore,
           testTime: row.testTime,
           examNotice: row.examNotice || row.exam_notice || '',
           collegeName: row.collegeName || row.instituteName || '',
           majorName: row.majorName || '',
           className: row.className || (row.classNames ? row.classNames : ''),
           studentName: sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username') || this.username,
           studentNo: sessionStorage.getItem('USERNAME') || sessionStorage.getItem('username') || this.username
         }
       })
     },
     getQuestion(test) {
       console.log('getQuestion::', test)
       const AUTHORIZATION = this.$route.query.AUTHORIZATION || sessionStorage.getItem('AUTHORIZATION') || ''
       if (AUTHORIZATION === '') {
         this.$router.push({ path: '/' })
       } else {
         console.log('AUTHORIZATION::', AUTHORIZATION)
         this.$axios.get('/front-api/test/' + test.id, {headers: {'AUTHORIZATION': AUTHORIZATION}})
           .then(res => {
             console.log(res)
             if (res.status === 200 && res.data['code'] === '0') {
               const profile = this.collectStudentProfile()
               const routeQuery = this.buildExamRouteQuery(test, res.data['data'], AUTHORIZATION, profile)
               this.$router.push({ path: '/exam/take', query: routeQuery })
              } else {
               this.$message.error(res.data['message'] || '获取试题失败')
             }
           })
           .catch(err => {
             console.log(err)
             this.$message.error('网络异常，请稍后重试')
           })
       }
     },
     collectStudentProfile() {
       if (typeof window === 'undefined' || !window.sessionStorage) return {}
       const store = window.sessionStorage
       return {
         name: store.getItem('REALNAME') || store.getItem('USERNAME') || store.getItem('username') || '',
         className: store.getItem('CLASS_NAME') || store.getItem('className') || '',
         collegeName: store.getItem('COLLEGE_NAME') || store.getItem('collegeName') || '',
         instituteName: store.getItem('INSTITUTE_NAME') || store.getItem('instituteName') || '',
         majorName: store.getItem('MAJOR_NAME') || store.getItem('majorName') || ''
       }
     },
     buildExamRouteQuery(test, questionList, AUTHORIZATION, profile = {}) {
       const classNames = []
       if (Array.isArray(test.classNameList)) classNames.push(...test.classNameList)
       if (test.classNames) classNames.push(...String(test.classNames).split(',').map(item => item.trim()))
       if (test.className) classNames.push(test.className)
       const uniqueClasses = Array.from(new Set(classNames.filter(Boolean)))
       return {
         questionList,
         AUTHORIZATION,
         username: this.username,
         testNo: test.id,
         testName: test.name || test.testName || '',
         testTime: test.testTime || test.test_time || 60,
         totalScore: test.totalScore || test.total_score || '',
         courseId: test.courseId || test.course_id || '',
         courseName: test.courseName || '',
         teacherName: test.teacherName || '',
         paperCreator: test.paperCreator || test.teacherName || '',
         startTime: test.startTime || '',
         endTime: test.endTime || '',
         examNotice: test.examNotice || test.exam_notice || '',
         classNames: uniqueClasses.join(','),
         collegeName: test.collegeName || '',
         instituteName: test.instituteName || '',
         majorName: test.majorName || '',
         studentName: profile.name || this.username,
         studentClassName: profile.className || '',
         studentCollegeName: profile.collegeName || '',
         studentInstituteName: profile.instituteName || '',
         studentMajorName: profile.majorName || ''
       }
     },
     normalizeStudentNo(v) {
       const raw = String(v || '').trim()
       if (!raw) return ''
       const idx = raw.indexOf(':')
       return idx > -1 ? raw.slice(idx + 1).trim() : raw
     },
     normalizeStatus(item) {
       const start = this.toMillis(item && item.startTime)
       const end = this.toMillis(item && item.endTime)
       const now = Date.now()
       if (start && end) {
         if (now < start) return '0'
         if (now > end) return '2'
         return '1'
       }
       const value = String(item && item.status != null ? item.status : '')
       return (value === '0' || value === '1' || value === '2') ? value : '0'
     },
     // Fallback: try old-style backage-api endpoint used by some backends
     tryFallbackFetch(username, token, attemptedUrl) {
       const alt = '/backage-api/test/test/1/10/' + encodeURIComponent(username) + '?name='
       console.warn('Fallback: trying', alt, 'because', attemptedUrl)
       this.$axios.get(alt, { headers: { 'AUTHORIZATION': token } })
         .then(res => {
           console.log('fallback response', res)
           if (res.status === 200) {
             const data = res.data
             // some older endpoints return { data: [...] } or plain array
             const raw = (data && data.data !== undefined) ? data.data : data
             const arr = Array.isArray(raw) ? raw : (raw ? [raw] : [])
             // Map fields if backend returns different names
             this.dataList = arr.map(it => ({
               id: it.id || it.test_no || it.testNo,
               name: it.name || it.test_name || it.testName,
               courseName: it.courseName || it.course_name || '',
               teacherName: it.teacherName || it.teacher_no || '',
               totalScore: it.totalScore || it.total_score || it.total || '',
               startTime: it.startTime || it.start_time || it.start,
               endTime: it.endTime || it.end_time || it.end,
               testTime: it.testTime || it.test_time || it.test_time || it.testTime,
               statusStr: this.normalizeStatus(it)
             }))
             this.sortTestsLikeAdmin()
             if (!this.dataList.length) this.$message.info('备用接口也未返回考试数据')
           } else {
             this.$message.error('备用接口返回非 200 状态: ' + res.status)
           }
         })
         .catch(e => {
           console.error('fallback failed', e)
           this.$message.error('备用接口调用失败，请确认后端服务（backage）已启动，或检查代理配置（front/config/index.js）')
           // 尝试直接访问常用后端端口（绕过 dev-server proxy），以便快速定位问题（可能被 CORS 拦截）
           this.tryDirectFetch(username, token)
         })
      },

      // 最后的尝试：直接请求后端常用端口（127.0.0.1:8081/8083/8082）用于快速排查代理或后端未启动问题
      tryDirectFetch(username, token) {
        const hosts = ['http://127.0.0.1:8081', 'http://127.0.0.1:8083']
        const path = '/test/student/' + encodeURIComponent(username)
        let tried = 0
        const tryNext = () => {
          if (tried >= hosts.length) {
            this.$message.error('已尝试所有直接后端地址，仍无法连接。请确认后端服务已启动并检查代理配置。')
            return
          }
          const full = hosts[tried] + path
          console.warn('Direct fetch attempt', full)
          this.$axios.get(full, { headers: { 'AUTHORIZATION': token }, timeout: 5000 })
            .then(r => {
              console.log('direct fetch success', full, r)
              if (r.status === 200) {
                const raw = (r.data && r.data.data !== undefined) ? r.data.data : r.data
                const arr = Array.isArray(raw) ? raw : (raw ? [raw] : [])
                this.dataList = arr.map(item => ({ ...item, statusStr: this.normalizeStatus(item) }))
                this.sortTestsLikeAdmin()
                if (!this.dataList.length) this.$message.info('直接接口返回但无数据')
              } else {
                this.$message.error('直接接口返回非 200：' + r.status + '，请检查后端')
              }
            })
            .catch(er => {
              console.warn('direct fetch failed for', full, er && er.message)
              tried++
              tryNext()
            })
        }
        tryNext()
      },
   }
 }
</script>

<style scoped>
.student-page-container {
  min-height: 100vh;
  padding: 40px;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.student-page-card {
  max-width: 960px;
  margin: 0 auto;
}

.debug-bar {
  margin-bottom: 12px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f2f6fc;
  color: #606266;
  font-size: 12px;
  word-break: break-all;
}

.debug-val {
  color: #409EFF;
  font-weight: 600;
}

.debug-sep {
  margin: 0 8px;
  color: #c0c4cc;
}
</style>
