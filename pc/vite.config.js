import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  base: '/pc/',
  plugins: [vue()],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: {
    port: 5175,
    proxy: { '/shop': { target: 'http://localhost:8080', changeOrigin: true }, '/admin': { target: 'http://localhost:8080', changeOrigin: true } }
  },
  build: { outDir: 'dist' }
})