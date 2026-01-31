<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <!-- 订单状态 -->
      <div class="order-status">
        <el-steps :active="getStepActive(order.status ?? 0)" finish-status="success">
          <el-step title="提交订单" :description="order.createdAt" />
          <el-step title="支付成功" :description="order.payTime || ''" />
          <el-step title="订单确认" :description="order.confirmTime || ''" />
          <el-step title="开始出行" :description="order.startDate || ''" />
          <el-step title="行程结束" :description="order.completeTime || ''" />
        </el-steps>
      </div>

      <el-divider />

      <!-- 基本信息 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(order.status ?? 0)">{{ order.statusDesc }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ order.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管理员备注">{{ order.adminRemark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 线路信息 -->
      <el-descriptions title="线路信息" :column="2" border class="mt-20">
        <el-descriptions-item label="线路名称">{{ order.productName }}</el-descriptions-item>
        <el-descriptions-item label="套餐名称">{{ order.skuName }}</el-descriptions-item>
        <el-descriptions-item label="出发日期">{{ order.startDate }}</el-descriptions-item>
        <el-descriptions-item label="出行天数">{{ order.days }}天</el-descriptions-item>
        <el-descriptions-item label="成人数">{{ order.adultCount }}</el-descriptions-item>
        <el-descriptions-item label="儿童数">{{ order.childCount }}</el-descriptions-item>
      </el-descriptions>

      <!-- 出行人信息 -->
      <div class="section-title">出行人信息</div>
      <el-table :data="order.travelers || []" border>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="idCard" label="身份证号" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="travelerTypeDesc" label="类型" width="100" />
      </el-table>

      <!-- 费用信息 -->
      <el-descriptions title="费用信息" :column="2" border class="mt-20">
        <el-descriptions-item label="商品金额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">-¥{{ order.discountAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠券">-¥{{ order.couponAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span class="price">¥{{ order.payAmount }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 联系信息 -->
      <el-descriptions title="联系信息" :column="2" border class="mt-20">
        <el-descriptions-item label="联系人">{{ order.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="用户备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 退款信息 -->
      <template v-if="order.refund">
        <el-descriptions title="退款信息" :column="2" border class="mt-20">
          <el-descriptions-item label="退款单号">{{ order.refund.refundNo }}</el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="order.refund.status === 0 ? 'warning' : order.refund.status === 1 ? 'success' : 'danger'">
              {{ order.refund.status === 0 ? '待审核' : order.refund.status === 1 ? '已同意' : '已拒绝' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ order.refund.refundAmount }}</el-descriptions-item>
          <el-descriptions-item label="实退金额">¥{{ order.refund.actualAmount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ order.refund.reason }}</el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- 操作按钮 -->
      <div class="action-bar" v-if="order.status === 1">
        <el-button type="primary" @click="handleConfirm">确认订单</el-button>
        <el-button type="danger" @click="handleReject">拒绝订单</el-button>
      </div>

      <!-- 备注 -->
      <div class="action-bar">
        <el-input v-model="remarkText" placeholder="添加管理员备注" style="width: 400px; margin-right: 10px" />
        <el-button type="primary" plain @click="handleAddRemark">添加备注</el-button>
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝订单" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, confirmOrder, addOrderRemark } from '@/api/order'
import type { OrderDetailVO } from '@/types'

const route = useRoute()
const loading = ref(false)
const remarkText = ref('')
const rejectDialogVisible = ref(false)
const rejectReason = ref('')

const order = reactive<Partial<OrderDetailVO>>({})

const statusMap: Record<number, string> = {
  0: 'info', 1: 'warning', 2: 'primary', 3: '', 4: 'success',
  5: 'info', 6: 'danger', 7: 'info', 8: 'info',
}

function getStatusType(status: number) {
  return statusMap[status] || 'info'
}

function getStepActive(status: number) {
  const map: Record<number, number> = {
    0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 0, 6: 2, 7: 2, 8: 0,
  }
  return map[status || 0] || 0
}

onMounted(async () => {
  const orderNo = route.params.orderNo as string
  loading.value = true
  try {
    const data = await getOrderDetail(orderNo)
    Object.assign(order, data)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
})

async function handleConfirm() {
  try {
    await confirmOrder({ orderNo: order.orderNo!, action: 'confirm' })
    ElMessage.success('确认成功')
    order.status = 2
    order.statusDesc = '已确认'
  } catch (_e) { /* ignore */ }
}

function handleReject() {
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await confirmOrder({
      orderNo: order.orderNo!,
      action: 'reject',
      rejectReason: rejectReason.value,
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    order.status = 5
    order.statusDesc = '已取消'
  } catch (_e) { /* ignore */ }
}

async function handleAddRemark() {
  if (!remarkText.value.trim()) {
    ElMessage.warning('请输入备注内容')
    return
  }
  try {
    await addOrderRemark({ orderNo: order.orderNo!, remark: remarkText.value })
    ElMessage.success('备注添加成功')
    order.adminRemark = remarkText.value
    remarkText.value = ''
  } catch (_e) { /* ignore */ }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-status {
  padding: 20px 0;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 0 15px;
  color: #303133;
}

.mt-20 {
  margin-top: 20px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.action-bar {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
