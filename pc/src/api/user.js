import http from './http'
export const login = (data) => http.post('/shop/auth/login', data)
export const register = (data) => http.post('/shop/auth/register', data)
export const getMe = () => http.get('/shop/auth/me')
