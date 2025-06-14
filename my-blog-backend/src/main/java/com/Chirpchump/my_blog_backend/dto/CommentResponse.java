package com.Chirpchump.my_blog_backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private AuthorDto author;
    private Long parentId;
    private List<CommentResponse> children;
    private Integer likeCount;
    private boolean likedByCurrentUser;
    private LocalDateTime createdAt;
}