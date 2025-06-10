<template>
  <div class="category-posts-view">
    <div v-if="loading" class="loading-spinner">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error-message">
      <el-alert :title="`加载分类 '${categorySlug}' 下的文章失败`" type="error" :description="error.message || '请稍后再试'" show-icon closable />
    </div>
    <div v-else>
      <h1>分类: {{ currentCategoryName || categorySlug }}</h1>
      <el-divider />
      <div v-if="posts.length === 0" class="no-posts">
        <el-empty :description="`分类 '${currentCategoryName || categorySlug }' 下暂无文章`" />
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPostsByCategory } from '@/api/postService'
import { ElMessage } from 'element-plus'

const posts = ref([])
const loading = ref(true)
const error = ref(null)
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = ref(10)
const categorySlug = ref('')
const currentCategoryName = ref(''); // 用于显示分类名称

const route = useRoute()
const router = useRouter()

const loadCategoryPosts = async (slug, page = 1) => {
  if (!slug) return;
  categorySlug.value = slug; // 更新当前页面的 slug
  loading.value = true
  error.value = null
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
      status: 'PUBLISHED',
      sort: 'publishedAt,desc'
    }
    const response = await fetchPostsByCategory(slug, params)
    posts.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
    currentPage.value = response.number + 1
    // 假设文章数据中第一个文章的分类里有当前分类信息，以此获取分类名
    // 更好的方式是单独请求分类详情API，或者API返回分类名
    if (response.content && response.content.length > 0) {
      const postWithCategory = response.content[0];
      const matchedCategory = postWithCategory.categories?.find(cat => cat.slug === slug);
      if (matchedCategory) {
        currentCategoryName.value = matchedCategory.name;
      } else {
        currentCategoryName.value = slug; // Fallback to slug if name not found
      }
    } else {
      // 如果没有文章，可能需要单独API获取分类名
      currentCategoryName.value = slug; // Fallback
    }

  } catch (err) {
    console.error(`获取分类 '${slug}' 文章列表失败:`, err)
    error.value = err
    ElMessage.error(err.message || `获取分类 '${slug}' 文章失败`)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage) => {
  router.push({ params: { categorySlug: categorySlug.value }, query: { page: newPage } })
}

const formatDate = (dateString) => {
  if (!dateString) return '未知日期';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
}

onMounted(() => {
  const slug = route.params.categorySlug
  const pageFromQuery = parseInt(route.query.page) || 1;
  loadCategoryPosts(slug, pageFromQuery)
})

watch(() => route.params.categorySlug, (newSlug) => {
  if (newSlug && newSlug !== categorySlug.value) {
    const pageFromQuery = parseInt(route.query.page) || 1; // 切换分类时，通常回到第一页
    loadCategoryPosts(newSlug, pageFromQuery);
  }
})

watch(() => route.query.page, (newPageQuery) => {
  const pageNum = parseInt(newPageQuery) || 1;
  if (route.params.categorySlug === categorySlug.value && pageNum !== currentPage.value) {
    loadCategoryPosts(categorySlug.value, pageNum);
  }
})
</script>

<style scoped>
.category-posts-view {
  padding: 20px;
  max-width: 900px;
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
.post-excerpt {
  color: #606266;
  line-height: 1.6;
}
.loading-spinner, .error-message, .no-posts {
  margin-top: 20px;
  text-align: center;
}
</style>