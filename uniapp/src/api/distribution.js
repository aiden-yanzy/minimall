import { get, post } from './request'

export const distOverview = () => get('/shop/distribution/overview')
export const distInvite = () => get('/shop/distribution/invite')
export const distCommissions = () => get('/shop/distribution/commissions')
export const distTeam = () => get('/shop/distribution/team')
export const distLevel = () => get('/shop/distribution/level')
export const bindByCode = (code) => post('/shop/distribution/bind-code?code=' + encodeURIComponent(code))
export const myReferrer = () => get('/shop/distribution/referrer')
