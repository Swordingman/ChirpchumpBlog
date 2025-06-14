package com.Chirpchump.my_blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentRequest {
    @NotBlank
    private String content;
    @NotNull
    private Long postId;
    private Long parentId; // 可选，如果是回复别人的评论
}