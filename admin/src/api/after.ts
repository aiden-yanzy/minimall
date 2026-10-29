import http from './http'

// 售后
export const afterListApi = (status?: number) => http.get('/admin/after', { params: { status } })
export const afterApproveApi = (id: number) => http.post(`/admin/after/${id}/approve`)
export const afterRejectApi = (id: number, reason: string) => http.post(`/admin/after/${id}/reject`, null, { params: { reason } })
