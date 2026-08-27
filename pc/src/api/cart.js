import http from './http'
export const addCart = (data) => http.post('/shop/cart', data)
export const getCartList = () => http.get('/shop/cart')
export const updateCartNum = (id, num) => http.put(`/shop/cart/num/${id}?num=${num}`)
export const updateSelected = (id, selected) => http.put(`/shop/cart/selected/${id}?selected=${selected}`)
export const removeCart = (id) => http.delete('/shop/cart/' + id)
