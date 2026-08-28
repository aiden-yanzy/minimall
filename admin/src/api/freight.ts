import http from './http'
export const freightTemplatesApi = () => http.get('/admin/freight/template')
export const freightRulesApi = (id: number) => http.get(`/admin/freight/template/${id}/rules`)
export const freightSaveApi = (data: any) => http.post('/admin/freight/template', data)
export const freightDeleteApi = (id: number) => http.delete(`/admin/freight/template/${id}`)
