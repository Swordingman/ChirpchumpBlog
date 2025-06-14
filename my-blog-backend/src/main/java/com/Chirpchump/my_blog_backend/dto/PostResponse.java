package com.Chirpchump.my_blog_backend.dto;

import com.Chirpchump.my_blog_backend.model.PostStatus;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PostResponse {
    private Long id;
    private String title;
    private String contentMd;
    private String contentHtml;
    private String slug;
    private AuthorDto author;
    private Set<CategoryDto> categories;
    private Set<TagDto> tags;
    private PostStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
