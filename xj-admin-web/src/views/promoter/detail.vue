<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>推广员详情</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <template v-if="detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="推广员ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="头像">
            <el-avatar :src="detail.avatar" :size="40" />
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detail.status)" size="small">{{ detail.statusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 推广业绩 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span>推广业绩</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ detail.bindCount }}</div>
                <div class="stat-label">绑定用户</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ detail.totalOrders }}</div>
                <div class="stat-label">推广订单</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value price">¥{{ detail.totalAmount }}</div>
                <div class="stat-label">推广金额</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-value">{{ detail.availablePoints }} / {{ detail.totalPoints }}</div>
                <div class="stat-label">可用积分 / 总积分</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 扫码绑定记录 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span>扫码绑定记录</span>
          </template>
          <el-table :data="scanRecords" v-loading="scanLoading" stripe>
            <el-table-column label="用户头像" width="80">
              <template #default="{ row }">
                <el-avatar :src="row.userAvatar" :size="32" />
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="用户昵称" width="120" />
            <el-table-column prop="userPhone" label="手机号" width="130" />
            <el-table-column prop="bindTime" label="绑定时间" width="170" />
            <el-table-column prop="orderCount" label="下单次数" width="100" />
            <el-table-column prop="totalAmount" label="消费金额" width="120">
              <template #default="{ row }">
                <span class="price">¥{{ row.totalAmount }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="scanPagination.page"
              :page-size="scanPagination.pageSize"
              :total="scanPagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchScanRecords"
            />
          </div>
        </el-card>

        <!-- 积分记录 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span>积分记录</span>
          </template>
          <el-table :data="pointRecords" v-loading="pointLoading" stripe>
            <el-table-column prop="typeDesc" label="类型" width="120" />
            <el-table-column prop="points" label="积分" width="100">
              <template #default="{ row }">
                <span :class="row.points > 0 ? 'points-add' : 'points-sub'">
                  {{ row.points > 0 ? '+' : '' }}{{ row.points }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="关联订单" width="180" />
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="时间" width="170" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pointPagination.page"
              :page-size="pointPagination.pageSize"
              :total="pointPagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchPointRecords"
            />
          </div>
        </el-card>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPromoterDetail, getScanRecords, getPointRecords } from '@/api/promoter'
import type { PromoterVO, ScanRecord, PointRecord } from '@/api/promoter'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const scanLoading = ref(false)
const pointLoading = ref(false)
const detail = ref<PromoterVO | null>(null)
const scanRecords = ref<ScanRecord[]>([])
const pointRecords = ref<PointRecord[]>([])

const scanPagination = reactive({
  page: 1,
  pageSize: 5,
  total: 0,
})

const pointPagination = reactive({
  page: 1,
  pageSize: 5,
  total: 0,
})

const statusMap: Record<number, { type: string }> = {
  0: { type: 'warning' },
  1: { type: 'success' },
  2: { type: 'danger' },
  3: { type: 'info' },
}

function getStatusType(status: number) {
  return statusMap[status]?.type || 'info'
}

onMounted(() => {
  fetchDetail()
  fetchScanRecords()
  fetchPointRecords()
})

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    detail.value = await getPromoterDetail(id)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function fetchScanRecords() {
  const id = Number(route.params.id)
  if (!id) return

  scanLoading.value = true
  try {
    const res = await getScanRecords(id, {
      page: scanPagination.page,
      pageSize: scanPagination.pageSize,
    })
    scanRecords.value = res.list
    scanPagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    scanLoading.value = false
  }
}

async function fetchPointRecords() {
  const id = Number(route.params.id)
  if (!id) return

  pointLoading.value = true
  try {
    const res = await getPointRecords(id, {
      page: pointPagination.page,
      pageSize: pointPagination.pageSize,
    })
    pointRecords.value = res.list
    pointPagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    pointLoading.value = false
  }
}

function handleBack() {
  router.back()
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-card {
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.price {
  color: #f56c6c;
}

.points-add {
  color: #67c23a;
  font-weight: bold;
}

.points-sub {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
