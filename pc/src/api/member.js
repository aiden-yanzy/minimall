import http from './http'
export const collectStatus = (goodsId) => http.get('/shop/member/collect/status', { params: { goodsId } })
export const toggleCollect = (goodsId) => http.post(`/shop/member/collect/toggle?goodsId=${goodsId}`)
export const getAddresses = () => http.get('/shop/member/address')
export const saveAddress = (data) => http.post('/shop/member/address', data)
export const getComments = (goodsId) => http.get(`/shop/goods/${goodsId}/comments`, { params: { page: 1, size: 6 } })

// 会员等级 / 钱包 / 收藏 / 足迹
export const myLevel = () => http.get('/shop/member/level')
export const levelList = () => http.get('/shop/member/level/list')
export const walletBalance = () => http.get('/shop/wallet/balance')
export const walletLogs = () => http.get('/shop/wallet/logs')
export const collectList = () => http.get('/shop/member/collect/list')
export const footprintList = () => http.get('/shop/member/footprint/list')
export const clearFootprint = () => http.post('/shop/member/footprint')
