<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>领队详情</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <template v-if="detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="领队ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detail.idCard }}</el-descriptions-item>
          <el-descriptions-item label="从业年限">{{ detail.experience }}年</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detail.status)" size="small">{{ detail.statusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="带队次数">{{ detail.leadCount }}次</el-descriptions-item>
          <el-descriptions-item label="累计佣金">
            <span class="price">¥{{ detail.totalCommission }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="简介" :span="3">
            {{ detail.intro || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 证件照片 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span>证件照片</span>
          </template>
          <div class="image-list">
            <div class="image-item">
              <p>头像</p>
              <el-image :src="detail.avatar" :preview-src-list="[detail.avatar]" fit="cover" />
            </div>
            <div class="image-item">
              <p>身份证正面</p>
              <el-image :src="detail.idCardFront" :preview-src-list="[detail.idCardFront]" fit="cover" />
            </div>
            <div class="image-item">
              <p>身份证背面</p>
              <el-image :src="detail.idCardBack" :preview-src-list="[detail.idCardBack]" fit="cover" />
            </div>
            <div class="image-item" v-if="detail.certificate">
              <p>资质证书</p>
              <el-image :src="detail.certificate" :preview-src-list="[detail.certificate]" fit="cover" />
            </div>
          </div>
        </el-card>

        <!-- 带队记录 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span>带队记录</span>
          </template>
          <el-table :data="leadRecords" v-loading="leadLoading" stripe>
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="routeName" label="线路名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="startDate" label="出发日期" width="110" />
            <el-table-column prop="peopleCount" label="人数" width="80" />
            <el-table-column prop="commission" label="佣金" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ row.commission }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="statusDesc" label="状态" width="100" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="leadPagination.page"
              :page-size="leadPagination.pageSize"
              :total="leadPagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchLeadRecords"
            />
          </div>
        </el-card>

        <!-- 佣金记录 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <span>佣金记录</span>
          </template>
          <el-table :data="commissionRecords" v-loading="commissionLoading" stripe>
            <el-table-column prop="typeDesc" label="类型" width="100" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                <span :class="row.amount > 0 ? 'price-success' : 'price'">
                  {{ row.amount > 0 ? '+' : '' }}¥{{ row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="关联订单" width="180" />
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createdAt" label="时间" width="170" />
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="commissionPagination.page"
              :page-size="commissionPagination.pageSize"
              :total="commissionPagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchCommissionRecords"
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
import { getLeaderDetail, getLeadRecords, getCommissionRecords } from '@/api/leader'
import type { LeaderVO, LeadRecord, CommissionRecord } from '@/api/leader'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const leadLoading = ref(false)
const commissionLoading = ref(false)
const detail = ref<LeaderVO | null>(null)
const leadRecords = ref<LeadRecord[]>([])
const commissionRecords = ref<CommissionRecord[]>([])

const leadPagination = reactive({
  page: 1,
  pageSize: 5,
  total: 0,
})

const commissionPagination = reactive({
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
  fetchLeadRecords()
  fetchCommissionRecords()
})

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    detail.value = await getLeaderDetail(id)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function fetchLeadRecords() {
  const id = Number(route.params.id)
  if (!id) return

  leadLoading.value = true
  try {
    const res = await getLeadRecords(id, {
      page: leadPagination.page,
      pageSize: leadPagination.pageSize,
    })
    leadRecords.value = res.list
    leadPagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    leadLoading.value = false
  }
}

async function fetchCommissionRecords() {
  const id = Number(route.params.id)
  if (!id) return

  commissionLoading.value = true
  try {
    const res = await getCommissionRecords(id, {
      page: commissionPagination.page,
      pageSize: commissionPagination.pageSize,
    })
    commissionRecords.value = res.list
    commissionPagination.total = res.total
  } catch (_e) { /* ignore */ }
  finally {
    commissionLoading.value = false
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

.image-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.image-item {
  text-align: center;
}

.image-item p {
  margin-bottom: 8px;
  color: #666;
}

.image-item .el-image {
  width: 150px;
  height: 100px;
  border-radius: 4px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.price-success {
  color: #67c23a;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
