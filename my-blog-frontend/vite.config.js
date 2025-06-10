// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    }
  },
  server: {
    port: 5173, // 前端开发服务器端口，可以自定义
    proxy: {
      // 字符串简写写法
      // '/foo': 'http://localhost:4567',
      // 带选项写法
      '/api': { // 匹配所有以 /api 开头的请求
        target: 'http://localhost:8080', // 后端服务实际地址
        changeOrigin: true, // 是否改变请求源头
        // rewrite: (path) => path.replace(/^\/api/, '') // 可选，重写路径，比如去掉/api前缀
                                                      // 如果你的后端API本身就有 /api 前缀，这里就不需要 rewrite
                                                      // 例如你的后端是 /api/v1/posts，前端请求 /api/v1/posts
                                                      // 如果后端是 /v1/posts，前端请求 /api/v1/posts，则需要 rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 使用 proxy 实例
      // '/api': {
      //   target: 'http://jsonplaceholder.typicode.com',
      //   changeOrigin: true,
      //   configure: (proxy, options) => {
      //     // proxy 是 'http-proxy' 的实例
      //   }
      // }
    }
  }
})