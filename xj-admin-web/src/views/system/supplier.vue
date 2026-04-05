<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>供应商管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加供应商
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="供应商名称" min-width="160" />
        <el-table-column prop="companyName" label="公司名称" min-width="160" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="username" label="登录账号" width="120" />
        <el-table-column prop="routeCount" label="线路数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPassword(row)">重置密码</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 编辑抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="isEdit ? '编辑供应商' : '添加供应商'"
      size="820px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">

        <!-- 基本信息 -->
        <div class="section-title">基本信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称">
              <el-input v-model="form.companyName" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公司地址">
              <el-input v-model="form.companyAddress" placeholder="请输入公司地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Logo">
              <div class="logo-uploader">
                <template v-if="form.logo">
                  <el-image
                    :src="form.logo"
                    :preview-src-list="[form.logo]"
                    preview-teleported
                    fit="cover"
                    class="logo-preview"
                  />
                  <el-upload
                    :action="uploadAction"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="(res: any) => handleSingleUpload(res, 'logo')"
                  >
                    <el-button size="small" link type="primary" class="reupload-btn">重新上传</el-button>
                  </el-upload>
                </template>
                <el-upload
                  v-else
                  class="logo-upload-trigger"
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="(res: any) => handleSingleUpload(res, 'logo')"
                >
                  <div class="logo-placeholder">
                    <el-icon class="uploader-icon"><Plus /></el-icon>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介">
              <el-input v-model="form.intro" type="textarea" :rows="2" placeholder="供应商简介" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 账号信息（仅新增时显示） -->
        <template v-if="!isEdit">
          <div class="section-title">账号信息</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="登录账号" prop="username">
                <el-input v-model="form.username" placeholder="请输入登录账号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="初始密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 联系人 -->
        <div class="section-title">联系人</div>
        <div class="contact-label">联系人 1</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="姓名">
              <el-input v-model="form.contact1Name" placeholder="联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职务">
              <el-input v-model="form.contact1Title" placeholder="职务" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="电话">
              <el-input v-model="form.contact1Phone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="contact-label">联系人 2 <span class="optional">（选填）</span></div>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="姓名">
              <el-input v-model="form.contact2Name" placeholder="联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职务">
              <el-input v-model="form.contact2Title" placeholder="职务" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="电话">
              <el-input v-model="form.contact2Phone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 资质证件 -->
        <div class="section-title">资质证件</div>
        <el-row :gutter="24">
          <el-col :span="8" v-for="cert in certFields" :key="cert.field">
            <el-form-item :label="cert.label">
              <div class="cert-uploader">
                <template v-if="(form as any)[cert.field]">
                  <!-- 点击图片预览 -->
                  <el-image
                    :src="(form as any)[cert.field]"
                    :preview-src-list="certPreviewList"
                    :initial-index="certPreviewIndex(cert.field)"
                    preview-teleported
                    fit="cover"
                    class="cert-preview"
                  />
                  <!-- 重新上传按钮 -->
                  <el-upload
                    :action="uploadAction"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="(res: any) => handleSingleUpload(res, cert.field)"
                  >
                    <el-button size="small" link type="primary" class="reupload-btn">
                      <el-icon><RefreshRight /></el-icon> 重新上传
                    </el-button>
                  </el-upload>
                </template>
                <!-- 无图时显示上传占位 -->
                <el-upload
                  v-else
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="(res: any) => handleSingleUpload(res, cert.field)"
                >
                  <div class="cert-placeholder">
                    <el-icon class="uploader-icon"><Plus /></el-icon>
                    <span>点击上传</span>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import {
  getSupplierList,
  createSupplier,
  updateSupplier,
  updateSupplierStatus,
  resetSupplierPassword,
} from '@/api/supplier'
import type { SupplierVO } from '@/types'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const drawerVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const tableData = ref<SupplierVO[]>([])

const uploadAction = computed(() => `${import.meta.env.VITE_API_BASE_URL}/admin/file/upload`)
const uploadHeaders = computed(() => ({
  Authorization: userStore.token || localStorage.getItem('token') || '',
}))

// 资质证件字段配置
const certFields = [
  { field: 'businessLicense', label: '营业执照' },
  { field: 'tourismLicense', label: '旅游业务许可证' },
  { field: 'idCardFront',    label: '法人身份证正面' },
  { field: 'idCardBack',     label: '法人身份证反面' },
  { field: 'insuranceImage', label: '保险证明' },
]

// 所有已上传的资质图片列表（用于预览时左右切换）
const certPreviewList = computed(() =>
  certFields.map(c => (form as any)[c.field]).filter(Boolean) as string[]
)

function certPreviewIndex(field: string): number {
  const url = (form as any)[field] as string
  return certPreviewList.value.indexOf(url)
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  id: null as number | null,
  // 基本信息
  name: '',
  companyName: '',
  companyAddress: '',
  phone: '',
  intro: '',
  logo: '',
  // 账号（仅新增）
  username: '',
  password: '',
  // 联系人
  contact1Name: '',
  contact1Title: '',
  contact1Phone: '',
  contact2Name: '',
  contact2Title: '',
  contact2Phone: '',
  // 资质证件
  businessLicense: '',
  tourismLicense: '',
  idCardFront: '',
  idCardBack: '',
  insuranceImage: '',
})

