import { get, post } from './request'

// 售后（后端口径：type 1仅退款 2退货退款 3换货；status 0待处理 1同意 2拒绝 3已退款 4已完成）
export const createAfterSale = (data) => post('/shop/after/create', data)
export const myAfterSales = () => get('/shop/after')
export const cancelAfterSale = (id) => post('/shop/after/' + id + '/cancel')
