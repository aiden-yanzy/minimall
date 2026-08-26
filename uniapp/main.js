import App from './App'
import { createSSRApp } from 'vue'
import * as Pinia from 'pinia'

export function createApp() {
  const app = createSSRApp(App)

  // H5 与小程序均可使用 pinia
  const pinia = Pinia.createPinia()
  app.use(pinia)

  return {
    app,
    Pinia
  }
}
