import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5174,
    proxy: {
      // H5 开发环境同源代理到后端
      '/shop': { target: 'http://localhost:8080', changeOrigin: true },
      '/admin': { target: 'http://localhost:8080', changeOrigin: true },
      '/pay': { target: 'http://localhost:8080', changeOrigin: true },
      '/file-api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
