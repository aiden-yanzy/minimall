import http from './http'

// ---------- 拼团 ----------
export const teamListApi = () => http.get('/admin/p5/team')
export const teamSaveApi = (data: any) => http.post('/admin/p5/team', data)
export const teamFoundsApi = () => http.get('/admin/p5/team/founds')
export const teamScanApi = () => http.post('/admin/p5/team/scan')

// ---------- 砍价 ----------
export const bargainListApi = () => http.get('/admin/p5/bargain')
export const bargainSaveApi = (data: any) => http.post('/admin/p5/bargain', data)

// ---------- 预售 ----------
export const presellListApi = () => http.get('/admin/p5/presell')
export const presellSaveApi = (data: any) => http.post('/admin/p5/presell', data)
export const presellOrdersApi = () => http.get('/admin/p5/presell/orders')
export const presellDeliverApi = (id: number, data: any) => http.post(`/admin/p5/presell/orders/${id}/deliver`, data)

// ---------- 签到 ----------
export const signConfigApi = () => http.get('/admin/sign/config')
export const signConfigSaveApi = (data: any) => http.post('/admin/sign/config', data)

// ---------- 分销 ----------
export const distLevelListApi = () => http.get('/admin/distribution/level')
export const distLevelSaveApi = (data: any) => http.post('/admin/distribution/level', data)
export const distLevelRemoveApi = (id: number) => http.delete(`/admin/distribution/level/${id}`)
export const distMembersApi = (keyword?: string) => http.get('/admin/distribution/members', { params: { keyword } })
export const distOrdersApi = (sellerId?: number) => http.get('/admin/distribution/orders', { params: { sellerId } })

// 选商品（活动配置用）
export const goodsOptionsApi = () => http.get('/admin/goods', { params: { page: 1, size: 100 } })
export const goodsItemsApi = (goodsId: number) => http.get(`/admin/goods/${goodsId}`)
