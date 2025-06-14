<template>
  <el-container class="dashboard-layout">
    <el-aside width="200px">
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/dashboard/my-posts">
          <el-icon><Tickets /></el-icon>
          <span>我的文章</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/profile">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </el-menu-item>

        <el-divider v-if="authStore.isAdmin" />

        <div v-if="authStore.isAdmin">
          <div class="menu-title">管理员面板</div>
          <el-menu-item index="/dashboard/admin/all-posts">
            <el-icon><DocumentCopy /></el-icon>
            <span>所有文章</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/admin/categories">
            <el-icon><Folder /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/admin/tags">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </div>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const route = useRoute();
const activeMenu = computed(() => route.path);
</script>