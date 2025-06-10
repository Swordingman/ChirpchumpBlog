<template>
  <div class="home-view">
    <h1>欢迎来到我的博客</h1>
    <el-row :gutter="20">
      <el-col :span="18">
        <div v-if="loading" class="loading-spinner">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="error" class="error-message">
          <el-alert title="加载失败" type="error" :description="error.message || '请稍后再试'" show-icon closable />
        </div>
        <div v-else>
          <div v-if="posts.length === 0" class="no-posts">
            <el-empty description="暂无文章" />
          </div>
          <article v-for="post in posts" :key="post.id" class="post-item">
            <h2>
              <router-link :to="{ name: 'PostDetail', params: { slug: post.slug } }">
                {{ post.title }}
              </router-link>
            </h2>
            <p class="post-meta">
              <span>作者: {{ post.author?.username || '未知' }}</span> |
              <span>发布于: {{ formatDate(post.publishedAt) }}</span>
            </p>
            <div v-if="post.categories && post.categories.length > 0" class="post-categories">
              分类:
              <el-tag
                  v-for="cat in post.categories"
                  :key="cat.id"
                  type="success"
                  size="small"
                  effect="light"
                  style="margin-right: 5px;"
              >
                <router-link :to="{ name: 'CategoryPosts', params: { categorySlug: cat.slug } }" class="category-link">
                  {{ cat.name }}
                </router-link>
              </el-tag>
            </div>
            <!-- 可以选择性展示部分 Markdown 内容的 HTML 预览 -->
            <div class="post-excerpt" v-html="post.contentHtml?.substring(0, 200) + '...'"></div>
            <el-divider />
          </article>

          <el-pagination
              v-if="totalPages > 1"
              background
              layout="prev, pager, next"
              :total="totalElements"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handlePageChange"
              style="margin-top: 20px; text-align: center;"
          />
        </div>
      </el-col>
      <el-col :span="6">
        <!-- 侧边栏，可以放分类列表、标签云等 -->
        <aside class="sidebar">
          <h3>分类</h3>
          <!-- 分类列表组件 -->
          <h3>标签</h3>
          <!-- 标签云组件 -->
        </aside>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 1. 导入必要的模块和函数
import { ref, onMounted, watch } from 'vue' // 从 Vue 导入响应式 API 和生命周期钩子
import { useRoute, useRouter } from 'vue-router' // 从 Vue Router 导入路由相关的 hooks
import { fetchPosts } from '@/api/postService' // 导入我们封装的 API 请求函数
import { ElMessage, ElSkeleton, ElCard, ElTag, ElPagination, ElAlert, ElEmpty } from 'element-plus' // 导入 Element Plus 组件

// 2. 定义响应式状态 (Reactivity State)
const posts = ref([]) // 用于存储文章列表，初始为空数组
const loading = ref(true) // 加载状态，初始为 true
const error = ref(null) // 错误状态，初始为 null (没有错误)

// 分页相关的响应式状态
const currentPage = ref(1)    // 当前页码
const totalPages = ref(0)     // 总页数
const totalElements = ref(0)  // 总记录数
const pageSize = ref(5)      // 每页显示数量 (可以和后端约定或从后端获取)

// 3. 获取路由实例 (Router Instance) 和当前路由信息 (Current Route)
const route = useRoute()   // 相当于 Vue 2 中的 this.$route
const router = useRouter() // 相当于 Vue 2 中的 this.$router

// 4. 定义获取数据的函数
const loadPosts = async (page = 1) => {
  console.log(`尝试加载第 ${page} 页的文章...`)
  loading.value = true // 开始加载，设置 loading 为 true
  error.value = null   // 清除之前的错误

  try {
    // 准备API请求参数
    const params = {
      page: page - 1, // 后端 API 分页通常从 0 开始
      size: pageSize.value,
      status: 'PUBLISHED', // 只获取已发布的文章
      sort: 'publishedAt,desc' // 按发布时间降序排序
    }

    // 调用 API
    const responseData = await fetchPosts(params) // fetchPosts 返回的是 response.data
    console.log('API响应:', responseData)

    // 更新状态
    posts.value = responseData.content || [] // 更新文章列表
    totalPages.value = responseData.totalPages || 0
    totalElements.value = responseData.totalElements || 0
    currentPage.value = (responseData.number || 0) + 1 // 后端页码从0开始，前端显示从1开始

  } catch (err) {
    console.error('加载文章列表失败:', err)
    error.value = err // 设置错误状态
    // 可以使用 Element Plus 的 Message 组件提示用户
    ElMessage({
      message: err.message || '获取文章数据失败，请稍后再试。',
      type: 'error',
    })
  } finally {
    loading.value = false // 加载结束，设置 loading 为 false
  }
}

// 5. 定义处理分页变化的函数
const handlePageChange = (newPage) => {
  console.log(`分页改变，跳转到第 ${newPage} 页`)
  // 更新路由查询参数，而不是直接调用 loadPosts
  // 这样可以利用浏览器历史记录，并且可以通过 URL 分享特定页
  router.push({ query: { ...route.query, page: newPage } })
}

// 6. 定义辅助函数 (Helper Functions)
const formatDate = (dateString) => {
  if (!dateString) return '日期未知'
  const date = new Date(dateString)
  // 可以使用更专业的日期格式化库如 date-fns 或 moment.js
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const getExcerpt = (htmlContent, maxLength = 200) => {
  if (!htmlContent) return '';
  // 简单地去除 HTML 标签并截取文本
  const textContent = htmlContent.replace(/<[^>]+>/g, '');
  if (textContent.length <= maxLength) {
    return textContent;
  }
  return textContent.substring(0, maxLength) + '...';
}


// 7. 使用生命周期钩子 (Lifecycle Hooks)
onMounted(() => {
  // 组件挂载后，从路由查询参数获取页码并加载文章
  const pageFromQuery = parseInt(route.query.page) || 1
  console.log(`组件挂载，从查询参数获取页码: ${pageFromQuery}`)
  loadPosts(pageFromQuery)
})

// 8. 使用侦听器 (Watchers)
// 侦听路由查询参数 'page' 的变化
// 当用户通过浏览器前进/后退按钮改变URL中的page参数时，这个侦听器会被触发
watch(
    () => route.query.page,
    (newPageQuery) => {
      const newPageNum = parseInt(newPageQuery) || 1
      console.log(`路由查询参数 page 变化为: ${newPageNum}`)
      // 只有当计算出的页码与当前页码不同时才重新加载，避免不必要的请求
      if (newPageNum !== currentPage.value) {
        loadPosts(newPageNum)
      }
    }
)

// setup 函数会自动返回所有在顶层声明的 ref、reactive、computed、函数等
// 所以不需要显式 return
</script>

<style scoped>
.home-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.post-item {
  margin-bottom: 30px;
}
.post-item h2 a {
  color: #303133;
  text-decoration: none;
  transition: color 0.3s;
}
.post-item h2 a:hover {
  color: #409EFF;
}
.post-meta {
  font-size: 0.9em;
  color: #909399;
  margin-bottom: 10px;
}
.post-categories .el-tag {
  cursor: pointer;
}
.category-link {
  color: inherit; /* 继承el-tag的颜色 */
  text-decoration: none;
}
.category-link:hover {
  text-decoration: underline;
}
.post-excerpt {
  color: #606266;
  line-height: 1.6;
}
.loading-spinner, .error-message, .no-posts {
  margin-top: 20px;
  text-align: center;
}
.sidebar {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}
</style>