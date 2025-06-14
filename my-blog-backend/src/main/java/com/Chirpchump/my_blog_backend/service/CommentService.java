package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.CommentRequest;
import com.Chirpchump.my_blog_backend.dto.CommentResponse;
import java.util.List;
import java.util.Map;

public interface CommentService {

    /**
     * 根据文章ID获取树状评论列表
     * @param postId 文章ID
     * @return 评论树列表
     */
    List<CommentResponse> getCommentsByPostId(Long postId);

    /**
     * 创建一条新评论
     * @param commentRequest 评论请求数据
     * @return 创建后的评论
     */
    CommentResponse createComment(CommentRequest commentRequest);

    /**
     * 删除一条评论 (包含权限校验)
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 点赞一条评论
     * @param commentId 评论ID
     * @return 包含新点赞数和点赞状态的Map
     */
    Map<String, Object> likeComment(Long commentId);

    /**
     * 取消点赞一条评论
     * @param commentId 评论ID
     * @return 包含新点赞数和点赞状态的Map
     */
    Map<String, Object> unlikeComment(Long commentId);
}