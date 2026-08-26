import http from './http'

export const statsApi = () => http.get('/admin/dashboard/stats')
