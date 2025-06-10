<template>
  <el-config-provider :locale="zhCn">
    <div id="app-container">
      <!-- 渲染 AdminLayout 或 AdminLogin -->
      <router-view v-if="isAdminRouteFamily" v-slot="{ Component, route }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>

      <!-- 渲染前台布局 -->
      <el-container v-else class="app-layout">
        <el-header class="app-header">
          <AppHeader />
        </el-header>
        <el-main class="app-main">
          <router-view v-slot="{ Component, route }">
            <transition name="fade" mode="out-in">
              <component :is="Component" :key="route.path" />
            </transition>
          </router-view>
        </el-main>
        <el-footer class="app-footer">
          <AppFooter />
        </el-footer>
      </el-container>
    </div>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/layout/AppHeader.vue' // 需要创建
import AppFooter from './components/layout/AppFooter.vue' // 需要创建
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs' // Element Plus 中文语言包

const route = useRoute()

// 判断当前路由是否是后台管理相关路由
const isAdminRouteFamily = computed(() => {
  // route.matched 是一个数组，包含当前路由匹配到的所有路由记录 (从父到子)
  // 我们检查这个数组中是否有任何一个路由记录的 name 是 'AdminLayout' 或 'AdminLogin'
  return route.matched.some(record => record.name === 'AdminLayout' || record.name === 'AdminLogin');
})
</script>

<style>
/* 全局样式，或者在 src/styles/global.css 中引入 */
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f4f5f7; /* 可以设置一个浅灰色背景 */
}

#app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-layout {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  padding: 0 20px; /* 移除默认的左右padding，由内部组件控制 */
  height: 60px; /* 固定头部高度 */
  line-height: 60px; /* 垂直居中文本 */
}

.app-main {
  flex-grow: 1; /* 主内容区域占据剩余空间 */
  padding: 20px;
  background-color: #ffffff; /* 内容区白色背景 */
  /* max-width: 1200px; /* 主内容区域最大宽度 */
  /* margin: 0 auto; /* 主内容居中 */
}

.app-footer {
  text-align: center;
  padding: 15px 0;
  color: #909399;
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
  font-size: 0.9em;
}

/* 路由切换过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 重置 Element Plus 的一些默认样式，如果需要 */
.el-main { /* Element Plus 的 el-main 默认有 padding，如果你的 app-main 已经设置了，可以考虑重置或调整 */
  padding: 0; /* 通常我们希望由 app-main 来控制 padding */
}
.el-header, .el-footer { /* Element Plus 的 header/footer 默认有 padding */
  padding: 0;
}
</style>