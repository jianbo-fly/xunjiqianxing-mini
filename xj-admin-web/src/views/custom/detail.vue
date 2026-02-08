<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>定制需求详情</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <template v-if="detail">
        <!-- 基本信息 -->
        <el-descriptions title="需求信息" :column="3" border>
          <el-descriptions-item label="需求ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="用户手机">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detail.status)" size="small">{{ detail.statusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目的地">{{ detail.destination }}</el-descriptions-item>
          <el-descriptions-item label="出行时间">{{ detail.startDate }}</el-descriptions-item>
          <el-descriptions-item label="行程天数">{{ detail.days }}天</el-descriptions-item>
          <el-descriptions-item label="成人人数">{{ detail.adultCount }}人</el-descriptions-item>
          <el-descriptions-item label="儿童人数">{{ detail.childCount }}人</el-descriptions-item>
          <el-descriptions-item label="预算">{{ detail.budget }}</el-descriptions-item>
          <el-descriptions-item label="其他需求" :span="3">
            {{ detail.requirements || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
            v-if="detail.status === 0"
            type="warning"
            @click="handleUpdateStatus(1)"
          >
            开始跟进
          </el-button>
          <el-button
            v-if="detail.status === 1"
            type="success"
            @click="handleUpdateStatus(2)"
          >
            标记完成
          </el-button>
          <el-button
            v-if="detail.status === 0 || detail.status === 1"
            type="info"
            @click="handleClose"
          >
            关闭需求
          </el-button>
          <el-button
            v-if="detail.status === 1"
            type="primary"
            @click="showFollowDialog = true"
          >
            添加跟进记录
          </el-button>
        </div>

        <!-- 跟进记录 -->
        <el-card shadow="never" class="follow-card">
          <template #header>
            <span>跟进记录</span>
          </template>
          <el-timeline v-if="detail.followRecords?.length">
            <el-timeline-item
              v-for="record in detail.followRecords"
              :key="record.id"
              :timestamp="record.createdAt"
              placement="top"
            >
              <el-card shadow="never">
                <p>{{ record.content }}</p>
                <p class="record-operator">操作人：{{ record.operatorName }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无跟进记录" />
        </el-card>
      </template>
    </el-card>

    <!-- 添加跟进记录对话框 -->
    <el-dialog v-model="showFollowDialog" title="添加跟进记录" width="500px">
      <el-form>
        <el-form-item label="跟进内容">
          <el-input
            v-model="followContent"
            type="textarea"
            :rows="4"
            placeholder="请输入跟进内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFollowDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAddFollow">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCustomDemandDetail, updateCustomDemandStatus, addFollowRecord } from '@/api/custom'
import type { CustomDemandVO } from '@/api/custom'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const detail = ref<CustomDemandVO | null>(null)
const showFollowDialog = ref(false)
const followContent = ref('')

const statusMap: Record<number, { type: string }> = {
  0: { type: 'warning' },
  1: { type: 'primary' },
  2: { type: 'success' },
  3: { type: 'info' },
}

function getStatusType(status: number) {
  return statusMap[status]?.type || 'info'
}

onMounted(() => fetchDetail())

async function fetchDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    detail.value = await getCustomDemandDetail(id)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

function handleBack() {
  router.back()
}

async function handleUpdateStatus(status: number) {
  const statusText = status === 1 ? '开始跟进' : '标记完成'
  try {
    await ElMessageBox.confirm(`确定${statusText}吗？`, '提示', { type: 'warning' })
    await updateCustomDemandStatus(detail.value!.id, status)
    ElMessage.success('操作成功')
    fetchDetail()
  } catch (_e) { /* ignore */ }
}

async function handleClose() {
  try {
    const { value } = await ElMessageBox.prompt('请输入关闭原因', '关闭需求', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因',
    })
    await updateCustomDemandStatus(detail.value!.id, 3, value)
    ElMessage.success('已关闭')
    fetchDetail()
  } catch (_e) { /* ignore */ }
}

async function handleAddFollow() {
  if (!followContent.value.trim()) {
    ElMessage.warning('请输入跟进内容')
    return
  }

  submitLoading.value = true
  try {
    await addFollowRecord(detail.value!.id, followContent.value)
    ElMessage.success('添加成功')
    showFollowDialog.value = false
    followContent.value = ''
    fetchDetail()
  } catch (_e) { /* ignore */ }
  finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  margin: 20px 0;
}

.follow-card {
  margin-top: 20px;
}

.record-operator {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
