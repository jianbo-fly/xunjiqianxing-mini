<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>定制需求管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="用户手机号" clearable />
        </el-form-item>
        <el-form-item label="目的地">
          <el-input v-model="searchForm.destination" placeholder="目的地" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option v-for="(item, key) in statusMap" :key="key" :label="item.text" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="出行时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="用户手机" width="130" />
        <el-table-column prop="destination" label="目的地" min-width="150" show-overflow-tooltip />
        <el-table-column prop="startDate" label="出行时间" width="110" />
        <el-table-column prop="days" label="天数" width="70">
          <template #default="{ row }">
            {{ row.days }}天
          </template>
        </el-table-column>
        <el-table-column label="人数" width="100">
          <template #default="{ row }">
            {{ row.adultCount }}成人
            <span v-if="row.childCount > 0">+{{ row.childCount }}儿童</span>
          </template>
        </el-table-column>
        <el-table-column prop="budget" label="预算" width="100" />
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              link
              @click="handleFollow(row)"
            >
              开始跟进
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              link
              @click="handleComplete(row)"
            >
              完成
            </el-button>
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
import { getCustomDemandList, updateCustomDemandStatus } from '@/api/custom'
import type { CustomDemandVO } from '@/api/custom'

const router = useRouter()
const loading = ref(false)
const tableData = ref<CustomDemandVO[]>([])

const searchForm = reactive({
  phone: '',
  destination: '',
  status: undefined as number | undefined,
  dateRange: null as [string, string] | null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待处理', type: 'warning' },
  1: { text: '跟进中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已关闭', type: 'info' },
}

function getStatusType(status: number) {
  return statusMap[status]?.type || 'info'
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getCustomDemandList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      phone: searchForm.phone || undefined,
      destination: searchForm.destination || undefined,
      status: searchForm.status,
      startDateBegin: searchForm.dateRange?.[0],
      startDateEnd: searchForm.dateRange?.[1],
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
  searchForm.phone = ''
  searchForm.destination = ''
  searchForm.status = undefined
  searchForm.dateRange = null
  handleSearch()
}

function handleDetail(row: CustomDemandVO) {
  router.push(`/custom/detail/${row.id}`)
}

async function handleFollow(row: CustomDemandVO) {
  try {
    await ElMessageBox.confirm('确定开始跟进该需求吗？', '提示', { type: 'warning' })
    await updateCustomDemandStatus(row.id, 1)
    ElMessage.success('已开始跟进')
    fetchData()
  } catch (_e) { /* ignore */ }
}

async function handleComplete(row: CustomDemandVO) {
  try {
    await ElMessageBox.confirm('确定将该需求标记为已完成吗？', '提示', { type: 'warning' })
    await updateCustomDemandStatus(row.id, 2)
    ElMessage.success('已完成')
    fetchData()
  } catch (_e) { /* ignore */ }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
