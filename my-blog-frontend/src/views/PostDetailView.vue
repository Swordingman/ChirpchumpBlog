<template>
  <div class="post-detail-view">
    <div v-if="loading" class="loading-spinner">
      <el-skeleton :rows="8" animated />
    </div>
    <div v-else-if="error" class="error-message">
      <el-alert title="加载文章失败" type="error" :description="error.message || '请检查文章是否存在或网络连接'" show-icon closable />
      <el-button @click="goBack" style="margin-top: 10px;">返回首页</el-button>
    </div>
    <article v-else-if="post" class="post-content">
      <h1>{{ post.title }}</h1>
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
      <div v-if="post.tags && post.tags.length > 0" class="post-tags" style="margin-top: 5px;">
        标签:
        <el-tag
            v-for="tag in post.tags"
            :key="tag.id"
            type="info"
            size="small"
            effect="light"
            style="margin-right: 5px;"
        >
          <router-link :to="{ name: 'TagPosts', params: { tagSlug: tag.slug } }" class="tag-link">
            {{ tag.name }}
          </router-link>
        </el-tag>
      </div>
      <el-divider />
      <div v-html="renderedContent" class="markdown-body"></div>
      <comment-section :post-id="post.id" />
    </article>
  </div>
</template>

<script setup>
import {ref, onMounted, watch, computed} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPostBySlug } from '@/api/postService'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from "@/utils/markdown.js";
import CommentSection from "@/components/comments/CommentSection.vue";

const post = ref(null)
const loading = ref(true)
const error = ref(null)
const route = useRoute()
const router = useRouter()

const renderedContent = computed(() => {
  if (post.value && post.value.contentMd) {
    return renderMarkdown(post.value.contentMd);
  }
  return '';
});

const loadPost = async (slug) => {
  if (!slug) {
    error.value = { message: '文章标识 (slug) 缺失' };
    loading.value = false;
    return;
  }
  loading.value = true
  error.value = null
  try {
    post.value = await fetchPostBySlug(slug)
  } catch (err) {
    console.error('获取文章详情失败:', err)
    error.value = err
    ElMessage.error(err.message || '获取文章详情失败')
    if (err.status === 404) {
      // 可以跳转到404页面或显示特定信息
    }
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '未知日期';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
}

const goBack = () => {
  router.push('/');
}

// 组件挂载时或路由参数变化时加载文章
onMounted(() => {
  loadPost(route.params.slug)
})

watch(() => route.params.slug, (newSlug) => {
  if (newSlug) {
    loadPost(newSlug)
  }
})
</script>

<style scoped>
.post-detail-view {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}
.post-content h1 {
  margin-bottom: 0.5em;
}
.post-meta {
  font-size: 0.9em;
  color: #909399;
  margin-bottom: 10px;
}
.post-categories .el-tag, .post-tags .el-tag {
  cursor: pointer;
}
.category-link, .tag-link {
  color: inherit;
  text-decoration: none;
}
.category-link:hover, .tag-link:hover {
  text-decoration: underline;
}
/* 引入 Markdown 样式，例如 GitHub Markdown CSS */
/* 或者使用 Element Plus 的排版样式 */
.markdown-body {
  line-height: 1.7;
  word-wrap: break-word; /* 确保长单词或链接能换行 */
}
.markdown-body :deep(h1), /* 使用 :deep() 穿透 scoped CSS */
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}
.markdown-body :deep(p) {
  margin-bottom: 16px;
}
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 2em;
  margin-bottom: 16px;
}
.markdown-body :deep(pre) {
  border-radius: 6px;
  padding: 1.2em;
  margin: 1.5em 0;
}
.markdown-body :deep(code):not(pre > code) { /* 行内代码 */
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.9em;
}
.markdown-body :deep(img) {
  max-width: 100%; /* 图片响应式 */
  height: auto;
  display: block; /* 避免图片下方有额外空隙 */
  margin: 10px 0;
}
.markdown-body :deep(blockquote) {
  padding: 0 1em;
  color: #6a737d;
  border-left: .25em solid #dfe2e5;
  margin-bottom: 16px;
}
.loading-spinner, .error-message {
  margin-top: 20px;
  text-align: center;
}
</style>