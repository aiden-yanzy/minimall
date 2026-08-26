import http from './http'

export const withdrawListApi = (status?: number) => http.get('/admin/wallet/withdraws', { params: { status } })
export const withdrawApproveApi = (id: number) => http.post(`/admin/wallet/withdraws/${id}/approve`)
export const withdrawRejectApi = (id: number, reason?: string) =>
  http.post(`/admin/wallet/withdraws/${id}/reject`, null, { params: { reason } })
