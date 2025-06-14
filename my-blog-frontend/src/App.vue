<template>
  <el-config-provider :locale="zhCn">
    <div id="app-container">
      <router-view v-if="isAdminRouteFamily" v-slot="{ Component, route }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>

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
import AppHeader from './components/layout/AppHeader.vue'
import AppFooter from './components/layout/AppFooter.vue'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const route = useRoute()

const isAdminRouteFamily = computed(() => {
  return route.matched.some(record => record.name === 'AdminLayout' || record.name === 'AdminLogin');
})
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f4f5f7;
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
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
}

.app-main {
  flex-grow: 1;
  padding: 20px;
  background-color: #ffffff;
}

.app-footer {
  text-align: center;
  padding: 15px 0;
  color: #909399;
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
  font-size: 0.9em;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.el-main {
  padding: 0;
}
.el-header, .el-footer {
  padding: 0;
}
</style>