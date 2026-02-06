import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'

// 布局组件
import Layout from '@/components/Layout/index.vue'

// 路由配置
export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'DataLine' },
      },
    ],
  },
  {
    path: '/route',
    component: Layout,
    redirect: '/route/list',
    meta: { title: '线路管理', icon: 'Location' },
    children: [
      {
        path: 'list',
        name: 'RouteList',
        component: () => import('@/views/route/list.vue'),
        meta: { title: '线路列表' },
      },
      {
        path: 'create',
        name: 'RouteCreate',
        component: () => import('@/views/route/form.vue'),
        meta: { title: '新建线路', hidden: true },
      },
      {
        path: 'edit/:id',
        name: 'RouteEdit',
        component: () => import('@/views/route/form.vue'),
        meta: { title: '编辑线路', hidden: true },
      },
      {
        path: 'package/:id',
        name: 'RoutePackage',
        component: () => import('@/views/route/package.vue'),
        meta: { title: '套餐管理', hidden: true },
      },
      {
        path: 'audit',
        name: 'RouteAudit',
        component: () => import('@/views/route/audit.vue'),
        meta: { title: '线路审核', roles: ['admin'] },
      },
    ],
  },
  {
    path: '/order',
    component: Layout,
    redirect: '/order/list',
    meta: { title: '订单管理', icon: 'Document' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '订单列表' },
      },
      {
        path: 'detail/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: { title: '订单详情', hidden: true },
      },
      {
        path: 'refund',
        name: 'RefundList',
        component: () => import('@/views/order/refund.vue'),
        meta: { title: '退款管理' },
      },
    ],
  },
  {
    path: '/custom',
    component: Layout,
    redirect: '/custom/list',
    meta: { title: '定制需求', icon: 'Edit' },
    children: [
      {
        path: 'list',
        name: 'CustomList',
        component: () => import('@/views/custom/list.vue'),
        meta: { title: '需求列表' },
      },
      {
        path: 'detail/:id',
        name: 'CustomDetail',
        component: () => import('@/views/custom/detail.vue'),
        meta: { title: '需求详情', hidden: true },
      },
    ],
  },
  {
    path: '/leader',
    component: Layout,
    redirect: '/leader/list',
    meta: { title: '领队管理', icon: 'Flag', roles: ['admin'] },
    children: [
      {
        path: 'list',
        name: 'LeaderList',
        component: () => import('@/views/leader/list.vue'),
        meta: { title: '领队列表' },
      },
      {
        path: 'detail/:id',
        name: 'LeaderDetail',
        component: () => import('@/views/leader/detail.vue'),
        meta: { title: '领队详情', hidden: true },
      },
    ],
  },
  {
    path: '/promoter',
    component: Layout,
    redirect: '/promoter/list',
    meta: { title: '推广员管理', icon: 'Promotion', roles: ['admin'] },
    children: [
      {
        path: 'list',
        name: 'PromoterList',
        component: () => import('@/views/promoter/list.vue'),
        meta: { title: '推广员列表' },
      },
      {
        path: 'detail/:id',
        name: 'PromoterDetail',
        component: () => import('@/views/promoter/detail.vue'),
        meta: { title: '推广员详情', hidden: true },
      },
    ],
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    meta: { title: '用户管理', icon: 'User', roles: ['admin'] },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户列表' },
      },
      {
        path: 'member',
        name: 'MemberList',
        component: () => import('@/views/user/member.vue'),
        meta: { title: '会员管理' },
      },
    ],
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/supplier',
    meta: { title: '系统管理', icon: 'Setting', roles: ['admin'] },
    children: [
      {
        path: 'supplier',
        name: 'SupplierList',
        component: () => import('@/views/system/supplier.vue'),
        meta: { title: '供应商管理' },
      },
      {
        path: 'banner',
        name: 'BannerList',
        component: () => import('@/views/system/banner.vue'),
        meta: { title: 'Banner管理' },
      },
      {
        path: 'category',
        name: 'CategoryList',
        component: () => import('@/views/system/category.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/settings.vue'),
        meta: { title: '系统设置' },
      },
    ],
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 白名单路由
const whiteList = ['/login']

// 路由守卫
router.beforeEach((to, _from, next) => {
  NProgress.start()

  // 设置页面标题
  document.title = `${to.meta.title || ''} - ${import.meta.env.VITE_APP_TITLE}`

  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
