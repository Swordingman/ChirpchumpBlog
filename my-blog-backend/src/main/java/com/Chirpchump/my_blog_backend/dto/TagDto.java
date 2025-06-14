package com.Chirpchump.my_blog_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagDto {
    private Long id;
    @NotBlank(message = "标签名称不能为空")
    private String name;
    private String slug;
}
