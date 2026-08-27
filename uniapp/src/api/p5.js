import { get, post } from './request'

// 拼团
export const getTeamList = () => get('/shop/team/list')
export const teamBuy = (data) => post('/shop/team/buy', data)
export const teamJoin = (orderId) => post('/shop/team/join/' + orderId)
export const getTeamMine = () => get('/shop/team/mine')

// 砍价
export const getBargainList = () => get('/shop/bargain/list')
export const bargainStart = (activityId) => post('/shop/bargain/start/' + activityId)
export const bargainHelp = (initiateId) => post('/shop/bargain/help/' + initiateId)
export const bargainMine = () => get('/shop/bargain/mine')
export const bargainOrder = (initiateId, contact, mobile, address) =>
  post('/shop/bargain/order/' + initiateId + '?contact=' + encodeURIComponent(contact) + '&mobile=' + mobile + '&address=' + encodeURIComponent(address))

// 预售
export const getPresellList = () => get('/shop/presell/list')
export const presellOrder = (data) => post('/shop/presell/order', data)
export const presellPayDeposit = (orderId) => post('/shop/presell/pay-deposit/' + orderId)
export const presellPayFinal = (orderId) => post('/shop/presell/pay-final/' + orderId)
export const presellMine = () => get('/shop/presell/mine')
