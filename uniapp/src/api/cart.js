import { get, post, put, del } from './request'

export const addCart = (data) => post('/shop/cart', data)
export const getCartList = () => get('/shop/cart')
export const updateCartNum = (id, num) => put('/shop/cart/num/' + id + '?num=' + num)
export const updateSelected = (id, selected) => put('/shop/cart/selected/' + id + '?selected=' + selected)
export const removeCart = (id) => del('/shop/cart/' + id)
