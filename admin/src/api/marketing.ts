import http from './http'

// 优惠券
export const couponPageApi = (params: any) => http.get('/admin/marketing/coupon', { params })
export const couponSaveApi = (data: any) => http.post('/admin/marketing/coupon', data)
export const couponStatusApi = (id: number, status: number) =>
  http.put(`/admin/marketing/coupon/status/${id}`, null, { params: { status } })
export const couponDeleteApi = (id: number) => http.delete(`/admin/marketing/coupon/${id}`)

// 满减
export const reductionListApi = () => http.get('/admin/marketing/full-reduction')
export const reductionSaveApi = (data: any) => http.post('/admin/marketing/full-reduction', data)
export const reductionDeleteApi = (id: number) => http.delete(`/admin/marketing/full-reduction/${id}`)