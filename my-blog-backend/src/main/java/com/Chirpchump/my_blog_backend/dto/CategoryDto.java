package com.Chirpchump.my_blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDto {
    private Long id; // 分类ID
    @NotBlank(message = "分类名称不能为空")
    private String name; // 分类名称
    private String slug;
}
