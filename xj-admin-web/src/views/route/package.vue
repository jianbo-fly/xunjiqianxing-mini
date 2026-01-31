<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
            <span class="route-name">{{ routeInfo.name }} - 套餐管理</span>
          </div>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加套餐
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="180" />
        <el-table-column prop="name" label="套餐名称" min-width="200" />
        <el-table-column prop="tags" label="标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" size="small" style="margin-right: 4px">
              {{ tag }}
            </el-tag>
            <span v-if="!row.tags?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column label="行程" width="100">
          <template #default="{ row }">
            {{ row.days }}天{{ row.nights }}晚
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="成人价" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="childPrice" label="儿童价" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.childPrice || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleCalendar(row)">价格日历</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 套餐编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑套餐' : '添加套餐'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="行程天数" prop="days">
          <el-input-number v-model="form.days" :min="1" :max="30" />
          <span style="margin: 0 10px">天</span>
          <el-input-number v-model="form.nights" :min="0" :max="30" />
          <span style="margin-left: 10px">晚</span>
        </el-form-item>
        <el-form-item label="成人价" prop="basePrice">
          <el-input-number v-model="form.basePrice" :min="0" :precision="2" style="width: 180px" />
          <span style="margin-left: 10px">元/人</span>
        </el-form-item>
        <el-form-item label="儿童价">
          <el-input-number v-model="form.childPrice" :min="0" :precision="2" style="width: 180px" />
          <span style="margin-left: 10px">元/人</span>
        </el-form-item>
        <el-form-item label="标签">
          <div class="tag-input-wrap">
            <el-tag
              v-for="tag in form.tags"
              :key="tag"
              closable
              @close="removeTag(tag)"
              style="margin-right: 8px"
            >{{ tag }}</el-tag>
            <el-input
              v-if="tagInputVisible"
              ref="tagInputRef"
              v-model="tagInputValue"
              size="small"
              style="width: 100px"
              @keyup.enter="addTag"
              @blur="addTag"
            />
            <el-button v-else size="small" @click="showTagInput">+ 添加</el-button>
          </div>
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

    <!-- 价格日历对话框 -->
    <el-dialog v-model="calendarDialogVisible" title="价格日历" width="800px">
      <div class="calendar-toolbar">
        <el-date-picker
          v-model="calendarMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          @change="loadCalendar"
        />
        <el-button type="primary" @click="handleBatchSet">批量设置</el-button>
      </div>
      <el-table :data="calendarData" v-loading="calendarLoading" max-height="400">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="成人价" width="140">
          <template #default="{ row }">
            <el-input-number
              v-model="row.price"
              :min="0"
              :precision="2"
              size="small"
              style="width: 110px"
            />
          </template>
        </el-table-column>
        <el-table-column label="儿童价" width="140">
          <template #default="{ row }">
            <el-input-number
              v-model="row.childPrice"
              :min="0"
              :precision="2"
              size="small"
              style="width: 110px"
            />
          </template>
        </el-table-column>
        <el-table-column label="库存" width="120">
          <template #default="{ row }">
            <el-input-number
              v-model="row.stock"
              :min="0"
              size="small"
              style="width: 90px"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="calendarDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="calendarSaving" @click="handleSaveCalendar">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量设置对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量设置" width="400px">
      <el-form label-width="80px">
        <el-form-item label="成人价">
          <el-input-number v-model="batchForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="儿童价">
          <el-input-number v-model="batchForm.childPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="batchForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="applyBatch">应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import {
  getRouteDetail,
  getRoutePackages,
  createPackage,
  updatePackage,
  deletePackage,
  getPriceCalendar,
  savePriceCalendar,
} from '@/api/route'
import type { PackageVO, PriceCalendarItem, ID } from '@/types'

const route = useRoute()
const productId = route.params.id as string

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const tableData = ref<PackageVO[]>([])
const routeInfo = ref({ name: '' })

