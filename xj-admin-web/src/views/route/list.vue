<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>线路列表</span>
          <el-button type="primary" @click="$router.push('/route/create')">
            <el-icon><Plus /></el-icon>
            新建线路
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="线路名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择" clearable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
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
        <el-table-column prop="coverImage" label="封面" width="100">
          <template #default="{ row }">
            <el-image :src="row.coverImage" style="width: 60px; height: 40px" fit="cover" :preview-src-list="[row.coverImage]" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="线路名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="destination" label="目的地" width="120" />
        <el-table-column prop="minPrice" label="最低价" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.minPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="status" label="上架" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核" width="80">
          <template #default="{ row }">
            <el-tag :type="auditStatusType(row.auditStatus)" size="small">
              {{ auditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handlePackage(row)">套餐</el-button>
            <el-button type="primary" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRouteList, updateRouteStatus, deleteRoute } from '@/api/route'
import { getCategoryList } from '@/api/category'
import type { RouteListVO, CategoryVO } from '@/types'

const router = useRouter()
const loading = ref(false)
const tableData = ref<RouteListVO[]>([])
const categories = ref<CategoryVO[]>([])

const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
  auditStatus: undefined as number | undefined,
  category: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function auditStatusText(status: number) {
  const map: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

function auditStatusType(status: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  fetchData()
  loadCategories()
})

async function loadCategories() {
  try {
    categories.value = await getCategoryList()
  } catch (_e) { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRouteList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status,
      auditStatus: searchForm.auditStatus,
      category: searchForm.category || undefined,
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = undefined
  searchForm.auditStatus = undefined
  searchForm.category = ''
  handleSearch()
}

function handleEdit(row: RouteListVO) {
  router.push(`/route/edit/${row.id}`)
}

function handlePackage(row: RouteListVO) {
  router.push(`/route/package/${row.id}`)
}

async function handleToggleStatus(row: RouteListVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该线路吗？`, '提示', { type: 'warning' })
    await updateRouteStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    row.status = newStatus
  } catch (_e) { /* ignore */ }
}

async function handleDelete(row: RouteListVO) {
  try {
    await ElMessageBox.confirm('确定要删除该线路吗？此操作不可恢复', '提示', { type: 'warning' })
    await deleteRoute(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (_e) { /* ignore */ }
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
</style>
