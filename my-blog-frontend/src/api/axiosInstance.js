// src/api/axiosInstance.js
import axios from 'axios'
import { useAuthStore } from '../store/auth'

const apiClient = axios.create({
    baseURL: '/api/v1',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    }
})

// 请求拦截器
apiClient.interceptors.request.use(
    (config) => {
        // 在这里加日志，每次请求都会触发
        console.log('Axios Interceptor: Attaching token...');
        const authStore = useAuthStore()

        if (authStore.isAuthenticated) { // 使用 isAuthenticated getter 判断
            config.headers.Authorization = `Bearer ${authStore.token}`
            console.log('Axios Interceptor: Token attached.');
        } else {
            console.log('Axios Interceptor: No token found in store.');
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
apiClient.interceptors.response.use(
    (response) => {
        return response.data
    },
    async (error) => {
        if (error.response) {
            console.error('API Error:', error.response.status, error.response.data)
            if (error.response.status === 401 || error.response.status === 403) {
                // Token 无效或权限不足
                const authStore = useAuthStore()
                authStore.logout() // 清除无效的本地 token

                const router = (await import('../router')).default;

                // 使用 router.push 跳转到登录页
                router.push({
                    name: 'AdminLogin',
                    query: { redirect: router.currentRoute.value.fullPath }
                }).then(() => {
                    // 可以在这里提示用户
                    console.log('Redirecting to login due to 401/403 error.');
                    //ElMessage.error('您的登录已失效或无权限，请重新登录。');
                });
            }
        }
        return Promise.reject(error.response ? error.response.data : error)
    }
)

export default apiClient