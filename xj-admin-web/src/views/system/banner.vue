<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>Banner管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加Banner
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="imageUrl" label="图片" width="200">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" :preview-src-list="[row.imageUrl]" style="width: 160px; height: 60px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="linkType" label="链接类型" width="100">
          <template #default="{ row }">{{ linkTypeText(row.linkType) }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑Banner' : '添加Banner'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="Banner图片" prop="imageUrl">
          <el-upload
            class="banner-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="banner-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：750 x 280</div>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="链接类型">
          <el-select v-model="form.linkType" placeholder="请选择">
            <el-option label="无链接" :value="0" />
            <el-option label="线路详情" :value="1" />
            <el-option label="外部链接" :value="2" />
            <el-option label="小程序页面" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.linkType > 0" label="链接值">
          <el-input v-model="form.linkValue" :placeholder="form.linkType === 1 ? '线路ID' : '链接地址'" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBannerListAll, createBanner, updateBanner, updateBannerStatus, deleteBanner } from '@/api/banner'
import type { BannerVO } from '@/types'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const tableData = ref<BannerVO[]>([])

const uploadAction = computed(() => `${import.meta.env.VITE_API_BASE_URL}/admin/file/upload`)
const uploadHeaders = computed(() => ({
  Authorization: localStorage.getItem('token') || '',
}))

const form = reactive({
  id: null as number | null,
  imageUrl: '',
  title: '',
  linkType: 0,
  linkValue: '',
  sortOrder: 0,
})

const rules: FormRules = {
  imageUrl: [{ required: true, message: '请上传Banner图片', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

function linkTypeText(type: number) {
  const map: Record<number, string> = { 0: '无链接', 1: '线路详情', 2: '外部链接', 3: '小程序页面' }
  return map[type] || '未知'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    tableData.value = await getBannerListAll()
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.imageUrl = ''
  form.title = ''
  form.linkType = 0
  form.linkValue = ''
  form.sortOrder = 0
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: BannerVO) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    imageUrl: row.imageUrl,
    title: row.title,
    linkType: row.linkType,
    linkValue: row.linkValue,
    sortOrder: row.sortOrder,
  })
  dialogVisible.value = true
}

function handleUploadSuccess(response: any) {
  if (response.code === 0) {
    form.imageUrl = response.data
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBanner({
        id: form.id!,
        imageUrl: form.imageUrl,
        title: form.title,
        linkType: form.linkType,
        linkValue: form.linkValue,
        sortOrder: form.sortOrder,
      })
    } else {
      await createBanner({
        imageUrl: form.imageUrl,
        title: form.title,
        linkType: form.linkType,
        linkValue: form.linkValue,
        sortOrder: form.sortOrder,
      })
    }
    ElMessage.success(isEdit.value ? '保存成功' : '添加成功')
    dialogVisible.value = false
    fetchData()
  } catch (_e) { /* ignore */ }
  finally {
    submitLoading.value = false
  }
}

async function handleStatusChange(row: BannerVO) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateBannerStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch (_e) { /* ignore */ }
}

async function handleDelete(row: BannerVO) {
  try {
    await ElMessageBox.confirm('确定删除该Banner吗？', '提示', { type: 'warning' })
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (_e) { /* ignore */ }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.banner-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; width: 300px; height: 112px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.banner-uploader:hover { border-color: #409eff; }
.banner-image { width: 300px; height: 112px; object-fit: cover; }
.uploader-icon { font-size: 28px; color: #8c939d; }
.upload-tip { font-size: 12px; color: #909399; margin-top: 8px; }
</style>
