package com.Chirpchump.my_blog_backend.service;

import com.Chirpchump.my_blog_backend.dto.PasswordChangeRequest;

public interface UserService {
    void changePassword(String username, PasswordChangeRequest request);
}