package com.Chirpchump.my_blog_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String username;
    private String role; // 或 Collection<String> roles

    public LoginResponse(String accessToken, String username, String role) {
        this.accessToken = accessToken;
        this.username = username;
        this.role = role;
    }
}
