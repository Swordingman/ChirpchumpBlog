package com.Chirpchump.my_blog_backend.dto;

import com.Chirpchump.my_blog_backend.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class PostUpdateRequest {
    @NotNull(message = "更新文章时，文章ID不能为空")
    private Long id;

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 255, message = "文章标题长度不能超过255个字符")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String contentMd;

    private String slug;

    private Set<Long> categoryIds;
    private Set<Long> tagIds;

    private PostStatus status;
}