import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      // 开发环境把 /admin、/file-api 代理到后端
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/file-api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    // 与 nginx 后台路由 /admin-web/ 对应
    base: '/admin-web/'
  }
})
