<template>
  <div class="home-view">
    <h1 class="page-title">食草兽博客</h1>
    <p class="page-subtitle">探索、学习、分享</p>

    <el-row :gutter="30">
      <!-- 主内容区域 -->
      <el-col :xs="24" :sm="24" :md="18">
        <!-- 加载状态 -->
        <div v-if="loading">
          <el-card v-for="n in 3" :key="n" class="skeleton-card" shadow="never">
            <el-skeleton :rows="4" animated />
          </el-card>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="status-wrapper">
          <el-alert title="加载失败" type="error" :description="error.message || '请检查您的网络连接或稍后再试。'" show-icon :closable="false" />
        </div>

        <!-- 内容显示 -->
        <div v-else>
          <!-- 无文章状态 -->
          <div v-if="posts.length === 0" class="status-wrapper">
            <el-empty description="博主很懒，还没有留下任何足迹..." />
          </div>

          <!-- 文章列表 -->
          <transition-group name="list-fade" tag="div">
            <el-card
                v-for="post in posts"
                :key="post.id"
                class="post-card"
                shadow="hover"
                @click="navigateToPost(post.slug)"
            >
              <template #header>
                <div class="card-header">
                  <h2 class="post-title">
                    <!-- 使用 @click.stop 防止事件冒泡到父级 card 的 click 事件 -->
                    <router-link :to="{ name: 'PostDetail', params: { slug: post.slug } }" @click.stop>
                      {{ post.title }}
                    </router-link>
                  </h2>
                </div>
              </template>

              <p class="post-excerpt">{{ getExcerpt(post.contentHtml, 150) }}</p>

              <footer class="card-footer">
                <div class="post-meta">
                  <span class="meta-item">
                    <el-icon><User /></el-icon> {{ post.author?.username || '佚名' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Calendar /></el-icon> {{ formatDate(post.publishedAt) }}
                  </span>
                </div>
                <div v-if="post.categories && post.categories.length > 0" class="post-categories">
                  <el-tag
                      v-for="cat in post.categories"
                      :key="cat.id"
                      type="success"
                      size="small"
                      effect="light"
                      round
                  >
                    <!-- 同样使用 @click.stop -->
                    <router-link :to="{ name: 'CategoryPosts', params: { categorySlug: cat.slug } }" class="category-link" @click.stop>
                      {{ cat.name }}
                    </router-link>
                  </el-tag>
                </div>
              </footer>
            </el-card>
          </transition-group>

          <!-- 分页 -->
          <div v-if="totalPages > 1" class="pagination-container">
            <el-pagination
                background
                layout="prev, pager, next"
                :total="totalElements"
                :page-size="pageSize"
                :current-page="currentPage"
                @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :xs="24" :sm="24" :md="6">
        <aside class="sidebar">
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="sidebar-header">
                <el-icon><CollectionTag /></el-icon>
                <span>文章分类</span>
              </div>
            </template>
            <ul class="category-list">
              <!-- 这里应该是动态数据，暂时用静态示例 -->
              <li><a href="#">技术杂谈 (12)</a></li>
              <li><a href="#">Vue.js 深入 (8)</a></li>
              <li><a href="#">后端之旅 (5)</a></li>
              <li><a href="#">生活随笔 (3)</a></li>
            </ul>
          </el-card>
          <el-card class="sidebar-card" shadow="never">
            <template #header>
              <div class="sidebar-header">
                <el-icon><PriceTag /></el-icon>
                <span>热门标签</span>
              </div>
            </template>
            <div class="tag-cloud">
              <!-- 标签云组件 -->
              <el-tag v-for="tag in ['Vue', 'Java', 'Spring Boot', 'Docker', 'Nginx', '生活', '随想']" :key="tag" effect="plain" round class="custom-tag">{{ tag }}</el-tag>
            </div>
          </el-card>
        </aside>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 1. 导入必要的模块和函数
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPosts } from '@/api/postService'
// 按需导入 Element Plus 组件和图标
import { ElMessage, ElSkeleton, ElCard, ElTag, ElPagination, ElAlert, ElEmpty, ElRow, ElCol, ElIcon } from 'element-plus'
import { User, Calendar, CollectionTag, PriceTag } from '@element-plus/icons-vue'

// 2. 响应式状态 (与原版基本一致)
const posts = ref([])
const loading = ref(true)
const error = ref(null)
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = ref(5)

// 3. 路由实例
const route = useRoute()
const router = useRouter()

// 4. 定义获取数据的函数 (与原版基本一致)
const loadPosts = async (page = 1) => {
  loading.value = true
  error.value = null
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
      status: 'PUBLISHED',
      sort: 'publishedAt,desc'
    }
    const responseData = await fetchPosts(params)
    posts.value = responseData.content || []
    totalPages.value = responseData.totalPages || 0
    totalElements.value = responseData.totalElements || 0
    currentPage.value = (responseData.number || 0) + 1
  } catch (err) {
    console.error('加载文章列表失败:', err)
    error.value = err
    ElMessage.error(err.message || '获取文章数据失败，请稍后再试。')
  } finally {
    loading.value = false
  }
}

