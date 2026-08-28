import http from './http'
export const userPageApi = (params: any) => http.get('/admin/user', { params })
export const userUpdateApi = (id: number, data: any) => http.put(`/admin/user/${id}`, null, { params: data })
export const levelListApi = () => http.get('/admin/level')
export const levelSaveApi = (data: any) => http.post('/admin/level', data)
export const levelRemoveApi = (id: number) => http.post(`/admin/level/remove/${id}`)
