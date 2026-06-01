import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

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
  return `${config.method}_${config.url}_${JSON.stringify(config.params || {})}_${JSON.stringify(config.data || {})}`
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
    removePendingRequest(config)

    const controller = new AbortController()
    config.signal = controller.signal
    pendingRequests.set(getRequestKey(config), controller)

    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
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
      case 401:
        ElMessage.error('登录已过期，请重新登录')
        {
          const userStore = useUserStore()
          userStore.clearToken()
        }
        router.push('/login')
        break
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