const rules: FormRules = {
  name:     [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  phone:    [{ required: true, message: '请输入联系电话',   trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录账号',   trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码',   trigger: 'blur' }],
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getSupplierList({ page: pagination.page, pageSize: pagination.pageSize })
    tableData.value = res.list
    pagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally { loading.value = false }
}

function resetForm() {
  Object.assign(form, {
    id: null,
    name: '', companyName: '', companyAddress: '', phone: '', intro: '', logo: '',
    username: '', password: '',
    contact1Name: '', contact1Title: '', contact1Phone: '',
    contact2Name: '', contact2Title: '', contact2Phone: '',
    businessLicense: '', tourismLicense: '', idCardFront: '', idCardBack: '', insuranceImage: '',
  })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  drawerVisible.value = true
}

function handleEdit(row: SupplierVO) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name ?? '',
    companyName: row.companyName ?? '',
    companyAddress: row.companyAddress ?? '',
    phone: row.phone ?? '',
    intro: row.intro ?? '',
    logo: row.logo ?? '',
    contact1Name: row.contact1Name ?? '',
    contact1Title: row.contact1Title ?? '',
    contact1Phone: row.contact1Phone ?? '',
    contact2Name: row.contact2Name ?? '',
    contact2Title: row.contact2Title ?? '',
    contact2Phone: row.contact2Phone ?? '',
    businessLicense: row.businessLicense ?? '',
    tourismLicense: row.tourismLicense ?? '',
    idCardFront: row.idCardFront ?? '',
    idCardBack: row.idCardBack ?? '',
    insuranceImage: row.insuranceImage ?? '',
  })
  drawerVisible.value = true
}

function handleSingleUpload(response: any, field: string) {
  if (response.code === 0) {
    (form as any)[field] = response.data
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateSupplier({
        id: form.id!,
        name: form.name,
        companyName: form.companyName,
        companyAddress: form.companyAddress,
        phone: form.phone,
        intro: form.intro,
        logo: form.logo,
        contact1Name: form.contact1Name,
        contact1Title: form.contact1Title,
        contact1Phone: form.contact1Phone,
        contact2Name: form.contact2Name,
        contact2Title: form.contact2Title,
        contact2Phone: form.contact2Phone,
        businessLicense: form.businessLicense,
        tourismLicense: form.tourismLicense,
        idCardFront: form.idCardFront,
        idCardBack: form.idCardBack,
        insuranceImage: form.insuranceImage,
      })
    } else {
      await createSupplier({
        name: form.name,
        companyName: form.companyName,
        companyAddress: form.companyAddress,
        phone: form.phone,
        intro: form.intro,
        logo: form.logo,
        username: form.username,
        password: form.password,
        contact1Name: form.contact1Name,
        contact1Title: form.contact1Title,
        contact1Phone: form.contact1Phone,
        contact2Name: form.contact2Name,
        contact2Title: form.contact2Title,
        contact2Phone: form.contact2Phone,
        businessLicense: form.businessLicense,
        tourismLicense: form.tourismLicense,
        idCardFront: form.idCardFront,
        idCardBack: form.idCardBack,
        insuranceImage: form.insuranceImage,
      })
    }
    ElMessage.success(isEdit.value ? '保存成功' : '添加成功')
    drawerVisible.value = false
    fetchData()
  } catch (_e) { /* ignore */ }
  finally { submitLoading.value = false }
}

async function handleResetPassword(row: SupplierVO) {
  try {
    await ElMessageBox.confirm('确定重置该供应商的密码吗？', '提示', { type: 'warning' })
    const newPassword = await resetSupplierPassword(row.id)
    ElMessageBox.alert(`新密码：${newPassword}`, '密码已重置', { type: 'success' })
  } catch (_e) { /* ignore */ }
}

async function handleToggleStatus(row: SupplierVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}该供应商吗？`, '提示', { type: 'warning' })
    await updateSupplierStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    row.status = newStatus
  } catch (_e) { /* ignore */ }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  padding: 12px 0 16px;
  margin-bottom: 4px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.contact-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
  font-weight: 500;
}
.contact-label + .el-row { margin-bottom: 8px; }
.optional { font-weight: normal; color: #909399; font-size: 12px; }

/* Logo 上传 */
.logo-uploader { display: flex; flex-direction: column; align-items: flex-start; gap: 6px; }
.logo-placeholder {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
}
.logo-placeholder:hover { border-color: #409eff; }
.logo-upload-trigger :deep(.el-upload) { display: block; }
.logo-preview {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: zoom-in;
  display: block;
}

/* 资质证件上传 */
.cert-uploader { width: 100%; }
.cert-uploader :deep(.el-upload) { width: 100%; display: block; }
.cert-placeholder {
  width: 100%;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.cert-placeholder:hover { border-color: #409eff; color: #409eff; }
.cert-preview {
  width: 100%;
  height: 120px;
  border-radius: 6px;
  display: block;
  cursor: zoom-in;
}
.reupload-btn { margin-top: 4px; padding-left: 0; }

.uploader-icon { font-size: 24px; color: #8c939d; }
</style>
