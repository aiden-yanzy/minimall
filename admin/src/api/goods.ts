import http from './http'

export const goodsPageApi = (params: any) => http.get('/admin/goods', { params })

export const goodsDetailApi = (id: number) => http.get(`/admin/goods/${id}`)

export const goodsCreateApi = (data: any) => http.post('/admin/goods', data)

export const goodsUpdateApi = (id: number, data: any) => http.put(`/admin/goods/${id}`, data)

export const goodsStatusApi = (id: number, status: number) =>
  http.put(`/admin/goods/status/${id}`, null, { params: { status } })

export const goodsDeleteApi = (id: number) => http.delete(`/admin/goods/${id}`)

export const categoryTreeApi = () => http.get('/admin/goods/category/tree')
