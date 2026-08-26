import http from './http'

// 积分商品
export const integralGoodsApi = () => http.get('/admin/integral/goods')
export const integralGoodsSaveApi = (data: any) => http.post('/admin/integral/goods', data)
export const integralGoodsDelApi = (id: number) => http.delete(`/admin/integral/goods/${id}`)
// 兑换订单
export const integralOrdersApi = (status?: number) => http.get('/admin/integral/orders', { params: { status } })
export const integralDeliverApi = (id: number, expressName: string, expressNo: string) =>
  http.post(`/admin/integral/orders/${id}/deliver`, null, { params: { expressName, expressNo } })
// 签到配置
export const signConfigApi = () => http.get('/admin/sign/config')
export const signConfigSaveApi = (data: any) => http.post('/admin/sign/config', data)