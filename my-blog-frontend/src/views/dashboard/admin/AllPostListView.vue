<template>
  <div class="post-list-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文章管理</span>
          <el-button type="primary" :icon="Plus" @click="handleCreatePost">新建文章</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="filters" @submit.prevent="loadPosts(1)" class="filter-form">
        <el-form-item label="标题">
          <el-input v-model="filters.title" placeholder="文章标题关键词" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="文章状态" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPosts(1)">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="posts" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleEditPost(row.id)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="author.username" label="作者" width="120" />
        <el-table-column label="分类" width="150">
          <template #default="{ row }">
            <el-tag v-for="cat in row.categories" :key="cat.id" size="small" style="margin-right: 5px;">
              {{ cat.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag.id" type="info" size="small" style="margin-right: 5px; margin-bottom: 5px;">
              {{ tag.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="170">
          <template #default="{ row }">{{ formatDate(row.publishedAt) }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" :icon="Edit" @click="handleEditPost(row.id)" />
            <el-popconfirm
                title="确定要删除这篇文章吗？"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleDeletePost(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete" />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-if="totalPages > 1"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalElements"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          style="margin-top: 20px; text-align: right;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { fetchPosts, deletePostAdmin } from '@/api/postService.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const posts = ref([])
const loading = ref(true)
const currentPage = ref(1)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = ref(10)

const filters = reactive({
  title: '',
  status: '',
})

const router = useRouter()

const loadPosts = async (page = currentPage.value) => {
  loading.value = true
  try {
    const params = {
      page: page - 1,
      size: pageSize.value,
      sort: 'updatedAt,desc',
      status: filters.status || null,
    }

    const response = await fetchPosts(params)
    posts.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
    currentPage.value = response.number + 1
  } catch (err) {
    console.error('获取文章列表失败 (Admin):', err)
    ElMessage.error(err.message || '获取文章列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  loadPosts(1)
}

const handlePageChange = (newPage) => {
  loadPosts(newPage)
}

const handleCreatePost = () => {
  router.push({ name: 'PostCreate' })
}

const handleEditPost = (id) => {
  router.push({ name: 'PostEdit', params: { id } })
}

const handleDeletePost = async (id) => {
  try {
    await deletePostAdmin(id)
    ElMessage.success('文章删除成功')
    loadPosts()
  } catch (err) {
    console.error('删除文章失败:', err)
    ElMessage.error(err.message || '删除文章失败')
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', { hour12: false });
}

const formatStatus = (status) => {
  switch (status) {
    case 'PUBLISHED': return '已发布';
    case 'DRAFT': return '草稿';
    case 'ARCHIVED': return '已归档';
    default: return status;
  }
}

const getStatusTagType = (status) => {
  switch (status) {
    case 'PUBLISHED': return 'success';
    case 'DRAFT': return 'warning';
    case 'ARCHIVED': return 'info';
    default: return 'default';
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.post-list-view {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-form {
  margin-bottom: 20px;
}
.el-table .el-button + .el-button {
  margin-left: 8px;
}
</style>