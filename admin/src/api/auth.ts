import http from './http'

export const loginApi = (data: { account: string; password: string }) =>
  http.post('/admin/auth/login', data)

export const meApi = () => http.get('/admin/auth/me')

export const logoutApi = () => http.post('/admin/auth/logout')
