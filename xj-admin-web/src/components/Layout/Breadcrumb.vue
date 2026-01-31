<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
      <span v-if="item.redirect === 'noRedirect' || !item.path" class="no-redirect">
        {{ item.meta?.title }}
      </span>
      <router-link v-else :to="item.path">{{ item.meta?.title }}</router-link>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, type RouteLocationMatched } from 'vue-router'

const route = useRoute()
const breadcrumbs = ref<RouteLocationMatched[]>([])

function getBreadcrumbs() {
  const matched = route.matched.filter((item) => item.meta && item.meta.title)

  // 如果不是首页，在前面添加首页
  const first = matched[0]
  if (first && first.path !== '/dashboard' && first.path !== '/') {
    breadcrumbs.value = [
      {
        path: '/dashboard',
        meta: { title: '首页' },
      } as any,
      ...matched,
    ]
  } else {
    breadcrumbs.value = matched
  }
}

watch(
  () => route.path,
  () => {
    getBreadcrumbs()
  },
  { immediate: true }
)
</script>

<style scoped>
.no-redirect {
  color: #97a8be;
  cursor: text;
}

:deep(.el-breadcrumb__inner a) {
  font-weight: normal;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #97a8be;
}
</style>
