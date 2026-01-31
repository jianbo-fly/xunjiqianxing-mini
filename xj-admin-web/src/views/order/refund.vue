<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>退款管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" :value="0" />
            <el-option label="已同意" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="refundNo" label="退款单号" width="180" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productName" label="线路名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userNickname" label="申请人" width="100" />
        <el-table-column prop="refundAmount" label="退款金额" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ row.refundAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="statusDesc" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link @click="handleApprove(row)">同意</el-button>
              <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button v-else type="primary" link @click="handleViewOrder(row)">查看订单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝退款" width="400px">
      <el-input
        v-model="auditForm.auditRemark"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRefundList, auditRefund } from '@/api/refund'
import type { RefundListVO } from '@/types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const auditLoading = ref(false)
const rejectDialogVisible = ref(false)
const tableData = ref<RefundListVO[]>([])

const searchForm = reactive({
  orderNo: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const auditForm = reactive({
  id: '' as string | number,
  auditRemark: '',
})

function getStatusType(status: number) {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

onMounted(() => {
  if (route.query.status) {
    searchForm.status = Number(route.query.status)
  }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getRefundList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status,
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
  searchForm.orderNo = ''
  searchForm.status = undefined
  handleSearch()
}

async function handleApprove(row: RefundListVO) {
  try {
    await ElMessageBox.confirm('确定同意该退款申请吗？将退还用户付款金额', '提示', { type: 'warning' })
    await auditRefund({ id: row.id, status: 1 })
    ElMessage.success('已同意退款')
    fetchData()
  } catch (_e) { /* ignore */ }
}

function handleReject(row: RefundListVO) {
  auditForm.id = row.id
  auditForm.auditRemark = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!auditForm.auditRemark.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  auditLoading.value = true
  try {
    await auditRefund({
      id: auditForm.id,
      status: 2,
      auditRemark: auditForm.auditRemark,
    })
    ElMessage.success('已拒绝退款')
    rejectDialogVisible.value = false
    fetchData()
  } catch (_e) { /* ignore */ }
  finally {
    auditLoading.value = false
  }
}

function handleViewOrder(row: RefundListVO) {
  router.push(`/order/detail/${row.orderNo}`)
}
</script>

<style scoped>
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
