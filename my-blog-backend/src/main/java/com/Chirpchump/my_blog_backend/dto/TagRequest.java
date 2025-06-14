package com.Chirpchump.my_blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String slug;
}
