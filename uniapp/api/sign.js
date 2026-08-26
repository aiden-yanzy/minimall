import { get, post } from './request'

export const getSignStatus = () => get('/shop/sign/status')
export const doSign = () => post('/shop/sign/do')
export const getSignMonth = (month) => get('/shop/sign/month', { month })
