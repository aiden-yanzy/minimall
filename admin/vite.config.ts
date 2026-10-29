import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  // base 必须放在顶层，放在 build 里不生效（会导致部署到 /admin-web/ 后白屏）
  base: '/admin-web/',
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
    outDir: 'dist'
  }
})
