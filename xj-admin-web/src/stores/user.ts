import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRequest } from '@/types'
import * as authApi from '@/api/auth'
import { setToken, setUserInfo, clearAuth, getToken, getUserInfo as getStoredUserInfo } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getStoredUserInfo())

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const isSupplier = computed(() => userInfo.value?.role === 'supplier')

  // 登录
  async function login(loginForm: LoginRequest) {
    const res = await authApi.login(loginForm)
    // 将后端返回的扁平数据转换为 userInfo 对象
    const user: UserInfo = {
      id: res.userId,
      username: res.username,
      nickname: res.nickname,
      avatar: res.avatar,
      role: res.roleType,
      supplierId: res.supplierId,
      supplierName: res.supplierName,
    }
    token.value = res.token
    userInfo.value = user
    setToken(res.token)
    setUserInfo(user)
    return res
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await authApi.getUserInfo()
    userInfo.value = res
    setUserInfo(res)
    return res
  }

  // 退出登录
  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // 忽略错误
    }
    token.value = null
    userInfo.value = null
    clearAuth()
    router.push('/login')
  }

  // 重置状态
  function resetState() {
    token.value = null
    userInfo.value = null
    clearAuth()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isSupplier,
    login,
    fetchUserInfo,
    logout,
    resetState,
  }
})
