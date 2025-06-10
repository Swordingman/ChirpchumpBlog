// src/views/NotFoundView.vue
<template>
  <div class="not-found-view">
    <el-result
        icon="error"
        title="404 - 页面未找到"
        sub-title="抱歉，您访问的页面不存在。"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px); /* 减去可能的header和footer高度 */
  padding: 20px;
}
</style>