// 套餐表单
const form = reactive({
  id: '' as ID,
  name: '',
  days: 1,
  nights: 0,
  basePrice: 0,
  childPrice: 0,
  tags: [] as string[],
  sortOrder: 0,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  days: [{ required: true, message: '请输入行程天数', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入成人价', trigger: 'blur' }],
}

// 标签输入
const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref()

// 价格日历
const calendarDialogVisible = ref(false)
const calendarLoading = ref(false)
const calendarSaving = ref(false)
const calendarMonth = ref('')
const calendarData = ref<PriceCalendarItem[]>([])
const currentPackage = ref<PackageVO | null>(null)

// 批量设置
const batchDialogVisible = ref(false)
const batchForm = reactive({
  price: 0,
  childPrice: 0,
  stock: 10,
})

onMounted(async () => {
  // 初始化日历月份为当前月
  const now = new Date()
  calendarMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

  await fetchRouteInfo()
  await fetchData()
})

async function fetchRouteInfo() {
  try {
    const detail = await getRouteDetail(productId)
    routeInfo.value = { name: detail.name }
  } catch (_e) { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    tableData.value = await getRoutePackages(productId)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = ''
  form.name = ''
  form.days = 1
  form.nights = 0
  form.basePrice = 0
  form.childPrice = 0
  form.tags = []
  form.sortOrder = 0
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: PackageVO) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    days: row.days || 1,
    nights: row.nights || 0,
    basePrice: row.basePrice || 0,
    childPrice: row.childPrice || 0,
    tags: row.tags || [],
    sortOrder: row.sortOrder || 0,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updatePackage({
        id: form.id,
        productId,
        name: form.name,
        tags: form.tags,
        basePrice: form.basePrice,
        childPrice: form.childPrice,
        sortOrder: form.sortOrder,
        days: form.days,
        nights: form.nights,
      })
    } else {
      await createPackage({
        productId,
        name: form.name,
        tags: form.tags,
        basePrice: form.basePrice,
        childPrice: form.childPrice,
        sortOrder: form.sortOrder,
        days: form.days,
        nights: form.nights,
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

async function handleStatusChange(row: PackageVO) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updatePackage({
      id: row.id,
      productId,
      name: row.name,
      basePrice: row.basePrice,
      days: row.days,
      nights: row.nights,
      status: newStatus,
    })
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch (_e) { /* ignore */ }
}

async function handleDelete(row: PackageVO) {
  try {
    await ElMessageBox.confirm('确定删除该套餐吗？', '提示', { type: 'warning' })
    await deletePackage(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (_e) { /* ignore */ }
}

// 标签操作
function removeTag(tag: string) {
  form.tags = form.tags.filter(t => t !== tag)
}

function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => tagInputRef.value?.input?.focus())
}

function addTag() {
  const val = tagInputValue.value.trim()
  if (val && !form.tags.includes(val)) {
    form.tags.push(val)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 价格日历
function handleCalendar(row: PackageVO) {
  currentPackage.value = row
  calendarDialogVisible.value = true
  loadCalendar()
}

async function loadCalendar() {
  if (!currentPackage.value || !calendarMonth.value) return

  calendarLoading.value = true
  try {
    // 获取当月的开始和结束日期
    const parts = calendarMonth.value.split('-').map(Number)
    const year = parts[0] || new Date().getFullYear()
    const month = parts[1] || 1
    const startDate = `${year}-${String(month).padStart(2, '0')}-01`
    const lastDay = new Date(year, month, 0).getDate()
    const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`

    const data = await getPriceCalendar(currentPackage.value.id, startDate, endDate)

    // 生成完整日期列表
    const result: PriceCalendarItem[] = []
    for (let d = 1; d <= lastDay; d++) {
      const date = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      const existing = data.find(item => item.date === date)
      result.push(existing || {
        date,
        price: currentPackage.value!.basePrice || 0,
        childPrice: currentPackage.value!.childPrice || 0,
        stock: 10,
        status: 1,
      })
    }
    calendarData.value = result
  } catch (_e) {
    // 如果获取失败，生成默认数据
    const parts = calendarMonth.value.split('-').map(Number)
    const year = parts[0] || new Date().getFullYear()
    const month = parts[1] || 1
    const lastDay = new Date(year, month, 0).getDate()
    const result: PriceCalendarItem[] = []
    for (let d = 1; d <= lastDay; d++) {
      const date = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      result.push({
        date,
        price: currentPackage.value!.basePrice || 0,
        childPrice: currentPackage.value!.childPrice || 0,
        stock: 10,
        status: 1,
      })
    }
    calendarData.value = result
  }
  finally {
    calendarLoading.value = false
  }
}

async function handleSaveCalendar() {
  if (!currentPackage.value) return

  calendarSaving.value = true
  try {
    await savePriceCalendar({
      skuId: currentPackage.value.id,
      prices: calendarData.value,
    })
    ElMessage.success('保存成功')
    calendarDialogVisible.value = false
  } catch (_e) { /* ignore */ }
  finally {
    calendarSaving.value = false
  }
}

function handleBatchSet() {
  batchForm.price = currentPackage.value?.basePrice || 0
  batchForm.childPrice = currentPackage.value?.childPrice || 0
  batchForm.stock = 10
  batchDialogVisible.value = true
}

function applyBatch() {
  calendarData.value.forEach(item => {
    item.price = batchForm.price
    item.childPrice = batchForm.childPrice
    item.stock = batchForm.stock
  })
  batchDialogVisible.value = false
  ElMessage.success('已应用批量设置')
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.route-name {
  font-size: 16px;
  font-weight: 500;
}
.price {
  color: #f56c6c;
  font-weight: bold;
}
.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}
.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
