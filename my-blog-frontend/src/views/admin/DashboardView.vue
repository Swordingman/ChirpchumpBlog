// src/views/admin/DashboardView.vue
<template>
  <div class="dashboard-container">
    <el-card>
      <template #header>
        <div>仪表盘</div>
      </template>
      <p>欢迎回来，{{ authStore.user?.username }}!</p>
      <p>这里可以展示一些博客的统计数据，例如文章总数、评论数、访客数等。</p>
      <p>当前时间: {{ new Date().toLocaleString() }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/auth';
const authStore = useAuthStore();
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
</style>