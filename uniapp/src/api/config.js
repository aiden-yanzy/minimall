// 环境配置
//
// H5：经 nginx 同源部署，baseUrl 留空即可（相对路径）
// 小程序：**必须**是完整域名（小程序不认相对路径的接口和图片）
//   开发调试：微信开发者工具勾「不校验合法域名、web-view、TLS 版本以及 HTTPS 证书」
//   正式上线：必须是已备案的 https 域名，并在小程序后台配 request/uploadFile/downloadFile 白名单
//
// 改这一处就能切环境，业务代码不用动
let baseUrl = ''
// #ifdef MP-WEIXIN
baseUrl = 'http://localhost:18082'
// #endif

export default {
  baseUrl,
  /** 站点对外地址（分享链接、邀请链接用） */
  siteUrl: baseUrl
}
