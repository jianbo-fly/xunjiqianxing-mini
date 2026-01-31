import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResponse } from '@/types'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data

    // 后端 Result<T> 使用 code=0 表示成功
    if (code === 0) {
      return data as any
    }

    // 登录过期
    if (code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error(message))
    }

    // 其他业务错误
    ElMessage.error(message || '操作失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    console.error('响应错误:', error)
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

// 封装请求方法
export function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

export function get<T = any>(url: string, params?: any): Promise<T> {
  return request({ method: 'GET', url, params })
}

export function post<T = any>(url: string, data?: any): Promise<T> {
  return request({ method: 'POST', url, data })
}

export function put<T = any>(url: string, data?: any): Promise<T> {
  return request({ method: 'PUT', url, data })
}

export function del<T = any>(url: string, params?: any): Promise<T> {
  return request({ method: 'DELETE', url, params })
}

export default service
