package com.Chirpchump.my_blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagDto {
    private Long id; // 标签ID
    @NotBlank(message = "标签名称不能为空")
    private String name; // 标签名称
    private String slug;
}
