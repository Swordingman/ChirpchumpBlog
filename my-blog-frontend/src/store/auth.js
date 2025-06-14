// src/store/auth.js
import { defineStore } from 'pinia'
import {computed, ref} from 'vue'

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null) // 从 localStorage 初始化 token
    const user = ref(JSON.parse(localStorage.getItem('user')) || null) // 从 localStorage 初始化用户信息

    const isAuthenticated = computed(() => !!token.value);
    const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN');

    function setToken(newToken) {
        token.value = newToken
        if (newToken) {
            localStorage.setItem('token', newToken)
        } else {
            localStorage.removeItem('token')
        }
        console.log('AuthStore: Token set to', newToken);
    }


    function setUser(newUser) {
        user.value = newUser
        if (newUser) {
            localStorage.setItem('user', JSON.stringify(newUser))
        } else {
            localStorage.removeItem('user')
        }
        console.log('AuthStore: User set to', newUser);
    }

    function logout() {
        setToken(null)
        setUser(null)
        // 可以在这里添加其他登出逻辑，例如跳转到登录页
        console.log('AuthStore: User logged out');
    }

    return { token, user, setToken, setUser, logout, isAuthenticated, isAdmin }
})