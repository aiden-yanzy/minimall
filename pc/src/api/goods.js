import http from './http'
export const getCategoryTree = () => http.get('/shop/goods/category')
export const getGoodsList = (params) => http.get('/shop/goods', { params })
export const getGoodsDetail = (id) => http.get('/shop/goods/' + id)
export const getHomeDecorate = () => http.get('/shop/decorate/home')
