<template>
  <div class="archives-view">
    <h1>文章归档</h1>
    <el-divider />
    <div v-if="loading" class="loading-spinner">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error" class="error-message">
      <el-alert title="加载归档失败" type="error" :description="error.message || '请稍后再试'" show-icon closable />
    </div>
    <div v-else-if="Object.keys(archives).length === 0" class="no-posts">
      <el-empty description="暂无归档文章" />
    </div>
    <div v-else>
      <el-timeline>
        <template v-for="(months, year) in archives" :key="year">
          <el-timeline-item :timestamp="`${year}年`" placement="top" size="large" type="primary">
            <el-card v-for="(posts, month) in months" :key="month" style="margin-bottom: 20px;">
              <h4>{{ formatMonth(month) }} ({{ posts.length }}篇)</h4>
              <ul>
                <li v-for="post in posts" :key="post.slug">
                  <router-link :to="{ name: 'PostDetail', params: { slug: post.slug } }">
                    {{ post.title }}
                  </router-link>
                  <span class="archive-post-date"> - {{ formatDate(post.publishedAt, false) }}</span>
                </li>
              </ul>
            </el-card>
          </el-timeline-item>
        </template>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchArchives } from '@/api/postService'
import { ElMessage } from 'element-plus'

const archives = ref({})
const loading = ref(true)
const error = ref(null)

const loadArchives = async () => {
  loading.value = true
  error.value = null
  try {
    const rawArchives = await fetchArchives();

    const sortedYears = Object.keys(rawArchives).sort((a, b) => parseInt(b) - parseInt(a));
    const sortedArchives = {};
    for (const year of sortedYears) {
      const months = rawArchives[year];
      const sortedMonths = Object.keys(months).sort((a, b) => parseInt(b) - parseInt(a));
      sortedArchives[year] = {};
      for (const month of sortedMonths) {
        sortedArchives[year][month] = months[month];
        sortedArchives[year][month].sort((a,b) => new Date(b.publishedAt) - new Date(a.publishedAt));
      }
    }
    archives.value = sortedArchives;

  } catch (err) {
    console.error('获取归档数据失败:', err)
    error.value = err
    ElMessage.error(err.message || '获取归档数据失败')
  } finally {
    loading.value = false
  }
}

const formatMonth = (monthStr) => {
  return `${parseInt(monthStr, 10)}月`;
}

const formatDate = (dateString, showYear = true) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const options = { month: 'long', day: 'numeric' };
  if (showYear) {
    options.year = 'numeric';
  }
  return date.toLocaleDateString('zh-CN', options);
}

onMounted(() => {
  loadArchives()
})
</script>

<style scoped>
.archives-view {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}
.el-timeline-item h4 {
  margin-top: 0;
  margin-bottom: 10px;
}
.el-timeline-item ul {
  list-style: none;
  padding-left: 0;
}
.el-timeline-item li {
  margin-bottom: 8px;
  font-size: 1em;
}
.el-timeline-item li a {
  color: #303133;
  text-decoration: none;
}
.el-timeline-item li a:hover {
  color: #409EFF;
}
.archive-post-date {
  font-size: 0.85em;
  color: #909399;
}
.loading-spinner, .error-message, .no-posts {
  margin-top: 20px;
  text-align: center;
}
</style>