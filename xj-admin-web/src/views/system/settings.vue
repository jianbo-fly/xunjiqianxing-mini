<template>
  <div class="page-container">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <span>系统设置</span>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicForm" label-width="140px" class="settings-form">
            <el-form-item label="客服电话">
              <el-input v-model="basicForm.customerServicePhone" placeholder="请输入客服电话" style="width: 300px" />
              <span class="form-tip">C端显示的客服联系电话</span>
            </el-form-item>
            <el-form-item label="企微客服链接">
              <el-input v-model="basicForm.wecomServiceUrl" placeholder="请输入企业微信客服链接" style="width: 400px" />
              <span class="form-tip">在线客服跳转地址</span>
            </el-form-item>
            <el-form-item label="支付超时时间">
              <el-input-number v-model="basicForm.paymentTimeout" :min="5" :max="120" />
              <span class="unit">分钟</span>
              <span class="form-tip">订单创建后多长时间未支付自动关闭</span>
            </el-form-item>
            <el-form-item label="儿童年龄上限">
              <el-input-number v-model="basicForm.childAgeLimit" :min="1" :max="18" />
              <span class="unit">周岁</span>
              <span class="form-tip">低于该年龄按儿童价计费</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="basicLoading" @click="handleSaveBasic">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 积分规则 -->
        <el-tab-pane label="积分规则" name="points">
          <el-form :model="pointsForm" label-width="140px" class="settings-form">
            <el-form-item label="下单积分比例">
              <el-input-number v-model="pointsForm.orderPointsRate" :min="0" :max="100" :precision="1" />
              <span class="unit">%</span>
              <span class="form-tip">用户下单获得的积分 = 订单金额 × 比例</span>
            </el-form-item>
            <el-form-item label="推广积分比例">
              <el-input-number v-model="pointsForm.promoterPointsRate" :min="0" :max="100" :precision="1" />
              <span class="unit">%</span>
              <span class="form-tip">推广员获得的积分 = 推广订单金额 × 比例</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pointsLoading" @click="handleSavePoints">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 退款规则 -->
        <el-tab-pane label="退款规则" name="refund">
          <el-form label-width="140px" class="settings-form">
            <div class="refund-rules">
              <div class="rule-header">
                <span>出发前天数</span>
                <span>退款比例</span>
                <span>操作</span>
              </div>
              <div class="rule-item" v-for="(rule, index) in refundRules" :key="index">
                <el-input-number v-model="rule.daysBeforeStart" :min="0" :max="365" />
                <span class="rule-text">天以上</span>
                <el-input-number v-model="rule.refundRate" :min="0" :max="100" />
                <span class="rule-text">%</span>
                <el-button type="danger" link @click="handleRemoveRule(index)">删除</el-button>
              </div>
              <el-button type="primary" link @click="handleAddRule">+ 添加规则</el-button>
            </div>
            <el-form-item class="rule-tip">
              <el-alert
                title="规则说明"
                type="info"
                :closable="false"
                description="按出发前天数从大到小匹配，例如：7天以上100%、3天以上70%、1天以上50%。未匹配到规则时不可退款。"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="refundLoading" @click="handleSaveRefund">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 价格配置 -->
        <el-tab-pane label="价格配置" name="price">
          <el-form :model="priceForm" label-width="140px" class="settings-form">
            <el-form-item label="会员价格">
              <el-input-number v-model="priceForm.memberPrice" :min="0" :precision="2" />
              <span class="unit">元/年</span>
              <span class="form-tip">用户开通会员的年费价格</span>
            </el-form-item>
            <el-form-item label="领队价格">
              <el-input-number v-model="priceForm.leaderPrice" :min="0" :precision="2" />
              <span class="unit">元</span>
              <span class="form-tip">申请成为领队的费用</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="priceLoading" @click="handleSavePrice">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getBasicSettings,
  updateBasicSettings,
  getPointsSettings,
  updatePointsSettings,
  getRefundSettings,
  updateRefundSettings,
  getPriceSettings,
  updatePriceSettings,
} from '@/api/settings'
import type { RefundRule } from '@/api/settings'

const loading = ref(false)
const basicLoading = ref(false)
const pointsLoading = ref(false)
const refundLoading = ref(false)
const priceLoading = ref(false)
const activeTab = ref('basic')

const basicForm = reactive({
  customerServicePhone: '',
  wecomServiceUrl: '',
  paymentTimeout: 30,
  childAgeLimit: 12,
})

const pointsForm = reactive({
  orderPointsRate: 1,
  promoterPointsRate: 5,
})

const refundRules = ref<RefundRule[]>([
  { daysBeforeStart: 7, refundRate: 100 },
  { daysBeforeStart: 3, refundRate: 70 },
  { daysBeforeStart: 1, refundRate: 50 },
])

const priceForm = reactive({
  memberPrice: 99,
  leaderPrice: 2000,
})

onMounted(() => {
  fetchBasicSettings()
})

watch(activeTab, (val) => {
  if (val === 'basic') fetchBasicSettings()
  else if (val === 'points') fetchPointsSettings()
  else if (val === 'refund') fetchRefundSettings()
  else if (val === 'price') fetchPriceSettings()
})

async function fetchBasicSettings() {
  loading.value = true
  try {
    const res = await getBasicSettings()
    Object.assign(basicForm, res)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function fetchPointsSettings() {
  loading.value = true
  try {
    const res = await getPointsSettings()
    Object.assign(pointsForm, res)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function fetchRefundSettings() {
  loading.value = true
  try {
    const res = await getRefundSettings()
    refundRules.value = res.rules || []
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function fetchPriceSettings() {
  loading.value = true
  try {
    const res = await getPriceSettings()
    Object.assign(priceForm, res)
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

async function handleSaveBasic() {
  basicLoading.value = true
  try {
    await updateBasicSettings(basicForm)
    ElMessage.success('保存成功')
  } catch (_e) { /* ignore */ }
  finally {
    basicLoading.value = false
  }
}

async function handleSavePoints() {
  pointsLoading.value = true
  try {
    await updatePointsSettings(pointsForm)
    ElMessage.success('保存成功')
  } catch (_e) { /* ignore */ }
  finally {
    pointsLoading.value = false
  }
}

function handleAddRule() {
  refundRules.value.push({ daysBeforeStart: 0, refundRate: 0 })
}

function handleRemoveRule(index: number) {
  refundRules.value.splice(index, 1)
}

async function handleSaveRefund() {
  // 验证规则
  const sortedRules = [...refundRules.value].sort((a, b) => b.daysBeforeStart - a.daysBeforeStart)
  refundRules.value = sortedRules

  refundLoading.value = true
  try {
    await updateRefundSettings({ rules: refundRules.value })
    ElMessage.success('保存成功')
  } catch (_e) { /* ignore */ }
  finally {
    refundLoading.value = false
  }
}

async function handleSavePrice() {
  priceLoading.value = true
  try {
    await updatePriceSettings(priceForm)
    ElMessage.success('保存成功')
  } catch (_e) { /* ignore */ }
  finally {
    priceLoading.value = false
  }
}
</script>

<style scoped>
.settings-form {
  max-width: 800px;
}

.unit {
  margin-left: 8px;
  color: #606266;
}

.form-tip {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.refund-rules {
  margin-bottom: 20px;
}

.rule-header {
  display: flex;
  gap: 20px;
  padding: 10px 0;
  font-weight: bold;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
}

.rule-header span:first-child {
  width: 150px;
}

.rule-header span:nth-child(2) {
  width: 150px;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.rule-text {
  color: #606266;
}

.rule-tip {
  margin-top: 20px;
}
</style>
