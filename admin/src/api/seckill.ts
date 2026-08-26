import http from './http'

// 场次
export const activityListApi = () => http.get('/admin/seckill/activity')
export const activitySaveApi = (data: any) => http.post('/admin/seckill/activity', data)
export const activityStatusApi = (id: number, status: number) =>
  http.put(`/admin/seckill/activity/status/${id}`, null, { params: { status } })
// 秒杀商品
export const seckillGoodsApi = (activityId?: number) => http.get('/admin/seckill/goods', { params: { activityId } })
export const seckillGoodsSaveApi = (data: any) => http.post('/admin/seckill/goods', data)
export const seckillGoodsDelApi = (id: number) => http.delete(`/admin/seckill/goods/${id}`)