import http from './http'
export const commentListApi = () => http.get('/admin/comment')
export const commentShowApi = (id: number, isShow: number) => http.put(`/admin/comment/${id}/show`, null, { params: { isShow } })
