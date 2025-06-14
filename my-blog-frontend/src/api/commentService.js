import apiClient from './axiosInstance';

/**
 * 根据文章ID获取评论列表
 * @param {number} postId
 * @returns {Promise<any>}
 */
export const fetchCommentsByPost = (postId) => {
    return apiClient.get(`/comments/post/${postId}`);
};

/**
 * 创建新评论
 * @param {object} data - { content, postId, parentId }
 * @returns {Promise<any>}
 */
export const createComment = (data) => {
    return apiClient.post('/comments', data);
};

/**
 * 删除评论
 * @param {number} commentId
 * @returns {Promise<any>}
 */
export const deleteComment = (commentId) => {
    return apiClient.delete(`/comments/${commentId}`);
};

/**
 * 点赞评论
 * @param {number} commentId
 * @returns {Promise<any>}
 */
export const likeComment = (commentId) => {
    return apiClient.post(`/comments/${commentId}/like`);
};

/**
 * 取消点赞评论
 * @param {number} commentId
 * @returns {Promise<any>}
 */
export const unlikeComment = (commentId) => {
    return apiClient.delete(`/comments/${commentId}/like`);
};