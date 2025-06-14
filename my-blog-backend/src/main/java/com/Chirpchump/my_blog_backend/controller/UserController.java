package com.Chirpchump.my_blog_backend.controller;

import com.Chirpchump.my_blog_backend.dto.PasswordChangeRequest; // 需要创建
import com.Chirpchump.my_blog_backend.service.UserService; // 需要创建
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody PasswordChangeRequest request) {

        userService.changePassword(currentUser.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "密码修改成功！"));
    }
}
