import http from './http'

// 订单
export const orderPageApi = (params: any) => http.get('/admin/order', { params })
export const orderDeliverApi = (id: number, data: any) => http.post(`/admin/order/${id}/deliver`, data)
export const orderBatchDeliverApi = (orderIds: number[], expressId: number, expressName: string, expressNo: string) =>
  http.post('/admin/order/deliver-batch', { orderIds, expressId, expressName, expressNo })

// 物流轨迹（真实落库）
export const orderTracesApi = (id: number) => http.get(`/admin/order/${id}/traces`)
export const orderAddTraceApi = (id: number, description: string, traceTime?: string) =>
  http.post(`/admin/order/${id}/trace`, { description, traceTime })
export const orderAdvanceTraceApi = (id: number) => http.post(`/admin/order/${id}/trace/advance`)

// 订单改价（payAmount 单位：分）
export const orderChangePriceApi = (id: number, payAmount: number, reason: string) =>
  http.post(`/admin/order/${id}/change-price`, { payAmount, reason })

// 统计报表
export const statOverviewApi = (days = 30) => http.get('/admin/stat/overview', { params: { days } })
export const statSaleDetailApi = (params: any) => http.get('/admin/stat/sale-detail', { params })
export const statSaleRankApi = (days = 30, limit = 10) =>
  http.get('/admin/stat/sale-rank', { params: { days, limit } })

// 售后
export const afterPageApi = (params: any) => http.get('/admin/after', { params })
export const afterApproveApi = (id: number) => http.post(`/admin/after/${id}/approve`)
export const afterRejectApi = (id: number, reason?: string) =>
  http.post(`/admin/after/${id}/reject`, null, { params: { reason } })
