import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 统一 axios 实例：自动携带 token、统一处理错误
const http = axios.create({
  baseURL: '',
  timeout: 15000
})

// 请求拦截：附加 Authorization
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：code!=200 统一提示；401 跳登录
http.interceptors.response.use(
  (resp) => {
    const { code, msg } = resp.data
    if (code === 200) {
      return resp.data
    }
    if (code === 401) {
      localStorage.removeItem('admin_token')
      router.push('/login')
    }
    ElMessage.error(msg || '操作失败')
    return Promise.reject(new Error(msg || 'error'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      router.push('/login')
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http
