<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <span>{{ isEdit ? '编辑线路' : '新建线路' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 800px"
      >
        <el-form-item label="线路名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入线路名称" />
        </el-form-item>

        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="请输入副标题" />
        </el-form-item>

        <el-form-item label="线路分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.name"
              :value="cat.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="出发城市">
          <el-input v-model="form.departureCity" placeholder="请输入出发城市" />
        </el-form-item>

        <el-form-item label="目的地" prop="destination">
          <el-input v-model="form.destination" placeholder="请输入目的地" />
        </el-form-item>

        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
          <span style="margin-left: 10px">元</span>
        </el-form-item>

        <el-form-item label="最低价">
          <el-input-number v-model="form.minPrice" :min="0" :precision="2" />
          <span style="margin-left: 10px">元</span>
        </el-form-item>

        <el-form-item label="封面图" prop="coverImage">
          <el-upload
            class="cover-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="详情图片">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="imageFileList"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="标签">
          <div class="tag-input-wrap">
            <el-tag
              v-for="tag in form.tags"
              :key="tag"
              closable
              @close="removeTag(tag)"
              style="margin-right: 8px"
            >{{ tag }}</el-tag>
            <el-input
              v-if="tagInputVisible"
              ref="tagInputRef"
              v-model="tagInputValue"
              size="small"
              style="width: 100px"
              @keyup.enter="addTag"
              @blur="addTag"
            />
            <el-button v-else size="small" @click="showTagInput">+ 添加标签</el-button>
          </div>
        </el-form-item>

        <el-form-item label="费用不含">
          <el-input v-model="form.costExclude" type="textarea" :rows="3" placeholder="费用不含说明" />
        </el-form-item>

        <el-form-item label="预订须知">
          <el-input v-model="form.bookingNotice" type="textarea" :rows="3" placeholder="预订须知" />
        </el-form-item>

        <el-form-item label="温馨提示">
          <el-input v-model="form.tips" type="textarea" :rows="3" placeholder="温馨提示" />
        </el-form-item>

        <el-form-item label="费用包含">
          <el-input v-model="form.costInclude" type="textarea" :rows="3" placeholder="费用包含说明" />
        </el-form-item>

        <!-- 行程编辑 -->
        <el-form-item label="行程安排">
          <div class="itinerary-editor">
            <div
              v-for="(day, dayIndex) in form.itinerary"
              :key="dayIndex"
              class="itinerary-day"
            >
              <div class="day-header">
                <div class="day-badge">D{{ day.day || dayIndex + 1 }}</div>
                <el-input
                  v-model="day.title"
                  placeholder="如：北京 → 昆明"
                  style="flex: 1"
                />
                <el-button type="danger" link @click="removeDay(dayIndex)">删除</el-button>
              </div>

              <!-- 活动列表 -->
              <div class="activities-list">
                <div
                  v-for="(act, actIndex) in day.activities"
                  :key="actIndex"
                  class="activity-item"
                >
                  <el-select v-model="act.icon" placeholder="图标" style="width: 100px">
                    <el-option label="飞机" value="plane" />
                    <el-option label="大巴" value="bus" />
                    <el-option label="景点" value="scenic" />
                    <el-option label="酒店" value="hotel" />
                    <el-option label="餐饮" value="food" />
                    <el-option label="步行" value="walk" />
                    <el-option label="游船" value="boat" />
                    <el-option label="默认" value="default" />
                  </el-select>
                  <el-input
                    v-model="act.time"
                    placeholder="时间(08:00)"
                    style="width: 100px"
                  />
                  <el-input
                    v-model="act.content"
                    placeholder="活动内容"
                    style="flex: 1"
                  />
                  <el-button type="danger" link @click="removeActivity(dayIndex, actIndex)">
                    删除
                  </el-button>
                </div>
                <el-button size="small" @click="addActivity(dayIndex)">+ 添加活动</el-button>
              </div>

              <!-- 餐食和住宿 -->
              <div class="day-footer-inputs">
                <el-input v-model="day.meals" placeholder="餐食说明(如：早中晚)">
                  <template #prepend>餐食</template>
                </el-input>
                <el-input v-model="day.hotel" placeholder="住宿说明(如：昆明五星酒店)">
                  <template #prepend>住宿</template>
                </el-input>
              </div>
            </div>

            <el-button type="primary" @click="addDay">+ 添加一天</el-button>
          </div>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>

        <el-form-item label="推荐">
          <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建线路' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRouteDetail, createRoute, updateRoute } from '@/api/route'
import { getCategoryList } from '@/api/category'
import type { CategoryVO } from '@/types'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const formRef = ref<FormInstance>()
const categoryList = ref<CategoryVO[]>([])

const uploadAction = computed(() => `${import.meta.env.VITE_API_BASE_URL}/admin/file/upload`)
const uploadHeaders = computed(() => ({
  Authorization: localStorage.getItem('token') || '',
}))

interface Activity {
  icon: string
  time: string
  content: string
}

interface ItineraryDay {
  day: number
  title: string
  activities: Activity[]
  meals: string
  hotel: string
}

const form = reactive({
  name: '',
  subtitle: '',
  category: '',
  departureCity: '',
  destination: '',
  originalPrice: 0,
  minPrice: 0,
  coverImage: '',
  images: [] as string[],
  tags: [] as string[],
  costExclude: '',
  bookingNotice: '',
  tips: '',
  costInclude: '',
  itinerary: [] as ItineraryDay[],
  sortOrder: 0,
  isRecommend: 0,
})

const imageFileList = ref<UploadFile[]>([])

const tagInputVisible = ref(false)
const tagInputValue = ref('')
const tagInputRef = ref<InstanceType<typeof import('element-plus')['ElInput']>>()

const rules: FormRules = {
  name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  coverImage: [{ required: true, message: '请上传封面图', trigger: 'change' }],
}

onMounted(async () => {
  await fetchCategories()
  if (isEdit.value) {
    await fetchDetail()
  }
})

async function fetchCategories() {
  try {
    categoryList.value = await getCategoryList()
  } catch (_e) { /* ignore */ }
}

async function fetchDetail() {
  loading.value = true
  try {
    const detail = await getRouteDetail(route.params.id as string)
    Object.assign(form, {
      name: detail.name,
      subtitle: detail.subtitle || '',
      category: detail.category || '',
      departureCity: detail.departureCity || '',
      destination: detail.destination || '',
      originalPrice: detail.originalPrice || 0,
      minPrice: detail.minPrice || 0,
      coverImage: detail.coverImage || '',
      images: detail.images || [],
      tags: detail.tags || [],
      costExclude: detail.costExclude || '',
      bookingNotice: detail.bookingNotice || '',
      tips: detail.tips || '',
      costInclude: detail.costInclude || '',
      itinerary: detail.itinerary || [],
      sortOrder: detail.sortOrder || 0,
      isRecommend: detail.isRecommend || 0,
    })
    imageFileList.value = form.images.map((url, i) => ({
      name: `image-${i}`,
      url,
      uid: i,
    } as unknown as UploadFile))
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}

function handleCoverSuccess(response: any) {
  if (response.code === 0) {
    form.coverImage = response.data
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleImageSuccess(response: any, _file: UploadFile, fileList: UploadFile[]) {
  if (response.code === 0) {
    form.images = fileList
      .map(f => (f.response as any)?.data || f.url)
      .filter(Boolean) as string[]
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleImageRemove(_file: UploadFile, fileList: UploadFile[]) {
  form.images = fileList
    .map(f => (f.response as any)?.data || f.url)
    .filter(Boolean) as string[]
}

function removeTag(tag: string) {
  form.tags = form.tags.filter(t => t !== tag)
}

function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => tagInputRef.value?.input?.focus())
}

function addTag() {
  const val = tagInputValue.value.trim()
  if (val && !form.tags.includes(val)) {
    form.tags.push(val)
  }
  tagInputVisible.value = false
  tagInputValue.value = ''
}

// 行程编辑方法
function addDay() {
  form.itinerary.push({
    day: form.itinerary.length + 1,
    title: '',
    activities: [],
    meals: '',
    hotel: '',
  })
}

function removeDay(dayIndex: number) {
  form.itinerary.splice(dayIndex, 1)
  // 重新编号
  form.itinerary.forEach((day, i) => {
    day.day = i + 1
  })
}

function addActivity(dayIndex: number) {
  form.itinerary[dayIndex].activities.push({
    icon: 'default',
    time: '',
    content: '',
  })
}

function removeActivity(dayIndex: number, actIndex: number) {
  form.itinerary[dayIndex].activities.splice(actIndex, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value) {
      await updateRoute({
        id: route.params.id as string,
        name: form.name,
        subtitle: form.subtitle,
        coverImage: form.coverImage,
        images: form.images,
        tags: form.tags,
        category: form.category,
        departureCity: form.departureCity,
        destination: form.destination,
        originalPrice: form.originalPrice,
        minPrice: form.minPrice,
        costExclude: form.costExclude,
        bookingNotice: form.bookingNotice,
        tips: form.tips,
        costInclude: form.costInclude,
        itinerary: form.itinerary,
        sortOrder: form.sortOrder,
        isRecommend: form.isRecommend,
      })
    } else {
      await createRoute({
        name: form.name,
        subtitle: form.subtitle,
        coverImage: form.coverImage,
        images: form.images,
        tags: form.tags,
        category: form.category,
        departureCity: form.departureCity,
        destination: form.destination,
        originalPrice: form.originalPrice,
        minPrice: form.minPrice,
        costExclude: form.costExclude,
        bookingNotice: form.bookingNotice,
        tips: form.tips,
        costInclude: form.costInclude,
        itinerary: form.itinerary,
        sortOrder: form.sortOrder,
        isRecommend: form.isRecommend,
      })
    }
    ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
    router.push('/route/list')
  } catch (_e) { /* ignore */ }
  finally {
    loading.value = false
  }
}
</script>

<style scoped>
.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-image {
  width: 178px;
  height: 178px;
  object-fit: cover;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

/* 行程编辑器样式 */
.itinerary-editor {
  width: 100%;
}

.itinerary-day {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fafafa;
}

.day-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.day-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.activities-list {
  margin-left: 52px;
  margin-bottom: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.day-footer-inputs {
  display: flex;
  gap: 16px;
  margin-left: 52px;
}

.day-footer-inputs .el-input {
  flex: 1;
}
</style>
