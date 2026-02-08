<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>领队管理</span>
          <el-button type="primary" @click="showConfigDialog = true">
            <el-icon><Setting /></el-icon>
            领队配置
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="领队姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option v-for="(item, key) in statusMap" :key="key" :label="item.text" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="experience" label="从业年限" width="100">
          <template #default="{ row }">
            {{ row.experience }}年
          </template>
        </el-table-column>
        <el-table-column prop="leadCount" label="带队次数" width="100" />
        <el-table-column prop="totalCommission" label="累计佣金" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalCommission }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <template v-if="row.status === 0">
              <el-button type="success" link @click="handleAudit(row, 'approve')">通过</el-button>
              <el-button type="danger" link @click="handleAudit(row, 'reject')">驳回</el-button>
            </template>
            <template v-else-if="row.status === 1">
              <el-button type="danger" link @click="handleToggleStatus(row)">禁用</el-button>
            </template>
            <template v-else-if="row.status === 3">
              <el-button type="success" link @click="handleToggleStatus(row)">启用</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 领队配置对话框 -->
    <el-dialog v-model="showConfigDialog" title="领队配置" width="400px">
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="领队价格">
          <el-input-number v-model="configForm.price" :min="0" :precision="2" />
          <span class="unit">元</span>
        </el-form-item>
        <el-form-item label="佣金比例">
          <el-input-number v-model="configForm.commissionRate" :min="0" :max="100" :precision="1" />
          <span class="unit">%</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" :loading="configLoading" @click="handleSaveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import {
  getLeaderList,
  auditLeader,
  updateLeaderStatus,
  getLeaderConfig,
  updateLeaderConfig,
} from '@/api/leader'
import type { LeaderVO } from '@/api/leader'

const router = useRouter()
const loading = ref(false)
const configLoading = ref(false)
const showConfigDialog = ref(false)
const tableData = ref<LeaderVO[]>([])

const searchForm = reactive({
  name: '',
  phone: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const configForm = reactive({
  price: 2000,
  commissionRate: 10,
})

const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
  3: { text: '已禁用', type: 'info' },
}

function getStatusType(status: number) {
  return statusMap[status]?.type || 'info'
}

onMounted(() => {
  fetchData()
  fetchConfig()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getLeaderList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      phone: searchForm.phone || undefined,
      status: searchForm.status,
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function fetchConfig() {
  try {
    const res = await getLeaderConfig()
    configForm.price = res.price
    configForm.commissionRate = res.commissionRate
  } catch (_e) { /* ignore */ }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.phone = ''
  searchForm.status = undefined
  handleSearch()
}

function handleDetail(row: LeaderVO) {
  router.push(`/leader/detail/${row.id}`)
}

async function handleAudit(row: LeaderVO, action: 'approve' | 'reject') {
  if (action === 'approve') {
    try {
      await ElMessageBox.confirm('确定通过该领队的申请吗？', '提示', { type: 'warning' })
      await auditLeader(row.id, 'approve')
      ElMessage.success('审核通过')
      fetchData()
    } catch (_e) { /* ignore */ }
  } else {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入原因',
      })
      await auditLeader(row.id, 'reject', value)
      ElMessage.success('已驳回')
      fetchData()
    } catch (_e) { /* ignore */ }
  }
}

async function handleToggleStatus(row: LeaderVO) {
  const newStatus = row.status === 1 ? 3 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}该领队吗？`, '提示', { type: 'warning' })
    await updateLeaderStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (_e) { /* ignore */ }
}

async function handleSaveConfig() {
  configLoading.value = true
  try {
    await updateLeaderConfig(configForm)
    ElMessage.success('保存成功')
    showConfigDialog.value = false
  } catch (_e) { /* ignore */ }
  finally {
    configLoading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.unit {
  margin-left: 8px;
}
</style>
