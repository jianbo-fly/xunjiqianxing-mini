<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>订单列表</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contactPhone" placeholder="联系电话" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option v-for="(item, key) in statusMap" :key="key" :label="item.text" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
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
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="productName" label="线路名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="人数" width="80">
          <template #default="{ row }">
            {{ row.adultCount + row.childCount }}
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ row.payAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="出发日期" width="110" />
        <el-table-column prop="createdAt" label="下单时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              link
              @click="handleConfirm(row)"
            >
              确认
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, confirmOrder } from '@/api/order'
import type { OrderListVO } from '@/types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const tableData = ref<OrderListVO[]>([])

const searchForm = reactive({
  orderNo: '',
  contactPhone: '',
  status: undefined as number | undefined,
  dateRange: null as [string, string] | null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const statusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待支付', type: 'info' },
  1: { text: '待确认', type: 'warning' },
  2: { text: '已确认', type: 'primary' },
  3: { text: '出行中', type: '' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'info' },
  6: { text: '退款中', type: 'danger' },
  7: { text: '已退款', type: 'info' },
  8: { text: '已关闭', type: 'info' },
}

function getStatusType(status: number) {
  return statusMap[status]?.type || 'info'
}

onMounted(() => {
  // 从 URL query 中读取筛选条件
  if (route.query.status) {
    searchForm.status = Number(route.query.status)
  }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getOrderList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      contactPhone: searchForm.contactPhone || undefined,
      status: searchForm.status,
      createDateBegin: searchForm.dateRange?.[0],
      createDateEnd: searchForm.dateRange?.[1],
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
  searchForm.contactPhone = ''
  searchForm.status = undefined
  searchForm.dateRange = null
  handleSearch()
}

function handleDetail(row: OrderListVO) {
  router.push(`/order/detail/${row.orderNo}`)
}

async function handleConfirm(row: OrderListVO) {
  try {
    await ElMessageBox.confirm('确定确认该订单吗？', '提示', { type: 'warning' })
    await confirmOrder({ orderNo: row.orderNo, action: 'confirm' })
    ElMessage.success('确认成功')
    fetchData()
  } catch (_e) { /* ignore */ }
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
