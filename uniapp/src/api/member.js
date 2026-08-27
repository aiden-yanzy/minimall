import { get, post, put, del } from './request'

// 地址
export const getAddresses = () => get('/shop/member/address')
export const getDefaultAddress = () => get('/shop/member/address/default')
export const saveAddress = (data) => post('/shop/member/address', data)
export const setAddressDefault = (id) => put('/shop/member/address/' + id + '/default')
export const removeAddress = (id) => del('/shop/member/address/' + id)

// 收藏
export const toggleCollect = (goodsId) => post('/shop/member/collect/toggle?goodsId=' + goodsId)
export const collectStatus = (goodsId) => get('/shop/member/collect/status', { goodsId })
export const getCollectList = () => get('/shop/member/collect/list')

// 足迹
export const getFootprintList = () => get('/shop/member/footprint/list')
export const clearFootprint = () => del('/shop/member/footprint')

// 评价
export const getComments = (goodsId, page) => get('/shop/goods/' + goodsId + '/comments', { page, size: 5 })
export const createComment = (data) => post('/shop/comment/create', data)