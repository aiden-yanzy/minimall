import http from './http'

// 优惠券
export const couponCenter = () => http.get('/shop/coupon/center')
export const couponMine = () => http.get('/shop/coupon/mine')
export const couponClaim = (id) => http.post(`/shop/coupon/claim/${id}`)

// 秒杀
export const seckillList = () => http.get('/shop/seckill/list')

// 积分商城
export const integralGoods = () => http.get('/shop/integral/goods')
export const integralExchange = (data) => http.post('/shop/integral/exchange', data)
export const integralOrders = () => http.get('/shop/integral/orders')

// 售后
export const afterMine = () => http.get('/shop/after')
export const afterCancel = (id) => http.post(`/shop/after/${id}/cancel`)

// 分销
export const distOverview = () => http.get('/shop/distribution/overview')
export const distInvite = () => http.get('/shop/distribution/invite')
export const distCommissions = () => http.get('/shop/distribution/commissions')
export const distTeam = () => http.get('/shop/distribution/team')

// 评价
export const createComment = (data) => http.post('/shop/comment/create', data)

// 售后
export const createAfterSale = (data) => http.post('/shop/after/create', data)
