package com.Chirpchump.my_blog_backend.controller;
import com.Chirpchump.my_blog_backend.dto.LoginRequest; // 需要创建
import com.Chirpchump.my_blog_backend.dto.LoginResponse; // 需要创建
import com.Chirpchump.my_blog_backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
// @CrossOrigin(origins = "http://localhost:5173") // 已在SecurityConfig中全局配置
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private Environment environment;

    public AuthController() {
        // 在构造函数打断点，看Spring是否尝试创建这个Bean
        // 或者在这里打印日志
        logger.info("AuthController Bean is being created!");
        // System.out.println("AuthController Bean is being created!"); // 也可以用这个
    }

    @GetMapping("/hello")
    public String sayHello() {
        logger.info("Accessing /hello endpoint. Active profiles: " + String.join(", ", environment.getActiveProfiles())); // 使用注入的Bean
        return "Hello from AuthController!";
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 你可以从 userDetails 中获取更多用户信息放入响应
        return ResponseEntity.ok(new LoginResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities().stream().findFirst().orElse(null).getAuthority()));
    }
}
