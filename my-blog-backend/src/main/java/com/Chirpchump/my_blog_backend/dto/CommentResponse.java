package com.Chirpchump.my_blog_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private AuthorDto author; // 复用 AuthorDto
    private Long parentId;
    private List<CommentResponse> children; // 嵌套的子评论
    private Integer likeCount;
    private boolean likedByCurrentUser; // 当前登录用户是否已点赞
    private LocalDateTime createdAt;
}