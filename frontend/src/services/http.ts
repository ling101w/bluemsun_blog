import axios from 'axios'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '@/config'

/**
 * Axios 实例：统一处理请求前缀、鉴权信息与错误提示。
 */
const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
    // Sa-Token 默认从 satoken 头部读取，携带 Bearer 前缀以兼容后端配置
    config.headers.satoken = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message ?? error.message ?? '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http

