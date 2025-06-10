// src/views/admin/PostEditView.vue
<template>
  <div class="post-edit-view">
    <el-card>
      <template #header>
        <div>{{ isEditMode ? '编辑文章' : '新建文章' }}</div>
      </template>
      <el-form ref="postFormRef" :model="postForm" :rules="postRules" label-width="100px" v-loading="formLoading">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入文章标题" />
        </el-form-item>

        <el-form-item label="自定义Slug" prop="slug">
          <el-input v-model="postForm.slug" placeholder="可选，留空则根据标题自动生成" />
          <div class="slug-tip">Slug是文章的URL友好型标识，例如 "my-first-post"。只能包含小写字母、数字和连字符。</div>
        </el-form-item>

        <el-form-item label="文章内容" prop="contentMd">
          <!-- 这里集成 Markdown 编辑器，例如 mavon-editor 或 Vditor -->
          <!-- 简单起见，先用 textarea 代替 -->
          <el-input
              v-model="postForm.contentMd"
              type="textarea"
              :autosize="{ minRows: 15, maxRows: 30 }"
              placeholder="请输入 Markdown 格式的文章内容"
          />
          <!-- 推荐使用 mavon-editor: https://github.com/hinesboy/mavonEditor -->
          <!-- <mavon-editor v-model="postForm.contentMd" language="zh-CN" /> -->
        </el-form-item>

        <el-form-item label="文章分类" prop="categoryIds">
          <el-select v-model="postForm.categoryIds" multiple placeholder="请选择分类 (可多选)" style="width: 100%;">
            <el-option
                v-for="category in availableCategories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="文章标签" prop="tagIds">
          <el-select
              v-model="postForm.tagIds"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入新标签 (可多选)"
              style="width: 100%;"
              :remote-method="searchTags"
              :loading="tagLoading"
              @change="handleTagChange"
          >
            <!-- remote search + create new tag example -->
            <!-- For simplicity now, just use existing tags, or create a simpler tag input -->
            <el-option
                v-for="tag in availableTags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
            />
          </el-select>
          <div class="slug-tip">输入新标签名后按回车可创建。</div>
        </el-form-item>


        <el-form-item label="文章状态" prop="status">
          <el-radio-group v-model="postForm.status">
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="PUBLISHED">发布</el-radio>
            <el-radio label="ARCHIVED">归档</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit(postFormRef)" :loading="submitLoading">
            {{ isEditMode ? '更新文章' : '立即创建' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createPostAdmin,
  updatePostAdmin,
  fetchPostByIdAdmin // 用于编辑时获取文章详情
} from '@/api/postService'
// 假设有获取分类和标签列表的 API
import { fetchAllCategoriesAdmin, fetchAllTagsAdmin, createTagAdmin } from '@/api/metaService' // 需要创建 metaService.js
import { ElMessage } from 'element-plus'
// 如果使用 mavon-editor
// import { mavonEditor } from 'mavon-editor'
// import 'mavon-editor/dist/css/index.css'

const route = useRoute()
const router = useRouter()
const postFormRef = ref(null)

const postId = computed(() => route.params.id || null) // 从路由获取文章ID (编辑模式)
const isEditMode = computed(() => !!postId.value)

const postForm = reactive({
  id: null,
  title: '',
  slug: '',
  contentMd: '',
  categoryIds: [],
  tagIds: [],
  status: 'DRAFT', // 默认状态
})

const postRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  contentMd: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择文章状态', trigger: 'change' }],
}

const formLoading = ref(false)    // 加载表单数据 (编辑模式)
const submitLoading = ref(false)  // 提交表单
const availableCategories = ref([])
const availableTags = ref([])
const tagLoading = ref(false)

// 加载分类和标签数据 (用于下拉选择)
const loadMeta = async () => {
  try {
    // 假设这些API返回 {id, name} 列表
    // availableCategories.value = await fetchAllCategoriesAdmin();
    // availableTags.value = await fetchAllTagsAdmin();

    // 模拟数据，你需要实现实际的API调用
    availableCategories.value = [
      { id: 1, name: '技术' }, { id: 2, name: '生活' }, { id: 3, name: '随笔' }
    ];
    availableTags.value = [
      { id: 1, name: 'Java' }, { id: 2, name: 'Spring Boot' }, { id: 3, name: 'Vue' }, { id: 4, name: '源码' }
    ];

  } catch (error) {
    ElMessage.error('加载分类或标签失败: ' + error.message)
  }
}

