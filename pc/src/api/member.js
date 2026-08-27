import http from './http'
export const collectStatus = (goodsId) => http.get('/shop/member/collect/status', { params: { goodsId } })
export const toggleCollect = (goodsId) => http.post(`/shop/member/collect/toggle?goodsId=${goodsId}`)
export const getAddresses = () => http.get('/shop/member/address')
export const saveAddress = (data) => http.post('/shop/member/address', data)
export const getComments = (goodsId) => http.get(`/shop/goods/${goodsId}/comments`, { params: { page: 1, size: 6 } })
