import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

let isRedirectingToLogin = false

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求队列用于取消重复请求
const pendingRequests = new Map<string, AbortController>()

const getRequestKey = (config: any) => {
  const dataKey = config.data instanceof FormData
    ? `formData_${Date.now()}_${Math.random()}`
    : JSON.stringify(config.data || {})
  return `${config.method}_${config.url}_${JSON.stringify(config.params || {})}_${dataKey}`
}

// 定期清理过期的 pending requests，防止内存泄漏
const cleanupTimer = setInterval(() => {
  pendingRequests.forEach((controller, key) => {
    if (controller.signal.aborted) {
      pendingRequests.delete(key)
    }
  })
}, 60000)

export function cleanupPendingRequests() {
  clearInterval(cleanupTimer)
  pendingRequests.forEach((controller) => controller.abort())
  pendingRequests.clear()
}

const removePendingRequest = (config: any) => {
  const key = getRequestKey(config)
  if (pendingRequests.has(key)) {
    pendingRequests.get(key)!.abort()
    pendingRequests.delete(key)
  }
}

request.interceptors.request.use(
  (config) => {
    const controller = new AbortController()
    config.signal = controller.signal
    removePendingRequest(config)
    pendingRequests.set(getRequestKey(config), controller)

    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 文件上传使用更长的超时时间
    if (config.data instanceof FormData) {
      config.timeout = 120000 // 2 minutes for file uploads
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    removePendingRequest(response.config)

    const res = response.data
    if (res.code !== 200) {
      const err = new Error(res.message) as Error & { _handled?: boolean }
      // 标记已由拦截器处理，视图层不应重复显示错误
      err._handled = true
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(err)
    }
    return res
  },
  (error) => {
    if (error.config) {
      removePendingRequest(error.config)
    }

    if (axios.isCancel(error)) {
      console.log('请求已取消:', error.message)
      return Promise.reject(error)
    }

    const { response } = error

    if (!response) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }

    switch (response.status) {
      case 400:
        ElMessage.error(response.data?.message || '请求参数错误')
        break
      case 401: {
        const userStore = useUserStore()
        if (!isRedirectingToLogin) {
          isRedirectingToLogin = true
          const message = response.data?.message || '登录已过期，请重新登录'
          ElMessage.error(message)
          userStore.clearToken()
          router.replace('/login')
          setTimeout(() => { isRedirectingToLogin = false }, 1000)
        }
        break
      }
      case 403:
        ElMessage.error('没有权限执行此操作')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 413:
        ElMessage.error('上传文件过大')
        break
      case 500:
      case 502:
      case 503:
        ElMessage.error('服务器繁忙，请稍后重试')
        break
      default:
        ElMessage.error(response.data?.message || `请求失败(${response.status})`)
    }

    return Promise.reject(error)
  }
)

export default request
