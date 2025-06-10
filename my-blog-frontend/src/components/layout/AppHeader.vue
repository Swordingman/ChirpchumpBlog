<template>
  <el-menu mode="horizontal" :router="true" :default-active="activeIndex" class="app-nav-menu">
    <el-menu-item index="/" :route="{ name: 'Home' }">首页</el-menu-item>
    <!-- 可以动态生成分类导航 -->
    <!-- <el-sub-menu index="categories">
      <template #title>分类</template>
      <el-menu-item v-for="cat in categories" :key="cat.id" :index="`/category/${cat.slug}`" :route="{name: 'CategoryPosts', params: {categorySlug: cat.slug}}">
        {{ cat.name }}
      </el-menu-item>
    </el-sub-menu> -->
    <el-menu-item index="/archives" :route="{ name: 'Archives' }">归档</el-menu-item>
    <el-menu-item index="/about" :route="{ name: 'About' }">关于</el-menu-item>

    <div style="flex-grow: 1;"></div> <!-- 占位符，将右侧内容推到最右边 -->

    <el-menu-item index="/admin" :route="{ name: 'AdminDashboard' }" v-if="!authStore.isAuthenticated">后台管理</el-menu-item>
    <el-sub-menu index="admin-actions" v-if="authStore.isAuthenticated">
      <template #title>{{ authStore.user?.username || '管理员' }}</template>
      <el-menu-item index="/admin/posts" :route="{name: 'AdminPosts'}">文章管理</el-menu-item>
      <el-menu-item @click="handleLogout">退出登录</el-menu-item>
    </el-sub-menu>
    <el-menu-item index="/admin/login" :route="{ name: 'AdminLogin' }" v-if="!authStore.isAuthenticated">登录</el-menu-item>
  </el-menu>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth' // 引入 auth store
import { ElMessage, ElMessageBox } from 'element-plus'


const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const activeIndex = ref('/')

// 监听路由变化，更新导航栏的激活项
watch(
    () => route.path,
    (newPath) => {
      // 根据新路径找到最匹配的导航项
      if (newPath.startsWith('/admin')) {
        // 根据更细的后台路径判断，或者直接给后台路由的导航项一个固定index
        activeIndex.value = '/admin';
      } else if (newPath.startsWith('/category/')) {
        activeIndex.value = '/categories'; // 假设分类有个总入口或保持当前路径
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
    router.push({ name: 'AdminLogin' }) // 跳转到登录页
  }).catch(() => {
    // 用户取消操作
  });
}

// 获取分类列表用于导航 (示例)
// const categories = ref([])
// onMounted(async () => {
//   try {
//     categories.value = await fetchAllCategories() // 假设有这个API
//   } catch (error) {
//     console.error("Failed to fetch categories for nav:", error)
//   }
// })
</script>

<style scoped>
.app-nav-menu {
  border-bottom: none; /* 移除 el-menu 默认的下边框，因为 header 已经有边框了 */
  height: 100%; /* 让菜单充满 header 高度 */
  display: flex; /* 使用 flex 布局 */
  align-items: center; /* 垂直居中菜单项 */
}
/* 如果需要让导航菜单项靠左，右侧内容靠右，可以使用 flex-grow: 1; 在中间的元素上 */
</style>