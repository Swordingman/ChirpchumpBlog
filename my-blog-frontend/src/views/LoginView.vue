// src/views/admin/LoginView.vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>博客后台登录</h2>
        </div>
      </template>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0px" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin(loginFormRef)" style="width: 100%;" size="large">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="error" class="error-message">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {useRoute, useRouter} from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth.js'
import apiClient from '@/api/axiosInstance.js'

const loginFormRef = ref(null)
const loginForm = ref({
  username: '',
  password: ''
})
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const loading = ref(false)
const error = ref('')
const router = useRouter()
const authStore = useAuthStore()
const route = useRoute()

const handleLogin = async (formEl) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      error.value = ''
      try {
        const response = await apiClient.post('/auth/login', {
          username: loginForm.value.username,
          password: loginForm.value.password
        })
        authStore.setToken(response.accessToken)
        authStore.setUser({ username: response.username, role: response.role })

        ElMessage.success('登录成功！')
        if (route.query.redirect) {
          router.push(route.query.redirect);
        } else {
          router.push({ name: 'MyPosts' });
        }
      } catch (err) {
        console.error('登录失败:', err)
        error.value = err?.message || err?.response?.data?.message || '用户名或密码错误，请重试'
        ElMessage.error(error.value)
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单校验失败!')
      return false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
}
.card-header h2 {
  margin: 0;
  font-size: 1.5em;
}
.error-message {
  margin-top: 15px;
}
</style>