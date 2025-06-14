<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>注册新用户</h2>
        </div>
      </template>
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0px" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名 (3-20位)" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="邮箱" prefix-icon="Message" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码 (6-40位)" prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister(registerFormRef)" style="width: 100%;" size="large">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
        <div class="footer-links">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { registerUser } from '@/api/authService'; // 引入新的API服务
import { User, Message, Lock } from '@element-plus/icons-vue';

const registerFormRef = ref(null);
const router = useRouter();
const loading = ref(false);

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error("两次输入的密码不一致!"));
  } else {
    callback();
  }
};

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 40, message: '长度在 6 到 40 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
};

const handleRegister = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await registerUser({
          username: registerForm.username,
          email: registerForm.email,
          password: registerForm.password,
        });
        ElMessage.success(response.message || '注册成功！请登录。');
        router.push({ name: 'Login' });
      } catch (error) {
        console.error('注册失败:', error);
        ElMessage.error(error.message || '注册失败，请稍后重试。');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.register-card {
  width: 400px;
}
.card-header {
  text-align: center;
}
.footer-links {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
}
</style>