// 编辑模式下加载文章数据
const loadPostData = async () => {
  if (!isEditMode.value) return
  formLoading.value = true
  try {
    const post = await fetchPostByIdAdmin(postId.value)
    postForm.id = post.id
    postForm.title = post.title
    postForm.slug = post.slug
    postForm.contentMd = post.contentMd
    postForm.status = post.status
    postForm.categoryIds = post.categories?.map(cat => cat.id) || []
    postForm.tagIds = post.tags?.map(tag => tag.id) || []
  } catch (error) {
    ElMessage.error('加载文章数据失败: ' + error.message)
    router.push({ name: 'AdminPosts' }) // 加载失败则返回列表页
  } finally {
    formLoading.value = false
  }
}

const handleSubmit = async (formEl) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = { ...postForm }
        if (isEditMode.value) {
          if (payload.id !== parseInt(postId.value, 10)) {
            console.error("Form ID does not match route param ID. Aborting.");
            ElMessage.error("页面数据异常，请刷新后重试。");
            submitLoading.value = false;
            return;
          }
          await updatePostAdmin(postId.value, payload)
          ElMessage.success('文章更新成功！')
        } else {
          delete payload.id
          await createPostAdmin(payload)
          ElMessage.success('文章创建成功！')
        }
        router.push({ name: 'AdminPosts' }) // 成功后跳转回列表页
      } catch (error) {
        console.error('提交文章失败:', error)
        ElMessage.error(error.message || '操作失败，请重试')
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.error('请检查表单填写是否正确！')
      return false
    }
  })
}

// 标签远程搜索和创建 (简化版，实际可能更复杂)
const searchTags = async (query) => {
  if (query) {
    tagLoading.value = true;
    // 模拟远程搜索，实际应调用API
    // const results = await searchTagsAPI(query);
    // availableTags.value = results; // 更新可选标签
    // 这里简单地允许用户输入新标签，提交时后端处理创建逻辑
    setTimeout(() => { // 模拟异步
      tagLoading.value = false;
      // 如果后端支持在创建/更新文章时自动创建不存在的标签，这里不需要额外操作
      // 否则，你可能需要一个专门的创建标签的逻辑
    }, 200);

  } else {
    // availableTags.value = []; // 清空或加载默认标签
  }
}
// 当 el-select 的 allow-create 生效时，如果输入的值不在 options 中，
// 且 filterable 为 true，select 的 v-model 会直接绑定这个输入的新值（字符串）。
// 后端在处理 tagIds 时，需要判断是数字ID还是新的字符串标签名，然后进行创建。
const handleTagChange = (selectedTagValues) => {
  // selectedTagValues 是一个数组，可能包含数字 (已存在的tagId) 或字符串 (新创建的tag name)
  // console.log('Selected tags:', selectedTagValues);
  // 在提交时，后端需要处理这种情况，或者前端在提交前处理，将新标签名先创建并获取ID
  // 为简单起见，我们假设后端能够处理 tagIds 中包含新标签名的情况。
  // 如果后端不能处理，你需要在提交前：
  // 1. 识别出新标签名
  // 2. 调用 createTagAdmin API 创建这些新标签，获取它们的 ID
  // 3. 将新标签的 ID 替换掉 postForm.tagIds 中的字符串名
}


const goBack = () => {
  router.go(-1) // 返回上一页
}

onMounted(async () => {
  await loadMeta(); // 先加载分类和标签
  if (isEditMode.value) {
    await loadPostData(); // 如果是编辑模式，再加载文章数据
  }
})
</script>

<style scoped>
.post-edit-view {
  padding: 20px;
}
.slug-tip {
  font-size: 0.85em;
  color: #909399;
  line-height: 1.2;
}
/* 如果使用 mavon-editor，可能需要调整其高度 */
/* .mavon-editor {
  min-height: 500px;
} */
</style>