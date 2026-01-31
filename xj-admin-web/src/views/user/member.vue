<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>会员管理</span>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="会员状态">
          <el-select v-model="searchForm.memberStatus" placeholder="请选择" clearable>
            <el-option label="有效" :value="1" />
            <el-option label="已过期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="memberStatusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.memberStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.memberStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberExpireAt" label="到期时间" width="170" />
        <el-table-column prop="remainDays" label="剩余天数" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.remainDays <= 7 }">{{ row.remainDays }}天</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalPaidAmount" label="累计付费" width="110">
          <template #default="{ row }">¥{{ row.totalPaidAmount }}</template>
        </el-table-column>
        <el-table-column prop="firstOpenAt" label="首次开通" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleExtend(row)">续期</el-button>
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

    <!-- 续期对话框 -->
    <el-dialog v-model="extendDialogVisible" title="会员续期" width="400px">
      <el-form :model="extendForm" label-width="80px">
        <el-form-item label="用户">{{ extendForm.nickname }}</el-form-item>
        <el-form-item label="续期天数">
          <el-input-number v-model="extendForm.days" :min="1" :max="365" />
          <span style="margin-left: 10px">天</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="extendForm.reason" type="textarea" :rows="2" placeholder="请输入续期原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="extendLoading" @click="confirmExtend">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberList, extendMember } from '@/api/member'
import type { MemberListVO } from '@/types'

const loading = ref(false)
const extendDialogVisible = ref(false)
const extendLoading = ref(false)
const tableData = ref<MemberListVO[]>([])

const searchForm = reactive({
  phone: '',
  memberStatus: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const extendForm = reactive({
  userId: '' as string | number,
  nickname: '',
  days: 30,
  reason: '',
})

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const res = await getMemberList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      phone: searchForm.phone || undefined,
      memberStatus: searchForm.memberStatus,
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
  searchForm.memberStatus = undefined
  handleSearch()
}

function handleExtend(row: MemberListVO) {
  extendForm.userId = row.userId
  extendForm.nickname = row.nickname
  extendForm.days = 30
  extendForm.reason = ''
  extendDialogVisible.value = true
}

async function confirmExtend() {
  if (!extendForm.reason.trim()) {
    ElMessage.warning('请输入续期原因')
    return
  }
  extendLoading.value = true
  try {
    await extendMember({
      userId: extendForm.userId,
      days: extendForm.days,
      reason: extendForm.reason,
    })
    ElMessage.success('续期成功')
    extendDialogVisible.value = false
    fetchData()
  } catch (_e) { /* ignore */ }
  finally {
    extendLoading.value = false
  }
}
</script>

<style scoped>
.search-form { margin-bottom: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.text-danger { color: #f56c6c; font-weight: bold; }
</style>
