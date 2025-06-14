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
          <mavon-editor
              v-model="postForm.contentMd"
              ref="mdEditor"
              language="zh-CN"
              style="height: 600px"
              @imgAdd="handleEditorImgAdd"
              @imgDel="handleEditorImgDel"
          />
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
  fetchPostByIdAdmin
} from '@/api/postService.js'
import { ElMessage } from 'element-plus'
import apiClient from '@/api/axiosInstance'

const route = useRoute()
const router = useRouter()
const postFormRef = ref(null)
const mdEditor = ref(null)

const postId = computed(() => route.params.id || null)
const isEditMode = computed(() => !!postId.value)

const postForm = reactive({
  id: null,
  title: '',
  slug: '',
  contentMd: '',
  categoryIds: [],
  tagIds: [],
  status: 'DRAFT',
})

const postRules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  contentMd: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择文章状态', trigger: 'change' }],
}

const formLoading = ref(false)
const submitLoading = ref(false)
const availableCategories = ref([])
const availableTags = ref([])
const tagLoading = ref(false)

const loadMeta = async () => {
  try {
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
    router.push({ name: 'AdminPosts' })
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
        router.push({ name: 'Home' })
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

const handleEditorImgAdd = async (pos, $file) => {
  const formData = new FormData();
  formData.append('image', $file);

  try {
    const response = await apiClient.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    const imageUrl = response.url;
    mdEditor.value?.$img2Url(pos, imageUrl);

  } catch (error) {
    console.error('图片上传失败:', error);
    ElMessage.error('图片上传失败: ' + (error.message || '请检查网络或联系管理员'));
    mdEditor.value?.$imgDelByFilename(pos);
  }
};

const handleEditorImgDel = (pos) => {
  console.log('删除图片:', pos[0]);
}

const searchTags = async (query) => {
  if (query) {
    tagLoading.value = true;
    setTimeout(() => {
      tagLoading.value = false;
    }, 200);
  } else {
    //
  }
}

const handleTagChange = (selectedTagValues) => {
  //
}

const goBack = () => {
  router.go(-1)
}

onMounted(async () => {
  await loadMeta();
  if (isEditMode.value) {
    await loadPostData();
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
</style>