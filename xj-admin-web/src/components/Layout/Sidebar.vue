<template>
  <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
    <div class="logo">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
      <span v-if="!sidebarCollapsed" class="logo-text">寻迹千行</span>
    </div>
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#ffffff"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 单个菜单项 -->
          <el-menu-item
            v-if="!route.children || route.children.length === 1"
            :index="route.redirect || route.path"
          >
            <el-icon v-if="getMenuIcon(route)">
              <component :is="getMenuIcon(route)" />
            </el-icon>
            <template #title>{{ getMenuTitle(route) }}</template>
          </el-menu-item>

          <!-- 有子菜单 -->
          <el-sub-menu v-else :index="route.path">
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in getVisibleChildren(route.children)"
              :key="child.path"
              :index="route.path + '/' + child.path"
            >
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAppStore, useUserStore } from '@/stores'
import { routes } from '@/router'
import '@/router/types'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活菜单
const activeMenu = computed(() => {
  const { path } = route
  return path
})

// 获取用户角色
const userRole = computed(() => userStore.userInfo?.role)

// 过滤菜单路由
const menuRoutes = computed(() => {
  return routes.filter((r) => {
    // 过滤隐藏路由
    if (r.meta?.hidden) return false
    // 过滤登录页等
    if (r.path === '/login' || r.path.includes(':pathMatch')) return false
    // 角色权限过滤
    const roles = r.meta?.roles as string[] | undefined
    if (roles && userRole.value && !roles.includes(userRole.value)) return false
    return true
  })
})

// 获取可见子菜单
function getVisibleChildren(children: RouteRecordRaw[]) {
  return children.filter((child) => {
    if (child.meta?.hidden) return false
    const roles = child.meta?.roles as string[] | undefined
    if (roles && userRole.value && !roles.includes(userRole.value)) return false
    return true
  })
}

// 获取菜单图标
function getMenuIcon(r: RouteRecordRaw) {
  if (r.meta?.icon) return r.meta.icon
  if (r.children?.[0]?.meta?.icon) return r.children[0].meta.icon
  return null
}

// 获取菜单标题
function getMenuTitle(r: RouteRecordRaw) {
  if (r.children && r.children.length === 1 && r.children[0]) {
    return r.children[0].meta?.title || r.meta?.title
  }
  return r.meta?.title
}
</script>

<style scoped>
.sidebar {
  width: 210px;
  height: 100vh;
  background-color: #001529;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1001;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background-color: #002140;
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-text {
  margin-left: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
}
</style>
