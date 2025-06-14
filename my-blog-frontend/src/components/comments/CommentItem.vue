<!-- src/components/comments/CommentItem.vue -->
<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <el-avatar size="small">{{ comment.author.username.charAt(0).toUpperCase() }}</el-avatar>
    </div>
    <div class="comment-main">
      <div class="comment-header">
        <strong class="author-name">{{ comment.author.username }}</strong>
        <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
      </div>
      <div class="comment-content">{{ comment.content }}</div>
      <div class="comment-actions">
        <el-button link type="primary" size="small" @click="toggleLike" :loading="likeLoading">
          <el-icon><CaretTop /></el-icon>
          <span>{{ localLiked ? '已赞' : '赞' }} ({{ localLikeCount }})</span>
        </el-button>
        <el-button link type="primary" size="small" @click="showReplyForm = !showReplyForm">回复</el-button>
        <el-popconfirm v-if="canDelete" title="确定要删除这条评论吗？" @confirm="handleDelete">
          <template #reference>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </div>

      <CommentForm
          v-if="showReplyForm"
          :post-id="comment.postId"
          :parent-id="comment.id"
          :placeholder="`回复 @${comment.author.username}`"
          button-text="回复"
          @comment-submitted="onReplySubmitted"
          class="reply-form"
      />

      <div v-if="comment.children && comment.children.length > 0" class="child-comments">
        <CommentItem
            v-for="child in comment.children"
            :key="child.id"
            :comment="child"
            @comment-deleted="emit('comment-deleted', $event)"
            @comment-submitted="emit('comment-submitted')"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue';
import { useAuthStore } from '@/store/auth';
import { ElMessage } from 'element-plus';
import { likeComment, unlikeComment, deleteComment } from '@/api/commentService';
import CommentForm from './CommentForm.vue';
import { CaretTop } from '@element-plus/icons-vue';

// 递归组件必须有一个 name
defineOptions({ name: 'CommentItem' });

const props = defineProps({
  comment: { type: Object, required: true }
});
const emit = defineEmits(['comment-deleted', 'comment-submitted']);

const authStore = useAuthStore();
const showReplyForm = ref(false);
const likeLoading = ref(false);
const localLikeCount = ref(props.comment.likeCount);
const localLiked = ref(props.comment.likedByCurrentUser);

const canDelete = computed(() => {
  if (!authStore.isAuthenticated) return false;
  return authStore.isAdmin || authStore.user?.username === props.comment.author.username;
});

const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录再点赞！');
    return;
  }
  likeLoading.value = true;
  try {
    const response = localLiked.value
        ? await unlikeComment(props.comment.id)
        : await likeComment(props.comment.id);

    localLikeCount.value = response.likeCount;
    localLiked.value = response.liked;
  } catch(error) {
    console.error("点赞/取消点赞失败:", error);
    ElMessage.error(error.message || '操作失败');
  }
  finally { likeLoading.value = false; }
};

const handleDelete = async () => {
  try {
    await deleteComment(props.comment.id);
    ElMessage.success("删除成功！");
    emit('comment-deleted', props.comment.id);
  } catch(error) {
    console.error("删除评论失败:", error);
    ElMessage.error(error.message || '删除失败');
  }
};

const onReplySubmitted = () => {
  showReplyForm.value = false;
  emit('comment-submitted');
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', { hour12: false });
};

</script>

<style scoped>
.comment-item {
  display: flex;
  margin-top: 20px;
}
.comment-avatar {
  margin-right: 12px;
}
.comment-main {
  flex: 1;
}
.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.author-name {
  font-weight: 600;
  margin-right: 10px;
}
.comment-date {
  font-size: 0.8em;
  color: #909399;
}
.comment-content {
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行 */
}
.comment-actions {
  margin-top: 8px;
  font-size: 0.9em;
  color: #909399;
}
.reply-form {
  margin-top: 15px;
  margin-left: -52px; /* 调整缩进以对齐 */
  padding-left: 52px;
}
.child-comments {
  border-left: 2px solid #f0f2f5;
  padding-left: 20px;
  margin-top: 15px;
}
</style>