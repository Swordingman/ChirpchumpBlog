import { defineStore } from 'pinia'
import {computed, ref} from 'vue'

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null)
    const user = ref(JSON.parse(localStorage.getItem('user')) || null)

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
        console.log('AuthStore: User logged out');
    }

    return { token, user, setToken, setUser, logout, isAuthenticated, isAdmin }
})