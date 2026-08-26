import { get, post } from './request'

export const getBalance = () => get('/shop/wallet/balance')
export const getTemplates = () => get('/shop/wallet/templates')
export const createRecharge = (data) => post('/shop/wallet/recharge', data)
export const payRecharge = (orderSn) => post('/shop/wallet/recharge/pay?orderSn=' + orderSn)
export const getLogs = (type) => get('/shop/wallet/logs', { type })
export const withdraw = (data) => post('/shop/wallet/withdraw', data)
export const getWithdraws = () => get('/shop/wallet/withdraws')

export const bindReferrer = (id) => post('/shop/distribution/bind?referrerId=' + id)
export const getDistOverview = () => get('/shop/distribution/overview')
