// src/main.js
import { createApp } from 'vue'
import App from './App.vue'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入 Element Plus 样式
// 如果你想使用暗黑模式，可以引入暗黑模式的 CSS
// import 'element-plus/theme-chalk/dark/css-vars.css'

import router from './router' // 下一步会创建
import { createPinia } from 'pinia' // 下一步会用到

const app = createApp(App)
const pinia = createPinia()

app.use(ElementPlus)
app.use(router)
app.use(pinia)

app.mount('#app')