<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="admin-aside">
      <div class="logo-container">
        <img src="/vite.svg" alt="Logo" class="logo-img" v-if="!isCollapse"/> <!-- 你可以替换成自己的Logo -->
        <span v-if="!isCollapse" class="logo-text">博客后台</span>
        <el-icon v-else class="logo-icon-collapsed"><ElemeFilled /></el-icon>
      </div>
      <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
      >
        <el-menu-item index="/admin" :route="{ name: 'AdminDashboard' }">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="content-management">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/admin/posts" :route="{ name: 'AdminPosts' }">
            <el-icon><Tickets /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories" :route="{ name: 'AdminCategories' }"> <!-- 假设有分类管理 -->
            <el-icon><Folder /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tags" :route="{ name: 'AdminTags' }"> <!-- 假设有标签管理 -->
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/settings" :route="{ name: 'AdminSettings' }"> <!-- 假设有设置页面 -->
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon @click="toggleSideBar" class="collapse-icon"><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          <el-breadcrumb separator="/">
            <!-- 面包屑导航，可以根据路由动态生成 -->
            <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path" :to="item.path ? { path: item.path } : null">
              {{ item.meta?.title || item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar size="small" :src="userAvatar" style="margin-right: 8px;">{{ authStore.user?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
              {{ authStore.user?.username || '管理员' }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main-content">
        <keep-alive :include="cachedViews">
          <router-view v-slot="{ Component, route }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" :key="route.fullPath" />
            </transition>
          </router-view>
        </keep-alive>
      </el-main>
      <!-- <el-footer>Footer</el-footer> -->
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入 Element Plus Icons (如果你是按需引入或全局注册不完整)
import {
  DataLine, Document, Setting, Fold, Expand, ArrowDown, Tickets, Folder, PriceTag, ElemeFilled
} from '@element-plus/icons-vue'

const isCollapse = ref(false) // 侧边栏是否折叠
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 激活的菜单项，根据当前路由路径匹配
const activeMenu = computed(() => {
  const { path, meta } = route;
  if (meta.activeMenu) { // 如果路由元信息指定了激活菜单
    return meta.activeMenu;
  }
  return path; // 默认使用当前路径
});


// 面包屑导航数据
const breadcrumbItems = computed(() => {
  return route.matched.filter(item => item.meta && item.meta.title && item.name); // 过滤掉没有 title 或 name 的
});


const toggleSideBar = () => {
  isCollapse.value = !isCollapse.value
}

const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png') // 默认头像或从用户信息获取

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    // router.push('/admin/profile') // 跳转到个人中心
    ElMessage.info('个人中心功能待开发');
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    authStore.logout()
    ElMessage.success('已成功退出登录')
    router.push({ name: 'AdminLogin' })
  }).catch(() => {
    // 用户取消操作
  });
}

// 缓存的视图 (可选，用于 <keep-alive>)
const cachedViews = ref([]) // 例如 ['AdminPostList', 'AdminCategoryList'] 存储组件的 name


</script>

<style lang="scss" scoped> // 使用 SCSS
.admin-layout {
  height: 100vh;
  overflow: hidden; // 防止内部滚动条影响布局

  .admin-aside {
    background-color: #304156;
    transition: width 0.28s;
    overflow-x: hidden; // 折叠时隐藏水平滚动条

    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #2b2f3a; // Slightly darker than menu
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      white-space: nowrap; // 防止文字换行

      .logo-img {
        height: 32px;
        width: 32px;
        margin-right: 8px;
      }
      .logo-icon-collapsed {
        font-size: 24px;
      }
    }

    .admin-menu {
      border-right: none; // 去掉 el-menu 的右边框
      height: calc(100vh - 60px); // 减去 logo 区域高度
      overflow-y: auto; // 当菜单项多时，允许垂直滚动

      // 解决折叠时文字不隐藏的问题
      &:not(.el-menu--collapse) {
        width: 200px;
      }
    }
  }

  .main-container {
    display: flex;
    flex-direction: column;
  }

  .admin-header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 15px;
    height: 60px;

    .header-left {
      display: flex;
      align-items: center;
      .collapse-icon {
        font-size: 20px;
        cursor: pointer;
        margin-right: 15px;
      }
    }

    .header-right {
      .el-dropdown-link {
        cursor: pointer;
        display: flex;
        align-items: center;
      }
    }
  }

  .admin-main-content {
    flex-grow: 1;
    padding: 20px;
    background-color: #f0f2f5; // 主内容区背景色
    overflow-y: auto; // 内容过多时允许滚动
  }
}

// 路由切换动画
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>