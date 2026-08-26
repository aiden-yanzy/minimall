import { get, post } from './request'

export const register = (data) => post('/shop/auth/register', data)
export const login = (data) => post('/shop/auth/login', data)
export const getMe = () => get('/shop/auth/me')
