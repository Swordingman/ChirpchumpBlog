package com.Chirpchump.my_blog_backend.dto;

import com.Chirpchump.my_blog_backend.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class PostCreateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过255个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String contentMd;

    private String slug;

    private Set<Long> categoryIds;
    private Set<Long> tagIds;
}
