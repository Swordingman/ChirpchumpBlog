package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.CommentRequest;
import com.Chirpchump.my_blog_backend.dto.CommentResponse;
import java.util.List;
import java.util.Map;

public interface CommentService {

    List<CommentResponse> getCommentsByPostId(Long postId);

    CommentResponse createComment(CommentRequest commentRequest);

    void deleteComment(Long commentId);

    Map<String, Object> likeComment(Long commentId);

    Map<String, Object> unlikeComment(Long commentId);
}