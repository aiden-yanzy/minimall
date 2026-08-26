import { get, post } from './request'

export const getCouponCenter = () => get('/shop/coupon/center')
export const claimCoupon = (id) => post('/shop/coupon/claim/' + id)
export const getMyCoupons = (status) => get('/shop/coupon/mine', { status })