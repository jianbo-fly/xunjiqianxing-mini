<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>线路审核</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="coverImage" label="封面" width="100">
          <template #default="{ row }">
            <el-image :src="row.coverImage" style="width: 60px; height: 40px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="线路名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="minPrice" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.minPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handlePreview(row)">预览</el-button>
            <el-button type="success" link @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
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

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectRemark" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRouteList, auditRoute } from '@/api/route'
import type { RouteListVO } from '@/types'

const router = useRouter()
const loading = ref(false)
const auditLoading = ref(false)
const rejectDialogVisible = ref(false)
const rejectRemark = ref('')
const currentRow = ref<RouteListVO | null>(null)
const tableData = ref<RouteListVO[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getRouteList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      auditStatus: 0, // 待审核
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

function handlePreview(row: RouteListVO) {
  router.push(`/route/edit/${row.id}`)
}

async function handleApprove(row: RouteListVO) {
  try {
    await ElMessageBox.confirm('确定通过该线路审核吗？', '提示', { type: 'warning' })
    await auditRoute({ id: row.id, auditStatus: 1 })
    ElMessage.success('审核通过')
    fetchData()
  } catch (_e) { /* ignore */ }
}

function handleReject(row: RouteListVO) {
  currentRow.value = row
  rejectRemark.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  auditLoading.value = true
  try {
    await auditRoute({
      id: currentRow.value!.id,
      auditStatus: 2,
      auditRemark: rejectRemark.value,
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchData()
  } catch (_e) { /* ignore */ }
  finally {
    auditLoading.value = false
  }
}
</script>

<style scoped>
.price { color: #f56c6c; font-weight: bold; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
