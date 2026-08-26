import http from './http'

// 订单
export const orderPageApi = (params: any) => http.get('/admin/order', { params })
export const orderDeliverApi = (id: number, data: any) => http.post(`/admin/order/${id}/deliver`, data)

// 售后
export const afterPageApi = (params: any) => http.get('/admin/after', { params })
export const afterApproveApi = (id: number) => http.post(`/admin/after/${id}/approve`)
export const afterRejectApi = (id: number, reason?: string) =>
  http.post(`/admin/after/${id}/reject`, null, { params: { reason } })
