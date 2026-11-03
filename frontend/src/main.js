import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { init as initCrypto } from '../utils/index.js'

async function bootstrap() {
  try {
    await initCrypto()
  } catch (e) {
    console.warn('加密初始化失败，部分功能可能受限', e)
  }

  const app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.use(ElementPlus)
  app.mount('#app')
}

bootstrap()
