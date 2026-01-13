<template>
  <div class="upload-page">
    <el-card class="upload-card">
      <div slot="header" class="header-row">
        <span>{{ i18n.pageTitle }}</span>
        <span class="header-tip">{{ i18n.pageTip }}</span>
      </div>

      <div class="upload-grid">
        <div v-for="item in uploadItems" :key="item.key" class="upload-block">
          <div class="block-title">{{ item.title }}</div>

          <el-upload
            drag
            :action="item.action"
            :headers="uploadHeaders"
            :data="{ type: item.type }"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="(res, file) => handleUploadSuccess(item, res, file)"
            :on-error="(err, file) => handleUploadError(item, err, file)"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              {{ i18n.dragPrefix }}{{ item.title }}{{ i18n.dragSuffix }}
              <em>{{ i18n.clickUpload }}</em>
            </div>
            <div class="el-upload__tip" slot="tip">{{ i18n.uploadTip }}</div>
          </el-upload>

          <div class="template-line">
            <el-button size="mini" type="primary" icon="el-icon-download" @click="downloadTemplate(item.template)">
              {{ i18n.downloadPrefix }}{{ item.title }}{{ i18n.downloadSuffix }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
const BACKAGE_ORIGIN = (typeof window !== 'undefined' && ['8084', '8085'].includes(String(window.location.port)))
  ? '/backage-api'
  : 'http://localhost:8094/backage-api';

const I18N = {
  pageTitle: '\u8bd5\u9898\u4e0a\u4f20',
  pageTip: '\u4ec5\u652f\u6301 xls/xlsx\uff0c\u5355\u4e2a\u6587\u4ef6\u5927\u5c0f\u4e0d\u8d85\u8fc7 1MB',
  dragPrefix: '\u5c06',
  dragSuffix: '\u6587\u4ef6\u62d6\u5230\u6b64\u5904\uff0c\u6216',
  clickUpload: '\u70b9\u51fb\u4e0a\u4f20',
  uploadTip: '\u8bf7\u5148\u4e0b\u8f7d\u6a21\u677f\uff0c\u6309\u6a21\u677f\u683c\u5f0f\u586b\u5199\u540e\u518d\u4e0a\u4f20',
  downloadPrefix: '\u4e0b\u8f7d',
  downloadSuffix: '\u6a21\u677f',
  single: '\u5355\u9009\u9898',
  judge: '\u5224\u65ad\u9898',
  multiple: '\u591a\u9009\u9898',
  subjective: '\u4e3b\u89c2\u9898',
  extError: '\u4ec5\u652f\u6301 xls/xlsx \u6587\u4ef6',
  sizeError: '\u6587\u4ef6\u5927\u5c0f\u4e0d\u80fd\u8d85\u8fc7 1MB',
  uploadOkTitle: '\u4e0a\u4f20\u6210\u529f',
  confirm: '\u786e\u5b9a',
  uploadFail: '\u4e0a\u4f20\u5931\u8d25',
  error: '\u9519\u8bef',
  uploadCountPrefix: '\u6210\u529f\u4e0a\u4f20 ',
  uploadCountMid: ' \u9053',
  uploadCountSuffix: ''
};

export default {
  name: 'UploadQuestion',
  data() {
    return {
      i18n: I18N,
      uploadHeaders: {
        AUTHORIZATION: sessionStorage.getItem('AUTHORIZATION') || ''
      }
    };
  },
  computed: {
    uploadItems() {
      return [
        {
          key: 'single',
          title: this.i18n.single,
          type: '1',
          action: `${BACKAGE_ORIGIN}/question/choice/file`,
          template: `${BACKAGE_ORIGIN}/file/single`
        },
        {
          key: 'judge',
          title: this.i18n.judge,
          type: '2',
          action: `${BACKAGE_ORIGIN}/question/choice/file`,
          template: `${BACKAGE_ORIGIN}/file/judge`
        },
        {
          key: 'multiple',
          title: this.i18n.multiple,
          type: '3',
          action: `${BACKAGE_ORIGIN}/question/choice/file`,
          template: `${BACKAGE_ORIGIN}/file/multiple`
        },
        {
          key: 'subjective',
          title: this.i18n.subjective,
          type: '4',
          action: `${BACKAGE_ORIGIN}/question/subjective/file`,
          template: `${BACKAGE_ORIGIN}/file/subjective`
        }
      ];
    }
  },
  methods: {
    beforeUpload(file) {
      const name = file.name || '';
      const ext = name.split('.').pop().toLowerCase();
      const isExcel = ext === 'xls' || ext === 'xlsx';
      const isLt1M = file.size / 1024 / 1024 < 1;

      if (!isExcel) {
        this.$message.error(this.i18n.extError);
        return false;
      }
      if (!isLt1M) {
        this.$message.error(this.i18n.sizeError);
        return false;
      }
      return true;
    },
    handleUploadSuccess(item, res) {
      if (res && res.code === '0') {
        const count = res.data || 0;
        const msg = `${this.i18n.uploadCountPrefix}${count}${this.i18n.uploadCountMid}${item.title}${this.i18n.uploadCountSuffix}`;
        this.$alert(msg, this.i18n.uploadOkTitle, {
          confirmButtonText: this.i18n.confirm,
          type: 'success'
        });
        return;
      }
      this.$alert((res && res.message) || this.i18n.uploadFail, this.i18n.error, {
        confirmButtonText: this.i18n.confirm,
        type: 'error'
      });
    },
    handleUploadError(item, err) {
      const fallback = `${item.title}${this.i18n.uploadFail}`;
      const msg = (err && err.message) ? err.message : fallback;
      this.$alert(msg, this.i18n.error, {
        confirmButtonText: this.i18n.confirm,
        type: 'error'
      });
    },
    downloadTemplate(url) {
      window.open(url, '_blank');
    }
  }
};
</script>

<style scoped>
.upload-page {
  padding: 20px;
  min-width: 1080px;
  overflow-x: auto;
}

.upload-card {
  min-height: 520px;
  min-width: 1040px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-tip {
  color: #909399;
  font-size: 12px;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(320px, 1fr));
  gap: 20px;
}

.upload-block {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.block-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.template-line {
  margin-top: 12px;
}

@media (max-width: 900px) {
  .upload-grid {
    grid-template-columns: 1fr;
  }
}
</style>

