import http from './http'
export const roleListApi = () => http.get('/admin/role')
export const menuTreeApi = () => http.get('/admin/role/menus')
export const roleSaveApi = (data: any) => http.post('/admin/role', data)
export const settingsAllApi = () => http.get('/settings/admin/all')
export const settingsSaveApi = (group: string, data: any) => http.post(`/settings/admin/${group}`, data)
