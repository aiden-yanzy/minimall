import http from './http'

export const decorateGetApi = () => http.get('/admin/content/decorate')
export const decorateSaveApi = (json: string) => http.post('/admin/content/decorate', json, { headers: { 'Content-Type': 'application/json' } })
export const articleListApi = () => http.get('/admin/content/article/list')
export const articleSaveApi = (data: any) => http.post('/admin/content/article', data)
export const kefuMessagesApi = () => http.get('/admin/kefu/messages')
export const kefuReplyApi = (id: number, reply: string) => http.post(`/admin/kefu/reply/${id}`, null, { params: { reply } })
