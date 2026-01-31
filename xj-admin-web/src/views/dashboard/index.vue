<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">今日订单</p>
              <p class="stat-value">{{ stats.todayOrderCount }}</p>
            </div>
            <div class="stat-icon" style="background-color: #409eff">
              <el-icon><Document /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <span>本月订单：{{ stats.monthOrderCount }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">今日销售额</p>
              <p class="stat-value">¥{{ formatAmount(stats.todaySalesAmount) }}</p>
            </div>
            <div class="stat-icon" style="background-color: #67c23a">
              <el-icon><Money /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <span>本月销售额：¥{{ formatAmount(stats.monthSalesAmount) }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">待确认订单</p>
              <p class="stat-value">{{ stats.pendingConfirmCount }}</p>
            </div>
            <div class="stat-icon" style="background-color: #e6a23c">
              <el-icon><Bell /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <span>待支付：{{ stats.pendingPayCount }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">退款处理中</p>
              <p class="stat-value">{{ stats.refundingCount }}</p>
            </div>
            <div class="stat-icon" style="background-color: #f56c6c">
              <el-icon><RefreshLeft /></el-icon>
            </div>
          </div>
          <div class="stat-footer">
            <span>已完成：{{ stats.completedCount }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 & 待办事项 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/route/create')">
              <el-icon><Plus /></el-icon>
              新建线路
            </el-button>
            <el-button @click="$router.push('/order/list')">
              <el-icon><Document /></el-icon>
              订单管理
            </el-button>
            <el-button @click="$router.push('/route/audit')">
              <el-icon><Check /></el-icon>
              线路审核
            </el-button>
            <el-button @click="$router.push('/order/refund')">
              <el-icon><RefreshLeft /></el-icon>
              退款处理
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>待处理事项</span>
          </template>
          <el-table :data="todoList" style="width: 100%" max-height="200">
            <el-table-column prop="type" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.tagType" size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" />
            <el-table-column prop="action" label="操作" width="80">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="$router.push(row.link)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Document,
  Money,
  Bell,
  Plus,
  Check,
  RefreshLeft,
} from '@element-plus/icons-vue'
import { getOrderStats } from '@/api/order'
import { getRefundPendingCount } from '@/api/refund'
import type { OrderStatsVO } from '@/types'

const stats = reactive<OrderStatsVO>({
  pendingPayCount: 0,
  pendingConfirmCount: 0,
  pendingTravelCount: 0,
  travelingCount: 0,
  completedCount: 0,
  refundingCount: 0,
  todayOrderCount: 0,
  todaySalesAmount: 0,
  monthOrderCount: 0,
  monthSalesAmount: 0,
})

const pendingRefundCount = ref(0)

// 待办事项
const todoList = computed(() => {
  const items = []
  if (stats.pendingConfirmCount > 0) {
    items.push({
      type: '订单确认',
      tagType: 'warning',
      content: `${stats.pendingConfirmCount} 个订单待确认`,
      link: '/order/list?status=1',
    })
  }
  if (pendingRefundCount.value > 0) {
    items.push({
      type: '退款审核',
      tagType: 'danger',
      content: `${pendingRefundCount.value} 个退款待处理`,
      link: '/order/refund?status=0',
    })
  }
  if (stats.pendingPayCount > 0) {
    items.push({
      type: '待支付',
      tagType: 'info',
      content: `${stats.pendingPayCount} 个订单待支付`,
      link: '/order/list?status=0',
    })
  }
  if (stats.travelingCount > 0) {
    items.push({
      type: '出行中',
      tagType: '',
      content: `${stats.travelingCount} 个订单出行中`,
      link: '/order/list?status=3',
    })
  }
  if (items.length === 0) {
    items.push({
      type: '暂无',
      tagType: 'success',
      content: '所有事项已处理完毕',
      link: '/dashboard',
    })
  }
  return items
})

function formatAmount(amount: number) {
  return (amount || 0).toLocaleString()
}

onMounted(async () => {
  try {
    const [statsData, refundCount] = await Promise.all([
      getOrderStats(),
      getRefundPendingCount(),
    ])
    Object.assign(stats, statsData)
    pendingRefundCount.value = refundCount
  } catch (_e) {
    // 错误已在拦截器中处理
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 140px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin: 0 0 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #909399;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.bottom-row {
  margin-bottom: 20px;
}
</style>
