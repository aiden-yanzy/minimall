import config from './config'

const BASE_URL = config.baseUrl

export function request(options) {
  const token = uni.getStorageSync('token') || ''
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      success: (res) => {
        const body = res.data
        if (body && body.code === 200) {
          resolve(body)
        } else if (body && body.code === 401) {
          // 未登录：跳登录页
          uni.removeStorageSync('token')
          uni.navigateTo({ url: '/pages/user/login' })
          reject(body)
        } else {
          uni.showToast({ title: (body && body.msg) || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })
