import { get, post } from './request'

export const createOrder = (data) => post('/shop/order/create', data)
export const getOrderList = (status) => get('/shop/order', { status })
export const getOrderDetail = (id) => get('/shop/order/' + id)
export const cancelOrder = (id, reason) => post('/shop/order/' + id + '/cancel?reason=' + (reason || ''))
export const mockPay = (orderSn, payType) => post('/shop/pay/mock', { orderSn, payType: payType || 1 })
