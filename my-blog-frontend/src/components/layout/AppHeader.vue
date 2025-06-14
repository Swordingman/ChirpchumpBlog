<template>
  <el-menu mode="horizontal" :router="true" :default-active="activeIndex" class="app-nav-menu">
    <el-menu-item index="/" :route="{ name: 'Home' }">首页</el-menu-item>
    <el-menu-item index="/archives" :route="{ name: 'Archives' }">归档</el-menu-item>
    <el-menu-item index="/about" :route="{ name: 'About' }">关于</el-menu-item>

    <div style="flex-grow: 1;"></div>

    <template v-if="!authStore.isAuthenticated">
      <el-menu-item index="/login" :route="{ name: 'Login' }">登录</el-menu-item>
      <el-menu-item index="/register" :route="{ name: 'Register' }">注册</el-menu-item>
    </template>

    <el-sub-menu index="user-actions" v-if="authStore.isAuthenticated">
      <template #title>
        <router-link :to="{ name: 'MyPosts' }" class="user-avatar-link">
          <el-avatar size="small" style="margin-right: 8px;">{{ authStore.user?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
          {{ authStore.user?.username }}
        </router-link>
      </template>
      <el-menu-item index="/dashboard/my-posts" :route="{ name: 'MyPosts' }">
        <el-icon><Tickets/></el-icon>
        <span>我的文章</span>
      </el-menu-item>
      <el-menu-item index="/dashboard/profile" :route="{ name: 'Profile' }">
        <el-icon><User/></el-icon>
        <span>个人资料</span>
      </el-menu-item>
      <el-menu-item index="logout" @click.prevent="handleLogout">
        <el-icon><SwitchButton/></el-icon>
        <span>退出登录</span>
      </el-menu-item>
    </el-sub-menu>

  </el-menu>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Tickets, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeIndex = ref('/')

watch(
    () => route.path,
    (newPath) => {
      if (newPath.startsWith('/admin')) {
        activeIndex.value = '/admin';
      } else if (newPath.startsWith('/category/')) {
        activeIndex.value = '/categories';
      } else if (newPath.startsWith('/tag/')) {
        activeIndex.value = '/tags';
      } else if (newPath === '/' || newPath.startsWith('/post/')) {
        activeIndex.value = '/';
      }
      else {
        activeIndex.value = newPath;
      }
    },
    { immediate: true }
)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    authStore.logout()
    ElMessage.success('已成功退出登录')
    router.push({ name: 'Login' })
  }).catch(() => {
    //
  });
}
</script>

<style scoped>
.app-nav-menu {
  border-bottom: none;
  height: 100%;
  display: flex;
  align-items: center;
}

.user-avatar-link {
  display: flex;
  align-items: center;
  color: inherit;
  text-decoration: none;
  height: 100%;
}
</style>