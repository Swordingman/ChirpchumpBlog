<template>
  <div class="profile-view">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div>修改密码</div>
          </template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmNewPassword">
              <el-input v-model="passwordForm.confirmNewPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleChangePassword">提交修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div>账户信息</div>
          </template>
          <p><strong>用户名:</strong> {{ authStore.user?.username }}</p>
          <p><strong>角色:</strong> {{ formatRole(authStore.user?.role) }}</p>
          <!-- 可以添加邮箱等其他信息 -->
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/store/auth';
import { updatePassword } from '@/api/authService';

const passwordFormRef = ref(null);
const loading = ref(false);
const authStore = useAuthStore();
const router = useRouter();

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: '',
});

const validateConfirmNewPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致!'));
  } else {
    callback();
  }
};

const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 40, message: '长度在 6 到 40 个字符', trigger: 'blur' },
  ],
  confirmNewPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmNewPassword, trigger: 'blur' },
  ],
});

const handleChangePassword = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const response = await updatePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        });
        ElMessageBox.alert('密码修改成功，请重新登录。', '提示', {
          confirmButtonText: '确定',
          callback: () => {
            authStore.logout();
            router.push({ name: 'Login' });
          },
        });
      } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error(error.message || '修改密码失败，请检查当前密码是否正确。');
      } finally {
        loading.value = false;
      }
    }
  });
};

const formatRole = (role) => {
  if (role === 'ROLE_ADMIN') return '管理员';
  if (role === 'ROLE_USER') return '普通用户';
  return '未知';
};
</script>

<style scoped>
.profile-view {
  padding: 20px;
}
</style>