import { get, post } from './request'

export const getIntegralGoods = () => get('/shop/integral/goods')
export const exchange = (data) => post('/shop/integral/exchange', data)
export const getIntegralOrders = () => get('/shop/integral/orders')
