<template>
  <div class="comment-form">
    <el-input
        v-model="content"
        :rows="3"
        type="textarea"
        :placeholder="placeholderText"
    />
    <div class="form-actions">
      <el-button
          type="primary"
          @click="handleSubmit"
          :loading="loading"
          :disabled="!content.trim()"
      >
        {{ buttonText }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { createComment } from '@/api/commentService';
import { useAuthStore } from '@/store/auth';

const props = defineProps({
  postId: { type: Number, required: true },
  parentId: { type: Number, default: null },
  placeholder: { type: String, default: '发表你的看法...' },
  buttonText: { type: String, default: '发表评论' },
});

const emit = defineEmits(['comment-submitted']);

const content = ref('');
const loading = ref(false);
const authStore = useAuthStore();

const placeholderText = computed(() => {
  if (!authStore.isAuthenticated) {
    return '请先登录再发表评论';
  }
  return props.placeholder;
});

const handleSubmit = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录！');
    // 可以考虑跳转到登录页
    return;
  }
  if (!content.value.trim()) {
    ElMessage.warning('评论内容不能为空！');
    return;
  }

  loading.value = true;
  try {
    await createComment({
      content: content.value,
      postId: props.postId,
      parentId: props.parentId,
    });
    ElMessage.success('评论成功！');
    content.value = ''; // 清空输入框
    emit('comment-submitted'); // 通知父组件评论已提交
  } catch (error) {
    console.error('评论失败:', error);
    ElMessage.error(error.message || '评论失败，请稍后重试。');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.comment-form {
  margin-top: 15px;
}
.form-actions {
  margin-top: 10px;
  text-align: right;
}
</style>