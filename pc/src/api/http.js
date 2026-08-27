import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const http = axios.create({ baseURL: '', timeout: 15000 })
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('pc_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
http.interceptors.response.use(
  (resp) => {
    const { code, msg } = resp.data
    if (code === 200) return resp.data
    if (code === 401) { localStorage.removeItem('pc_token'); router.push('/login') }
    ElMessage.error(msg || '操作失败')
    return Promise.reject(new Error(msg || 'error'))
  },
  (error) => {
    if (error.response?.status === 401) { localStorage.removeItem('pc_token'); router.push('/login') }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)
export default http
