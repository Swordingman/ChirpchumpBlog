package com.Chirpchump.my_blog_backend.dto;

import com.Chirpchump.my_blog_backend.model.PostStatus;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PostResponse {
    private Long id; // 文章ID
    private String title; // 文章标题
    private String contentMd;   // Markdown
    private String contentHtml; // 渲染后的 HTML 内容
    private String slug; // URL友好型标识
    private AuthorDto author;    // 作者信息
    private Set<CategoryDto> categories; // 分类信息
    private Set<TagDto> tags;         // 标签信息
    private PostStatus status; // 文章状态
    private LocalDateTime publishedAt; // 发布时间
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
