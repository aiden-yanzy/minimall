import config from '@/api/config'

/**
 * 资源地址补全。
 *
 * H5 同源部署时返回 `/file/xxx.jpg` 就能用；
 * 但**小程序不认相对路径**（图片必须完整域名），所以统一走这里拼 baseUrl。
 */
export function asset(path) {
  if (!path) return ''
  const p = String(path)
  if (/^(https?:)?\/\//.test(p) || p.startsWith('data:')) return p
  return config.baseUrl + p
}
