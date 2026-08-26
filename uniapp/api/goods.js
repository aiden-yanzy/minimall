import { get } from './request'

// 商品分类树
export const getCategoryTree = () => get('/shop/goods/category')
// 商品列表
export const getGoodsList = (params) => get('/shop/goods', params)
// 商品详情
export const getGoodsDetail = (id) => get('/shop/goods/' + id)