// 5. 交互处理函数
const handlePageChange = (newPage) => {
  router.push({ query: { ...route.query, page: newPage } })
}

// 新增：点击卡片跳转到详情页的函数
const navigateToPost = (slug) => {
  router.push({ name: 'PostDetail', params: { slug } })
}

// 6. 辅助函数 (优化 getExcerpt)
const formatDate = (dateString) => {
  if (!dateString) return '未知日期'
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

// 修复并优化摘要获取函数
const getExcerpt = (htmlContent, maxLength = 150) => {
  if (!htmlContent) return '';
  // 1. 创建一个临时的 div 元素
  const tempDiv = document.createElement('div');
  // 2. 将 HTML 字符串放入其中
  tempDiv.innerHTML = htmlContent;
  // 3. 获取纯文本内容
  const textContent = tempDiv.textContent || tempDiv.innerText || '';
  // 4. 截取并添加省略号
  if (textContent.length <= maxLength) {
    return textContent;
  }
  return textContent.substring(0, maxLength).trim() + '...';
}

// 7. 生命周期钩子 (与原版一致)
onMounted(() => {
  const pageFromQuery = parseInt(route.query.page) || 1
  loadPosts(pageFromQuery)
})

// 8. 侦听器 (与原版一致)
watch(
    () => route.query.page,
    (newPageQuery) => {
      const newPageNum = parseInt(newPageQuery) || 1
      if (newPageNum !== currentPage.value) {
        loadPosts(newPageNum)
      }
    }
)
</script>

<style scoped>
/* Google Fonts - 可选，但能极大提升观感 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&display=swap');

.home-view {
  padding: 20px;
  max-width: 1280px;
  margin: 0 auto;
  font-family: 'Noto Sans SC', sans-serif; /* 应用字体 */
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
}

.page-subtitle {
  font-size: 1.1rem;
  font-weight: 300;
  text-align: center;
  color: var(--el-text-color-secondary);
  margin-bottom: 40px;
}

/* 骨架屏卡片样式 */
.skeleton-card {
  margin-bottom: 20px;
  border: 1px solid var(--el-card-border-color);
}

/* 状态容器（错误、空） */
.status-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background-color: var(--el-bg-color-page);
  border-radius: 8px;
}

/* 文章卡片 */
.post-card {
  margin-bottom: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--el-card-border-color);
  border-radius: 8px; /* 更圆润的边角 */
}

.post-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--el-box-shadow-light);
}

.card-header .post-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
}

.post-title a {
  color: var(--el-text-color-primary);
  text-decoration: none;
  transition: color 0.3s;
}

.post-title a:hover {
  color: var(--el-color-primary);
}

.post-excerpt {
  color: var(--el-text-color-regular);
  line-height: 1.7;
  font-size: 0.95rem;
  margin: 16px 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 0.85rem;
  color: var(--el-text-color-secondary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.post-categories .el-tag {
  margin-left: 5px;
}

.category-link {
  color: inherit;
  text-decoration: none;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

/* 侧边栏 */
.sidebar {
  position: sticky; /* 粘性定位，滚动时保持在视图内 */
  top: 20px;
}
.sidebar-card {
  margin-bottom: 20px;
  border: 1px solid var(--el-card-border-color);
  border-radius: 8px;
}
.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li a {
  display: block;
  padding: 10px 15px;
  text-decoration: none;
  color: var(--el-text-color-regular);
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.category-list li a:hover {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.custom-tag {
  cursor: pointer;
  transition: all 0.2s;
}
.custom-tag:hover {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

/* 列表过渡动画 */
.list-fade-enter-active,
.list-fade-leave-active {
  transition: all 0.5s ease;
}
.list-fade-enter-from,
.list-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>