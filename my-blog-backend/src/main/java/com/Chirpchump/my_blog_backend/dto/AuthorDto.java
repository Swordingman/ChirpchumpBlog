package com.Chirpchump.my_blog_backend.dto;

import lombok.Data;

@Data
public class AuthorDto {
    private Long id; // 作者ID
    private String username; // 作者用户名
}