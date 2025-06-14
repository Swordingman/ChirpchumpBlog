<template>
  <el-container class="dashboard-layout">
    <el-aside width="200px">
      <!-- ... (Logo 部分) ... -->
      <el-menu :default-active="activeMenu" router>
        <!-- 对所有登录用户可见的菜单 -->
        <el-menu-item index="/dashboard/my-posts">
          <el-icon><Tickets /></el-icon>
          <span>我的文章</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/profile">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </el-menu-item>

        <!-- 分割线 -->
        <el-divider v-if="authStore.isAdmin" />

        <!-- 仅限管理员可见的菜单 -->
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
      <!-- ... (Header 和 Main 部分，类似 AdminLayout) ... -->
      <el-header>
        <!-- ... (右上角用户信息和退出) ... -->
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