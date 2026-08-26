import { get, post } from './request'

export const getHomeDecorate = () => get('/shop/decorate/home')
export const getArticleList = (categoryId) => get('/shop/article/list', { categoryId })
export const getArticle = (id) => get('/shop/article/' + id)
export const kefuSend = (content) => post('/shop/kefu/send', { content })
export const kefuMessages = () => get('/shop/kefu/messages')
