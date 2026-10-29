import http from './http'
export const userPageApi = (params: any) => http.get('/admin/user', { params })
export const userUpdateApi = (id: number, data: any) => http.put(`/admin/user/${id}`, null, { params: data })
export const levelListApi = () => http.get('/admin/level')
export const levelSaveApi = (data: any) => http.post('/admin/level', data)
export const levelRemoveApi = (id: number) => http.post(`/admin/level/remove/${id}`)

// 会员标签
export const tagListApi = () => http.get('/admin/user/tag')
export const tagSaveApi = (data: any) => http.post('/admin/user/tag', data)
export const tagRemoveApi = (id: number) => http.delete(`/admin/user/tag/${id}`)
export const userTagsApi = (id: number) => http.get(`/admin/user/${id}/tags`)
export const setUserTagsApi = (id: number, tagIds: number[]) => http.post(`/admin/user/${id}/tags`, { tagIds })

// 等级成长值
export const refreshLevelsApi = () => http.post('/admin/user/refresh-levels')
export const refreshLevelApi = (id: number) => http.post(`/admin/user/${id}/refresh-level`)
