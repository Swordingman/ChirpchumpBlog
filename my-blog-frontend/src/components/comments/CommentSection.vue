<!-- src/components/comments/CommentSection.vue -->
<template>
  <div class="comment-section">
    <h3>{{ totalComments }} 条评论</h3>

    <CommentForm @comment-submitted="loadComments" :post-id="postId" />

    <el-divider />

    <div v-if="loading" v-loading="loading" element-loading-text="加载评论中..." style="height: 100px;"></div>
    <div v-else-if="error" class="error-message">
      <el-alert type="error" title="评论加载失败" :description="error.message" show-icon />
    </div>
    <div v-else-if="comments.length === 0" class="no-comments">
      <el-empty description="暂无评论，快来抢沙发吧！" />
    </div>
    <div v-else class="comment-list">
      <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          @comment-deleted="loadComments"
          @comment-submitted="loadComments"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps, computed } from 'vue';
import CommentForm from './CommentForm.vue';
import CommentItem from './CommentItem.vue';
import { fetchCommentsByPost } from '@/api/commentService';

const props = defineProps({
  postId: { type: Number, required: true }
});

const comments = ref([]);
const loading = ref(true);
const error = ref(null);

const totalComments = computed(() => {
  let count = 0;
  function countComments(commentList) {
    for (const comment of commentList) {
      count++;
      if (comment.children) {
        countComments(comment.children);
      }
    }
  }
  countComments(comments.value);
  return count;
});

const loadComments = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await fetchCommentsByPost(props.postId);
    // 后端返回的数据中，子评论可能 postId 为 null，需要手动补上
    function addPostId(commentList, postId) {
      return commentList.map(c => {
        const newComment = { ...c, postId: postId };
        if (newComment.children) {
          newComment.children = addPostId(newComment.children, postId);
        }
        return newComment;
      });
    }
    comments.value = addPostId(response, props.postId);

  } catch (err) {
    console.error("加载评论失败", err);
    error.value = err;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadComments();
});
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
}
.no-comments {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>